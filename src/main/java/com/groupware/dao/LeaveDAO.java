package com.groupware.dao;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.time.*;
import com.groupware.dto.LeaveHistoryDTO; // 별도 생성 필요
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class LeaveDAO {
    private final DataSource dataSource;

    public LeaveDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	public List<com.groupware.dto.LeaveHistoryDTO> getMyLeaveList(int empNo) {
		List<com.groupware.dto.LeaveHistoryDTO> list = new ArrayList<>();
		String sql = "SELECT * FROM LEAVE_HISTORY WHERE EMP_NO = ? ORDER BY LEAVE_NO DESC";

		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, empNo);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				com.groupware.dto.LeaveHistoryDTO dto = new com.groupware.dto.LeaveHistoryDTO();
				dto.setLeaveNo(rs.getInt("LEAVE_NO"));
				dto.setEmpNo(rs.getInt("EMP_NO"));
				dto.setStartDate(rs.getDate("START_DATE"));
				dto.setEndDate(rs.getDate("END_DATE"));
				dto.setUseDays(rs.getInt("USE_DAYS"));
				dto.setReason(rs.getString("REASON"));
				dto.setStatus(rs.getString("STATUS"));
				dto.setApprovalStep(rs.getInt("APPROVAL_STEP"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	public int calculateWorkingDays(LocalDate start, LocalDate end) {
		int workingDays = 0;
		LocalDate date = start;
		while (!date.isAfter(end)) {
			DayOfWeek day = date.getDayOfWeek();
			if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
				workingDays++;
			}
			date = date.plusDays(1);
		}
		return workingDays;
	}



	public List<com.groupware.dto.LeaveHistoryDTO> getPendingLeaveList(int managerLevel) {
		List<com.groupware.dto.LeaveHistoryDTO> list = new ArrayList<>();
		String sql = "SELECT h.*, e.EMP_NAME, e.DEPT FROM LEAVE_HISTORY h " + "JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO "
				+ "WHERE h.STATUS = '승인대기' AND h.APPROVAL_STEP = ? ORDER BY h.START_DATE ASC";
		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, managerLevel);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				com.groupware.dto.LeaveHistoryDTO dto = new com.groupware.dto.LeaveHistoryDTO();
				dto.setLeaveNo(rs.getInt("LEAVE_NO"));
				dto.setEmpNo(rs.getInt("EMP_NO"));
				dto.setEmpName(rs.getString("EMP_NAME"));
				dto.setDept(rs.getString("DEPT"));
				dto.setStartDate(rs.getDate("START_DATE"));
				dto.setEndDate(rs.getDate("END_DATE"));
				dto.setUseDays(rs.getInt("USE_DAYS"));
				dto.setReason(rs.getString("REASON"));
				dto.setApprovalStep(rs.getInt("APPROVAL_STEP"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	public com.groupware.dto.LeaveHistoryDTO getLeaveDetail(int leaveNo) {
		com.groupware.dto.LeaveHistoryDTO dto = null;
		String sql = "SELECT h.*, e.EMP_NAME, e.EMP_LEVEL, e.DEPT " + "FROM LEAVE_HISTORY h "
				+ "JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO " + "WHERE h.LEAVE_NO = ?";

		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, leaveNo);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				dto = new com.groupware.dto.LeaveHistoryDTO();
				dto.setLeaveNo(rs.getInt("LEAVE_NO"));
				dto.setEmpNo(rs.getInt("EMP_NO"));
				dto.setEmpName(rs.getString("EMP_NAME"));
				dto.setDept(rs.getString("DEPT"));
				dto.setEmpLevel(rs.getInt("EMP_LEVEL"));//06 10 추가 
				dto.setStartDate(rs.getDate("START_DATE"));
				dto.setEndDate(rs.getDate("END_DATE"));
				dto.setUseDays(rs.getInt("USE_DAYS"));
				dto.setReason(rs.getString("REASON"));
				dto.setStatus(rs.getString("STATUS"));
				dto.setApprovalStep(rs.getInt("APPROVAL_STEP"));
				dto.setSign1(rs.getString("SIGN1"));
				dto.setSign1Date(rs.getDate("SIGN1_DATE"));
				dto.setSign2(rs.getString("SIGN2"));
				dto.setSign2Date(rs.getDate("SIGN2_DATE"));
				dto.setSign3(rs.getString("SIGN3"));
				dto.setSign3Date(rs.getDate("SIGN3_DATE"));
				dto.setSign4(rs.getString("SIGN4"));
				dto.setSign4Date(rs.getDate("SIGN4_DATE"));
				dto.setSign5(rs.getString("SIGN5"));
				dto.setSign5Date(rs.getDate("SIGN5_DATE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}
	public List<com.groupware.dto.LeaveHistoryDTO> getAllLeaveDocuments() {
		List<com.groupware.dto.LeaveHistoryDTO> list = new java.util.ArrayList<>();
		String sql = "SELECT h.*, e.EMP_NAME, e.EMP_LEVEL, e.DEPT FROM LEAVE_HISTORY h "
				+ "JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO ORDER BY h.LEAVE_NO DESC";
		try (java.sql.Connection conn = dataSource.getConnection();
				java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
				java.sql.ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				com.groupware.dto.LeaveHistoryDTO dto = new com.groupware.dto.LeaveHistoryDTO();
				dto.setLeaveNo(rs.getInt("LEAVE_NO"));
				dto.setEmpNo(rs.getInt("EMP_NO"));
				dto.setEmpName(rs.getString("EMP_NAME"));
				dto.setDept(rs.getString("DEPT"));
				dto.setStartDate(rs.getDate("START_DATE"));
				dto.setEndDate(rs.getDate("END_DATE"));
				dto.setUseDays(rs.getInt("USE_DAYS"));
				dto.setStatus(rs.getString("STATUS"));
				dto.setApprovalStep(rs.getInt("APPROVAL_STEP"));
				dto.setReason(rs.getString("REASON"));
				dto.setEmpLevel(rs.getInt("EMP_LEVEL")); // ★ 추가: 직급 매핑
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public int getTotalLeaveCount() {
		int count = 0;
		String sql = "SELECT COUNT(*) FROM LEAVE_HISTORY";
		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	public List<com.groupware.dto.LeaveHistoryDTO> getAllLeaveDocumentsPaging(int startRow, int endRow) {
		List<com.groupware.dto.LeaveHistoryDTO> list = new ArrayList<>();
		String sql = "SELECT * FROM ("
				   + "  SELECT ROWNUM AS RN, A.* FROM ("
				   + "    SELECT h.*, e.EMP_NAME, e.EMP_LEVEL, e.DEPT FROM LEAVE_HISTORY h "
				   + "    JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO ORDER BY h.LEAVE_NO DESC"
				   + "  ) A"
				   + ") WHERE RN BETWEEN ? AND ?";
		try (Connection conn = dataSource.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, startRow);
			pstmt.setInt(2, endRow);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					com.groupware.dto.LeaveHistoryDTO dto = new com.groupware.dto.LeaveHistoryDTO();
					dto.setLeaveNo(rs.getInt("LEAVE_NO"));
					dto.setEmpNo(rs.getInt("EMP_NO"));
					dto.setEmpName(rs.getString("EMP_NAME"));
					dto.setDept(rs.getString("DEPT"));
					dto.setStartDate(rs.getDate("START_DATE"));
					dto.setEndDate(rs.getDate("END_DATE"));
					dto.setUseDays(rs.getInt("USE_DAYS"));
					dto.setStatus(rs.getString("STATUS"));
					dto.setApprovalStep(rs.getInt("APPROVAL_STEP"));
					dto.setReason(rs.getString("REASON"));
					dto.setEmpLevel(rs.getInt("EMP_LEVEL"));
					list.add(dto);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}




	private void closeResource(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		} catch (Exception e) {
		}
	}
	public int getEmpLevelByNo(int empNo) {
	    int level = 0;
	    String sql = "SELECT EMP_LEVEL FROM EMPLOYEE WHERE EMP_NO = ?";

	    try (Connection conn = dataSource.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, empNo);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                level = rs.getInt("EMP_LEVEL");
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return level;
	}

}
