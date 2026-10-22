package com.groupware.dto;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private int boardNo;
    private int boardType; // 1: 사내소식, 2: 자유, 3: 건의
    private String title;
    private String content;
    private int empNo;
    private Date regDate;
    private int hit;
    private String empName;
    private int fileCount;
}
