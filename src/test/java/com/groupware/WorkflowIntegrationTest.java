package com.groupware;

import com.groupware.dao.*;
import com.groupware.dto.*;
import com.groupware.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** 실제 Oracle에서 원자성·중복 요청·동시 요청을 검증합니다. 테스트가 만든 행만 정리합니다. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_ORACLE_TESTS",matches="true")
class WorkflowIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired WorkflowService workflow;
    @Autowired ReservationService reservations;
    @Autowired BoardDAO boards;
    @Autowired CommentDAO comments;
    private int base;
    private String room;

    @BeforeEach
    void fixtures() {
        base=1_000_000+new Random().nextInt(1_000_000);
        room="TEST_"+base;
        for(int level=1;level<=5;level++) jdbc.update("INSERT INTO EMPLOYEE (EMP_NO,EMP_NAME,EMP_LEVEL,MANAGER,DEPT,MAX_LEAVE,CUR_LEAVE) VALUES (?,?,?,'N','BOOT_TEST',15,15)",base+level,"BOOT_TEST_"+level,level);
        jdbc.update("INSERT INTO EQUIPMENT (EQ_NO,EQ_NAME,TOTAL_COUNT,REMAIN_COUNT) VALUES (?,'BOOT_TEST',5,5)",base);
        jdbc.update("INSERT INTO ROOM (ROOM_ID,ROOM_NAME,CAPACITY,ENABLE) VALUES (?,'BOOT_TEST',5,'Y')",room);
    }
    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM BOARD_COMMENT WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
        jdbc.update("DELETE FROM BOARD WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
        jdbc.update("DELETE FROM RESERVATION WHERE ROOM_ID=?",room);
        jdbc.update("DELETE FROM RENTAL_HISTORY WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
        jdbc.update("DELETE FROM LEAVE_HISTORY WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
        jdbc.update("DELETE FROM RENTAL_HISTORY_BACKUP WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
        jdbc.update("DELETE FROM LEAVE_HISTORY_BACKUP WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
        jdbc.update("DELETE FROM EQUIPMENT WHERE EQ_NO=?",base);
        jdbc.update("DELETE FROM ROOM WHERE ROOM_ID=?",room);
        jdbc.update("DELETE FROM EMPLOYEE WHERE EMP_NO BETWEEN ? AND ?",base+1,base+5);
    }
    private int scalar(String sql,Object...args) { return jdbc.queryForObject(sql,Integer.class,args); }
    private int stock() { return scalar("SELECT REMAIN_COUNT FROM EQUIPMENT WHERE EQ_NO=?",base); }
    private int rentalNo() { return scalar("SELECT MAX(RENTAL_NO) FROM RENTAL_HISTORY WHERE EMP_NO=?",base+1); }
    private int leaveNo() { return scalar("SELECT MAX(LEAVE_NO) FROM LEAVE_HISTORY WHERE EMP_NO=?",base+1); }
    private RentalHistoryDTO rental(int count) {
        RentalHistoryDTO dto=new RentalHistoryDTO();dto.setEmpNo(base+1);dto.setEqNo(base);dto.setReqCount(count);
        dto.setTitle("BOOT_TEST 대여 日本語");dto.setContent("테스트 사유");
        dto.setRentalDate(Date.valueOf(LocalDate.now()));dto.setReturnDate(Date.valueOf(LocalDate.now().plusDays(7)));return dto;
    }
    private LeaveHistoryDTO leave(int employeeLevel) {
        LocalDate monday=LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
        LeaveHistoryDTO dto=new LeaveHistoryDTO();dto.setEmpNo(base+employeeLevel);dto.setStartDate(Date.valueOf(monday));
        dto.setEndDate(Date.valueOf(monday.plusDays(1)));dto.setReason("BOOT_TEST 휴가");return dto;
    }
    private ReservationDTO reservation() {
        ReservationDTO dto=new ReservationDTO();dto.setEmpNo(base+1);dto.setRoomId(room);
        dto.setResDate(Date.valueOf(LocalDate.now().plusDays(1)));dto.setStartTime("09:00");dto.setEndTime("10:00");return dto;
    }

    @Test
    void rentalApprovalAndReturnChangeStockOnlyOnce() {
        workflow.createRental(rental(2));int number=rentalNo();assertEquals(3,stock());
        assertThrows(IllegalArgumentException.class,()->workflow.approveRental(number,base,2,base+1,true));
        for(int step=2;step<=5;step++) workflow.approveRental(number,base,step,base+step,true);
        assertThrows(IllegalArgumentException.class,()->workflow.approveRental(number,base,5,base+5,true));
        assertThrows(IllegalArgumentException.class,()->workflow.returnRental(number,base+2));
        workflow.returnRental(number,base+1);assertEquals(5,stock());
        assertThrows(IllegalArgumentException.class,()->workflow.returnRental(number,base+1));assertEquals(5,stock());
        assertTrue(scalar("SELECT COUNT(*) FROM RENTAL_HISTORY_BACKUP WHERE RENTAL_NO=?",number)>=6);
    }
    @Test
    void rejectionRestoresStockOnceAndInvalidRequestsRollBack() {
        workflow.createRental(rental(3));int number=rentalNo();assertEquals(2,stock());
        workflow.approveRental(number,base,2,base+2,false);assertEquals(5,stock());
        assertThrows(IllegalArgumentException.class,()->workflow.approveRental(number,base,2,base+2,false));
        assertThrows(IllegalArgumentException.class,()->workflow.createRental(rental(0)));
        assertThrows(IllegalArgumentException.class,()->workflow.createRental(rental(6)));assertEquals(5,stock());
        assertEquals(1,scalar("SELECT COUNT(*) FROM RENTAL_HISTORY WHERE EMP_NO=?",base+1));
    }
    private int concurrently(int count,Callable<Boolean> task) throws Exception {
        ExecutorService executor=Executors.newFixedThreadPool(count);
        CountDownLatch start=new CountDownLatch(1);List<Future<Boolean>> results=new ArrayList<>();
        try {
            for(int i=0;i<count;i++) results.add(executor.submit(()->{ start.await(); return task.call(); }));
            start.countDown();int successes=0;
            for(Future<Boolean> result:results) if(result.get(30,TimeUnit.SECONDS)) successes++;
            return successes;
        } finally { executor.shutdownNow(); }
    }
    @Test
    void simultaneousRentalsCannotExceedStock() throws Exception {
        jdbc.update("UPDATE EQUIPMENT SET TOTAL_COUNT=3,REMAIN_COUNT=3 WHERE EQ_NO=?",base);
        int successes=concurrently(8,()->{
            try { return workflow.createRental(rental(1)); } catch(IllegalArgumentException expected) { return false; }
        });
        assertEquals(3,successes);assertEquals(0,stock());
        assertEquals(3,scalar("SELECT COUNT(*) FROM RENTAL_HISTORY WHERE EMP_NO=?",base+1));
    }
    @Test
    void leaveDeductionOccursAtFinalApprovalOnlyOnce() {
        workflow.createLeave(leave(1));int number=leaveNo();
        assertEquals(15,scalar("SELECT CUR_LEAVE FROM EMPLOYEE WHERE EMP_NO=?",base+1));
        for(int step=2;step<=5;step++) workflow.approveLeave(number,step,base+step,true);
        assertEquals(13,scalar("SELECT CUR_LEAVE FROM EMPLOYEE WHERE EMP_NO=?",base+1));
        assertThrows(IllegalArgumentException.class,()->workflow.approveLeave(number,5,base+5,true));
        assertEquals(13,scalar("SELECT CUR_LEAVE FROM EMPLOYEE WHERE EMP_NO=?",base+1));
        workflow.createLeave(leave(5));assertEquals(13,scalar("SELECT CUR_LEAVE FROM EMPLOYEE WHERE EMP_NO=?",base+5));
        assertEquals(5,workflow.businessDays(Date.valueOf("2026-10-19"),Date.valueOf("2026-10-25")));
    }
    @Test
    void insufficientLeaveRollsBackTheFinalSignature() {
        workflow.createLeave(leave(1));int number=leaveNo();
        for(int step=2;step<=4;step++) workflow.approveLeave(number,step,base+step,true);
        jdbc.update("UPDATE EMPLOYEE SET CUR_LEAVE=0 WHERE EMP_NO=?",base+1);
        assertThrows(IllegalArgumentException.class,()->workflow.approveLeave(number,5,base+5,true));
        assertEquals("승인대기",jdbc.queryForObject("SELECT STATUS FROM LEAVE_HISTORY WHERE LEAVE_NO=?",String.class,number));
        assertNull(jdbc.queryForObject("SELECT SIGN5 FROM LEAVE_HISTORY WHERE LEAVE_NO=?",String.class,number));
    }
    @Test
    void simultaneousReservationsHaveOneWinnerAndCheckOwnership() throws Exception {
        assertEquals(1,concurrently(8,()->reservations.hold(reservation())>0));
        int number=scalar("SELECT MAX(RES_NO) FROM RESERVATION WHERE ROOM_ID=?",room);
        assertFalse(reservations.confirm(number,"wrong owner",base+2));
        assertTrue(reservations.confirm(number,"회의 日本語",base+1));
        assertFalse(reservations.confirm(number,"repeat",base+1));
        assertFalse(reservations.cancel(number,base+2));assertTrue(reservations.cancel(number,base+1));
        assertTrue(reservations.hold(reservation())>0);
    }
    @Test
    void expiredHoldsCannotBeConfirmedAndDisabledRoomsCannotBeHeld() {
        int number=reservations.hold(reservation());assertTrue(number>0);
        jdbc.update("UPDATE RESERVATION SET EXPIRE_TIME=SYSTIMESTAMP-INTERVAL '1' MINUTE WHERE RES_NO=?",number);
        assertFalse(reservations.confirm(number,"expired",base+1));
        assertTrue(reservations.hold(reservation())>0);
        jdbc.update("UPDATE ROOM SET ENABLE='N' WHERE ROOM_ID=?",room);
        assertEquals(-1,reservations.hold(reservation()));
    }
    @Test
    void originalLastTimeslotRemainsAvailable() {
        ReservationDTO late=reservation();late.setStartTime("18:00");late.setEndTime("19:00");
        assertTrue(reservations.hold(late)>0);
        late.setStartTime("19:00");late.setEndTime("20:00");assertEquals(-1,reservations.hold(late));
    }
    @Test
    void boardAttachmentsAndNestedCommentsUseOracleSequences() {
        BoardDTO board=new BoardDTO();board.setEmpNo(base+1);board.setBoardType(2);board.setTitle("BOOT_TEST 한글 日本語");board.setContent("긴 본문\n두 번째 줄");
        BoardFileDTO file=new BoardFileDTO();file.setOrgName("테스트.txt");file.setSavedName("test-file.txt");file.setFileSize(10);
        assertTrue(boards.insertBoard(board,List.of(file)));
        int boardNo=scalar("SELECT MAX(BOARD_NO) FROM BOARD WHERE EMP_NO=?",base+1);
        assertEquals(1,boards.getFilesByBoardNo(boardNo).size());
        CommentDTO comment=new CommentDTO();comment.setBoardNo(boardNo);comment.setEmpNo(base+1);comment.setContent("부모 댓글");
        assertTrue(comments.insertComment(comment));
        CommentDTO parent=comments.getCommentsByBoardNo(boardNo).get(0);
        CommentDTO reply=new CommentDTO();reply.setBoardNo(boardNo);reply.setEmpNo(base+2);reply.setContent("답글");reply.setParentNo(parent.getCommentNo());
        assertTrue(comments.insertComment(reply));assertEquals(2,comments.getCommentsByBoardNo(boardNo).size());
        assertEquals("BOOT_TEST 한글 日本語",boards.getBoardDetail(boardNo).getTitle());
        assertTrue(boards.deleteBoard(boardNo));assertEquals(0,comments.getCommentsByBoardNo(boardNo).size());
    }
}
