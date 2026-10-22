package com.groupware.dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private int commentNo;
    private int boardNo;
    private int empNo;
    private String content;
    private Timestamp regDate;
    private Integer parentNo; // Nullable
    private int groupNo;
    private int orderNo;
    private int depth;
    private String empName;
}
