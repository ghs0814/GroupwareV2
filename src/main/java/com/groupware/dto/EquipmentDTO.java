package com.groupware.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDTO {
    private int eqNo;         // 비품 고유 번호
    private String eqName;     // 비품 명칭
    private int totalCount;    // 전체 보유 수량
    private int remainCount;   // 현재 대여 가능한 수량
    private char ischecked;//20260608 데이터 유무 체크
}
