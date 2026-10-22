package com.groupware.dao;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.groupware.dto.EmployeeDTO;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDAO {
    private final DataSource dataSource;

    public EmployeeDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }



	public EmployeeDTO getEmployeeByNo(String empNo) {
	    EmployeeDTO dto = null;
	    String sql = "SELECT emp_no, emp_name, emp_level, manager, dept, max_leave, cur_leave , retired, emp_pw "
	               + "FROM EMPLOYEE WHERE emp_no = ?";

	    try (Connection conn = dataSource.getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, empNo);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                dto = new EmployeeDTO();
	                dto.setEmpNo(rs.getInt("emp_no"));
	                dto.setEmpName(rs.getString("emp_name"));
	                dto.setEmpLevel(rs.getInt("emp_level"));
	                dto.setManager(rs.getString("manager"));
	                dto.setDept(rs.getString("dept"));
	                dto.setMaxLeave(rs.getInt("max_leave"));
	                dto.setCurLeave(rs.getInt("cur_leave"));
	                dto.setRetired(rs.getString("retired"));
	                dto.setEmpPw(rs.getString("emp_pw"));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return dto;
	}
	public List<EmployeeDTO> getAllEmployees() {

		List<EmployeeDTO> list = new ArrayList<>();
		String sql = "SELECT emp_no, emp_name, emp_level, manager, retired, "
		           + "(SELECT COUNT(*) FROM EMPLOYEE WHERE 1=1 AND MANAGER = 'Y') AS count_manager, dept " // count 뒤에 공백 추가 , 부서 호출 추가
		           + "FROM EMPLOYEE ORDER BY manager DESC, emp_level DESC, emp_no ASC";

		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				EmployeeDTO dto = new EmployeeDTO();
				dto.setEmpNo(rs.getInt("emp_no")); // int 타입 유지
				dto.setEmpName(rs.getString("emp_name"));
				dto.setEmpLevel(rs.getInt("emp_level"));
				dto.setManager(rs.getString("manager"));
				dto.setRetired(rs.getString("retired"));//20260513 퇴사자 데이터 확인
				dto.setCount_manager(rs.getInt("count_manager"));//20260514 위임 카운트 데이터
				dto.setDept(rs.getString("dept"));//20260618 부서 호출
				list.add(dto);
			}
			
			System.out.println("EmployeeDAO.getAllEmployees 전체 사원 목록 조회"+sql);
		} catch (Exception e) {
			e.printStackTrace();
			
			System.out.println("Employee.getAllEmployees 전체 사원 목록 조회 오류"+e);
			
		}
		return list;
	}
	public boolean updateEmployeeLevel(int empNo, int newLevel) {
		boolean result = false;
		String sql = "UPDATE EMPLOYEE SET emp_level = ? WHERE emp_no = ?";

		try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, newLevel);
			pstmt.setInt(2, empNo);

			if (pstmt.executeUpdate() > 0)
				result = true;
			
			System.out.println("updateEmployeeLevel사원 직급 변경"+sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

    public boolean transferManagerRole(int newManagerNo) {
        try (Connection conn=dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement check=conn.prepareStatement("SELECT EMP_NO FROM EMPLOYEE WHERE EMP_NO=? AND RETIRED='N' AND EMP_LEVEL>0 FOR UPDATE")) {
                    check.setInt(1,newManagerNo);
                    try (ResultSet rs=check.executeQuery()) { if(!rs.next()) { conn.rollback(); return false; } }
                }
                try (PreparedStatement clear=conn.prepareStatement("UPDATE EMPLOYEE SET MANAGER='N' WHERE MANAGER='Y' AND EMP_LEVEL<>5")) { clear.executeUpdate(); }
                try (PreparedStatement assign=conn.prepareStatement("UPDATE EMPLOYEE SET MANAGER='Y' WHERE EMP_NO=?")) { assign.setInt(1,newManagerNo);assign.executeUpdate(); }
                conn.commit();return true;
            } catch(Exception exception) { conn.rollback();throw exception; }
        } catch(Exception exception) { exception.printStackTrace();return false; }
    }

	public boolean updatePassword(int empNo, String newPw) {
		boolean result = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "UPDATE EMPLOYEE SET EMP_PW = ? WHERE EMP_NO = ?";

		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, newPw);
				pstmt.setInt(2, empNo);

				int count = pstmt.executeUpdate();
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public boolean deleteEmployee(int empNo) {
		boolean result = false;
		String sql = "UPDATE EMPLOYEE SET EMP_PW = 'RETIRED', EMP_LEVEL = 0, MANAGER = 'N', RETIRED = 'Y' WHERE EMP_NO = ?";

		try (java.sql.Connection conn = dataSource.getConnection();
				java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, empNo);

			int count = pstmt.executeUpdate();
			if (count > 0) {
				result = true;
			}

			System.out.println("EmployeeDAO.deleteEmployee 사원 퇴사 처리"+sql);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}




	public boolean insertEmployee(EmployeeDTO dto) {
		boolean result = false;
		String sql = "INSERT INTO EMPLOYEE (EMP_NO, EMP_NAME, EMP_LEVEL, MANAGER,RETIRED,DEPT) VALUES (?, ?, ?, ?,'N',?)";

		try (java.sql.Connection conn = dataSource.getConnection();
				java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, dto.getEmpNo());
			pstmt.setString(2, dto.getEmpName());
			pstmt.setInt(3, dto.getEmpLevel());
			pstmt.setString(4, dto.getManager());
			pstmt.setString(5, dto.getDept());

			int count = pstmt.executeUpdate();
			if (count > 0) {
				result = true;
			}
			System.out.println("(insertEmployee)신규 사원 사전 등록 비밀번호(EMP_PW)는 비워두고 사번, 이름, 직급, 관리자 여부 세팅 : "+sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean updateManagerStatus(int targetEmpNo, String status) {
	    String sql = "UPDATE EMPLOYEE SET MANAGER = ? WHERE EMP_NO = ?";
	
	    boolean result = false;

	    try (java.sql.Connection conn = dataSource.getConnection();
	         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, status);
	        pstmt.setInt(2, targetEmpNo);

	        int count = pstmt.executeUpdate();
	        if (count > 0) {
	            result = true;
	        }
	        
	        System.out.println("(updateManagerStatus) 관리자 상태 변경 완료 [대상:" + targetEmpNo + ", 상태:" + status + "]");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return result;
	}




	public boolean updateEmployeeDept(int empNo, String newDept) {
		boolean result = false;
		String sql = "UPDATE EMPLOYEE SET DEPT = ? WHERE EMP_NO = ?";

		try (Connection conn = dataSource.getConnection(); 
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, newDept); // 변경할 부서명 (ex: "개발팀")
			pstmt.setInt(2, empNo);      // 사원 번호
			if (pstmt.executeUpdate() > 0) {
				result = true;
			}

			System.out.println("(updateEmployeeDept) 사원 부서 변경 완료 - 사번: " + empNo + ", 부서: " + newDept + " [결과: " + result + "]");
			
		} catch (Exception e) {
			System.err.println("EmployeeDAO.updateEmployeeDept 오류 발생");
			e.printStackTrace();
		}
		return result;
	}
}
