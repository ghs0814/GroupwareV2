package com.groupware.service;

import com.groupware.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.time.*;
import java.util.Map;

/** 재고와 연차가 함께 바뀌는 작업은 한 트랜잭션으로 처리합니다. */
@Service
public class WorkflowService {
    private final JdbcTemplate jdbc;
    public WorkflowService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    private int number(Map<String,Object> row,String key) { return ((Number)row.get(key)).intValue(); }
    private void require(boolean condition,String message) {
        if(!condition) throw new IllegalArgumentException(message);
    }
    private Map<String,Object> employee(int employeeNo) {
        Map<String,Object> row=jdbc.queryForMap("SELECT EMP_NO,EMP_NAME,EMP_LEVEL,RETIRED,MANAGER,CUR_LEAVE FROM EMPLOYEE WHERE EMP_NO=?",employeeNo);
        require(!"Y".equals(row.get("RETIRED")),"퇴사한 사원입니다.");
        return row;
    }

    @Transactional
    public boolean createRental(RentalHistoryDTO dto) {
        Map<String,Object> actor=employee(dto.getEmpNo());
        int level=number(actor,"EMP_LEVEL");
        require(level>=1 && level<=5,"직급을 확인해주세요.");
        require(dto.getReqCount()>0,"대여 수량은 1개 이상이어야 합니다.");
        require(dto.getRentalDate()!=null && dto.getReturnDate()!=null && !dto.getReturnDate().before(dto.getRentalDate()),"대여 기간을 확인해주세요.");
        int changed=jdbc.update("UPDATE EQUIPMENT SET REMAIN_COUNT=REMAIN_COUNT-? WHERE EQ_NO=? AND ISCHECKED='Y' AND REMAIN_COUNT>=?",
                dto.getReqCount(),dto.getEqNo(),dto.getReqCount());
        require(changed==1,"비품이 없거나 대여 가능한 재고가 부족합니다.");
        Object[] signs=new Object[10];
        signs[(level-1)*2]=actor.get("EMP_NAME");
        signs[(level-1)*2+1]=Date.valueOf(LocalDate.now());
        jdbc.update("INSERT INTO RENTAL_HISTORY (RENTAL_NO,EMP_NO,EQ_NO,TITLE,REQ_COUNT,RENTAL_DATE,RETURN_DATE,STATUS,APPROVAL_STEP,REASON,SIGN1,SIGN1_DATE,SIGN2,SIGN2_DATE,SIGN3,SIGN3_DATE,SIGN4,SIGN4_DATE,SIGN5,SIGN5_DATE) VALUES (SEQ_RENTAL.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                dto.getEmpNo(),dto.getEqNo(),dto.getTitle(),dto.getReqCount(),dto.getRentalDate(),dto.getReturnDate(),level==5?"대여중":"승인대기",level+1,dto.getContent(),
                signs[0],signs[1],signs[2],signs[3],signs[4],signs[5],signs[6],signs[7],signs[8],signs[9]);
        return true;
    }

    @Transactional
    public void approveRental(int rentalNo,int equipmentNo,int requestedStep,int actorNo,boolean approve) {
        Map<String,Object> actor=employee(actorNo);
        Map<String,Object> rental=jdbc.queryForMap("SELECT EMP_NO,EQ_NO,REQ_COUNT,STATUS,APPROVAL_STEP FROM RENTAL_HISTORY WHERE RENTAL_NO=? FOR UPDATE",rentalNo);
        int step=number(rental,"APPROVAL_STEP");
        require("승인대기".equals(rental.get("STATUS")),"이미 처리된 문서입니다.");
        require(step>=1 && step<=5 && step==requestedStep && step==number(actor,"EMP_LEVEL"),"현재 결재 단계의 권한이 없습니다.");
        require(equipmentNo==number(rental,"EQ_NO"),"비품 정보가 일치하지 않습니다.");
        String name=actor.get("EMP_NAME")+(approve?"":" (差し戻し)");
        String status=approve?(step==5?"대여중":"승인대기"):"반려됨";
        jdbc.update("UPDATE RENTAL_HISTORY SET SIGN"+step+"=?,SIGN"+step+"_DATE=CURRENT_DATE,STATUS=?,APPROVAL_STEP=? WHERE RENTAL_NO=?",
                name,status,approve?step+1:step,rentalNo);
        if(!approve) jdbc.update("UPDATE EQUIPMENT SET REMAIN_COUNT=REMAIN_COUNT+? WHERE EQ_NO=?",number(rental,"REQ_COUNT"),number(rental,"EQ_NO"));
    }

    @Transactional
    public void returnRental(int rentalNo,int actorNo) {
        Map<String,Object> actor=employee(actorNo);
        Map<String,Object> rental=jdbc.queryForMap("SELECT R.EMP_NO,R.EQ_NO,R.REQ_COUNT,R.STATUS,E.RETIRED FROM RENTAL_HISTORY R JOIN EMPLOYEE E ON E.EMP_NO=R.EMP_NO WHERE R.RENTAL_NO=? FOR UPDATE OF R.STATUS",rentalNo);
        boolean own=number(rental,"EMP_NO")==actorNo;
        boolean retiredProxy="Y".equals(actor.get("MANAGER")) && "Y".equals(rental.get("RETIRED"));
        require(own||retiredProxy,"본인의 대여만 반납할 수 있습니다. 퇴사자는 관리자가 처리합니다.");
        require("대여중".equals(rental.get("STATUS")),"이미 반납되었거나 대여 중인 문서가 아닙니다.");
        jdbc.update("UPDATE RENTAL_HISTORY SET STATUS='반납완료' WHERE RENTAL_NO=?",rentalNo);
        jdbc.update("UPDATE EQUIPMENT SET REMAIN_COUNT=REMAIN_COUNT+? WHERE EQ_NO=?",number(rental,"REQ_COUNT"),number(rental,"EQ_NO"));
    }

    public int businessDays(Date start,Date end) {
        require(start!=null && end!=null && !end.before(start),"휴가 기간을 확인해주세요.");
        int result=0;
        for(LocalDate date=start.toLocalDate(); !date.isAfter(end.toLocalDate()); date=date.plusDays(1)) {
            if(date.getDayOfWeek()!=DayOfWeek.SATURDAY && date.getDayOfWeek()!=DayOfWeek.SUNDAY) result++;
        }
        return result;
    }

    @Transactional
    public boolean createLeave(LeaveHistoryDTO dto) {
        Map<String,Object> actor=employee(dto.getEmpNo());
        int level=number(actor,"EMP_LEVEL"), days=businessDays(dto.getStartDate(),dto.getEndDate());
        require(level>=1 && level<=5,"직급을 확인해주세요.");
        require(days>0 && days<=number(actor,"CUR_LEAVE"),"신청할 수 있는 연차가 부족합니다.");
        if(level==5) deductLeave(dto.getEmpNo(),days);
        Object[] signs=new Object[10];
        signs[(level-1)*2]=actor.get("EMP_NAME"); signs[(level-1)*2+1]=Date.valueOf(LocalDate.now());
        jdbc.update("INSERT INTO LEAVE_HISTORY (LEAVE_NO,EMP_NO,START_DATE,END_DATE,USE_DAYS,REASON,STATUS,APPROVAL_STEP,SIGN1,SIGN1_DATE,SIGN2,SIGN2_DATE,SIGN3,SIGN3_DATE,SIGN4,SIGN4_DATE,SIGN5,SIGN5_DATE) VALUES (SEQ_LEAVE.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                dto.getEmpNo(),dto.getStartDate(),dto.getEndDate(),days,dto.getReason(),level==5?"승인완료":"승인대기",level+1,
                signs[0],signs[1],signs[2],signs[3],signs[4],signs[5],signs[6],signs[7],signs[8],signs[9]);
        return true;
    }
    private void deductLeave(int employeeNo,int days) {
        int changed=jdbc.update("UPDATE EMPLOYEE SET CUR_LEAVE=CUR_LEAVE-? WHERE EMP_NO=? AND CUR_LEAVE>=? AND RETIRED='N'",days,employeeNo,days);
        require(changed==1,"남은 연차가 부족하거나 퇴사한 사원입니다.");
    }

    @Transactional
    public void approveLeave(int leaveNo,int requestedStep,int actorNo,boolean approve) {
        Map<String,Object> actor=employee(actorNo);
        Map<String,Object> leave=jdbc.queryForMap("SELECT EMP_NO,USE_DAYS,STATUS,APPROVAL_STEP FROM LEAVE_HISTORY WHERE LEAVE_NO=? FOR UPDATE",leaveNo);
        int step=number(leave,"APPROVAL_STEP");
        require("승인대기".equals(leave.get("STATUS")),"이미 처리된 문서입니다.");
        require(step>=1 && step<=5 && step==requestedStep && step==number(actor,"EMP_LEVEL"),"현재 결재 단계의 권한이 없습니다.");
        String status=approve?(step==5?"승인완료":"승인대기"):"반려됨";
        if(approve && step==5) deductLeave(number(leave,"EMP_NO"),number(leave,"USE_DAYS"));
        jdbc.update("UPDATE LEAVE_HISTORY SET SIGN"+step+"=?,SIGN"+step+"_DATE=CURRENT_DATE,STATUS=?,APPROVAL_STEP=? WHERE LEAVE_NO=?",
                actor.get("EMP_NAME")+(approve?"":" (差し戻し)"),status,approve?step+1:step,leaveNo);
    }
}
