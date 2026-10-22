package com.groupware.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
	private int empNo; // 사번
	private String empPw; // 비밀번호
	private String empName; // 이름
	private int empLevel; // 직급
	private String manager; // 관리자 여부 (Y/N)
	private String dept;
	private int maxLeave;
	private int curLeave;
	
	private String retired; // 퇴사 여부
	private int tempLevel; // 권한 임시 양도
	
	private int count_manager; //총 직원 관리자 count 20260513
	
	private int managerLevel; // 관리자 결재 단계 레벨 (1~5)
	
}
