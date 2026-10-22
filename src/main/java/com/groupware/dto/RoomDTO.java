package com.groupware.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private String roomId;      // 회의실 코드 (예: 401, Meeting 등)
    private String roomName;    // 회의실 명칭
    private int capacity;       // 최대 수용 인원
    private String hasBeam;     // 빔 프로젝터 등 기자재 유무 (Y/N)
    private String description; // 해당 회의실에 대한 추가 설명
    
    private String enable; // 방 예약 가능 여부
}
