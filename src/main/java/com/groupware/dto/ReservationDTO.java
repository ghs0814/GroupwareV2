package com.groupware.dto;

import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private int resNo;           // 예약 고유 번호 (PK)
    private int empNo;           // 예약을 시도한 사원 번호 (FK)
    private String roomId;       // 예약된 회의실 ID (FK)
    private Date resDate;        // 예약 날짜
    private String startTime;    // 이용 시작 시간 (예: 09:00)
    private String endTime;      // 이용 종료 시간 (예: 10:00)
    private String purpose;      // 회의 목적 또는 사용 용도
    private String status;       // 현재 예약 상태 (예약완료, 취소됨)
    private java.sql.Timestamp expireTime;
}
