package com.groupware.service;

import com.groupware.dto.ReservationDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.time.*;
import java.util.List;

@Service
public class ReservationService {
    private final JdbcTemplate jdbc;
    public ReservationService(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    @Transactional
    public int hold(ReservationDTO dto) {
        Date date=(Date)dto.getResDate();
        LocalTime start=LocalTime.parse(dto.getStartTime()),end=LocalTime.parse(dto.getEndTime());
        if(!start.isBefore(end) || !LocalDateTime.of(date.toLocalDate(),start).isAfter(LocalDateTime.now())) return -1;
        if(start.getMinute()!=0 || end.getMinute()!=0 || start.getHour()<9 || end.getHour()>19) return -1;
        // 방의 행을 잠근 뒤 확인과 저장을 묶습니다. 동시에 들어온 다른 요청도 DB에서 대기합니다.
        List<String> rooms=jdbc.query("SELECT ENABLE FROM ROOM WHERE ROOM_ID=? FOR UPDATE",(rs,row)->rs.getString(1),dto.getRoomId());
        if(rooms.isEmpty() || !"Y".equals(rooms.get(0))) return -1;
        jdbc.update("DELETE FROM RESERVATION WHERE ROOM_ID=? AND STATUS='임시선점' AND EXPIRE_TIME<LOCALTIMESTAMP",dto.getRoomId());
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM RESERVATION WHERE ROOM_ID=? AND RES_DATE=? AND STATUS IN ('예약완료','임시선점') AND START_TIME<? AND END_TIME>?",
                Integer.class,dto.getRoomId(),date,dto.getEndTime(),dto.getStartTime());
        if(count!=null && count>0) return -1;
        Integer number=jdbc.queryForObject("SELECT SEQ_RESERVATION.NEXTVAL FROM DUAL",Integer.class);
        jdbc.update("INSERT INTO RESERVATION (RES_NO,EMP_NO,ROOM_ID,RES_DATE,START_TIME,END_TIME,STATUS,EXPIRE_TIME) VALUES (?,?,?,?,?,?,'임시선점',LOCALTIMESTAMP+INTERVAL '5' MINUTE)",
                number,dto.getEmpNo(),dto.getRoomId(),date,dto.getStartTime(),dto.getEndTime());
        return number;
    }

    public boolean confirm(int number,String purpose,int employeeNo) {
        return jdbc.update("UPDATE RESERVATION SET STATUS='예약완료',PURPOSE=?,EXPIRE_TIME=NULL WHERE RES_NO=? AND EMP_NO=? AND STATUS='임시선점' AND EXPIRE_TIME>=LOCALTIMESTAMP",
                purpose,number,employeeNo)==1;
    }
    public boolean cancel(int number,int employeeNo) {
        return jdbc.update("UPDATE RESERVATION SET STATUS='취소됨' WHERE RES_NO=? AND EMP_NO=? AND STATUS IN ('예약완료','임시선점')",number,employeeNo)==1;
    }
}
