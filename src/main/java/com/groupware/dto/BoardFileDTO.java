package com.groupware.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardFileDTO {
    private int fileNo;
    private int boardNo;
    private String orgName;
    private String savedName;
    private long fileSize;
}
