package com.groupware.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.groupware.dto.RentalHistoryDTO;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class RentalDAO {
    private final DataSource dataSource;

    public RentalDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }




    public List<RentalHistoryDTO> getAllDocumentList() {
        List<RentalHistoryDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT h.RENTAL_NO, h.TITLE, h.REASON, h.EMP_NO, h.EQ_NO, h.RENTAL_DATE, h.RETURN_DATE, "
                + "CASE WHEN h.STATUS = '대여중' AND h.RETURN_DATE < TRUNC(CURRENT_DATE) THEN '미반납' ELSE h.STATUS END AS STATUS, "
                + "h.APPROVAL_STEP, h.SIGN1, h.SIGN1_DATE, h.SIGN2, h.SIGN2_DATE, h.SIGN3, h.SIGN3_DATE, "
                + "h.SIGN4, h.SIGN4_DATE, h.SIGN5, h.SIGN5_DATE, h.REQ_COUNT, "
                + "e.EMP_NAME, e.EMP_LEVEL, eq.EQ_NAME, eq.TOTAL_COUNT, eq.REMAIN_COUNT "
                + "FROM RENTAL_HISTORY h "
                + "LEFT JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO "
                + "LEFT JOIN EQUIPMENT eq ON h.EQ_NO = eq.EQ_NO "
                + "ORDER BY h.RENTAL_NO DESC";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    RentalHistoryDTO dto = mapResultSetToDTO(rs, true);
                    list.add(dto);
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return list;
    }

    public int getTotalRentalCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM RENTAL_HISTORY";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, rs);
        }
        return count;
    }

    public List<RentalHistoryDTO> getAllDocumentListPaging(int startRow, int endRow) {
        List<RentalHistoryDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM ("
                   + "  SELECT ROWNUM AS RN, A.* FROM ("
                   + "    SELECT h.RENTAL_NO, h.TITLE, h.REASON, h.EMP_NO, h.EQ_NO, h.RENTAL_DATE, h.RETURN_DATE, "
                   + "    CASE WHEN h.STATUS = '대여중' AND h.RETURN_DATE < TRUNC(CURRENT_DATE) THEN '미반납' ELSE h.STATUS END AS STATUS, "
                   + "    h.APPROVAL_STEP, h.SIGN1, h.SIGN1_DATE, h.SIGN2, h.SIGN2_DATE, h.SIGN3, h.SIGN3_DATE, "
                   + "    h.SIGN4, h.SIGN4_DATE, h.SIGN5, h.SIGN5_DATE, h.REQ_COUNT, "
                   + "    e.EMP_NAME, e.EMP_LEVEL, eq.EQ_NAME, eq.TOTAL_COUNT, eq.REMAIN_COUNT "
                   + "    FROM RENTAL_HISTORY h "
                   + "    LEFT JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO "
                   + "    LEFT JOIN EQUIPMENT eq ON h.EQ_NO = eq.EQ_NO "
                   + "    ORDER BY h.RENTAL_NO DESC"
                   + "  ) A"
                   + ") WHERE RN BETWEEN ? AND ?";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, startRow);
                pstmt.setInt(2, endRow);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    RentalHistoryDTO dto = mapResultSetToDTO(rs, true);
                    list.add(dto);
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return list;
    }

    public RentalHistoryDTO getDocumentDetail(int rentalNo) {
        RentalHistoryDTO dto = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT h.RENTAL_NO, h.TITLE, h.REASON, h.EMP_NO, h.EQ_NO, h.RENTAL_DATE, h.RETURN_DATE, "
                + "CASE WHEN h.STATUS = '대여중' AND h.RETURN_DATE < TRUNC(CURRENT_DATE) THEN '미반납' ELSE h.STATUS END AS STATUS, "
                + "h.APPROVAL_STEP, h.SIGN1, h.SIGN1_DATE, h.SIGN2, h.SIGN2_DATE, h.SIGN3, h.SIGN3_DATE, "
                + "h.SIGN4, h.SIGN4_DATE, h.SIGN5, h.SIGN5_DATE, h.REQ_COUNT, "
                + "e.EMP_NAME, e.EMP_LEVEL, eq.EQ_NAME, eq.TOTAL_COUNT, eq.REMAIN_COUNT "
                + "FROM RENTAL_HISTORY h "
                + "LEFT JOIN EMPLOYEE e ON h.EMP_NO = e.EMP_NO "
                + "LEFT JOIN EQUIPMENT eq ON h.EQ_NO = eq.EQ_NO "
                + "WHERE h.RENTAL_NO = ?";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, rentalNo);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    dto = mapResultSetToDTO(rs, true);
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return dto;
    }
    public RentalHistoryDTO getRentalDetail(int rentalNo) {
        return getDocumentDetail(rentalNo);
    }




    public List<RentalHistoryDTO> getPendingList(int managerLevel) {
        List<RentalHistoryDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM RENTAL_HISTORY WHERE STATUS = '승인대기' AND APPROVAL_STEP = ? ORDER BY RENTAL_DATE ASC";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, managerLevel);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs, false));
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return list;
    }

    public List<RentalHistoryDTO> getMyRentalList(int empNo) {
        List<RentalHistoryDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT h.RENTAL_NO, h.TITLE, h.REASON, h.EMP_NO, h.EQ_NO, h.RENTAL_DATE, h.RETURN_DATE, "
                + "CASE WHEN h.STATUS = '대여중' AND h.RETURN_DATE < TRUNC(CURRENT_DATE) THEN '미반납' ELSE h.STATUS END AS STATUS, "
                + "h.APPROVAL_STEP, h.SIGN1, h.SIGN1_DATE, h.SIGN2, h.SIGN2_DATE, h.SIGN3, h.SIGN3_DATE, "
                + "h.SIGN4, h.SIGN4_DATE, h.SIGN5, h.SIGN5_DATE, h.REQ_COUNT, e.EQ_NAME, emp.EMP_LEVEL "
                + "FROM RENTAL_HISTORY h "
                + "JOIN EQUIPMENT e ON h.EQ_NO = e.EQ_NO "
                + "LEFT JOIN EMPLOYEE emp ON h.EMP_NO = emp.EMP_NO "
                + "WHERE h.EMP_NO = ? ORDER BY h.RENTAL_NO DESC";
        
        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, empNo);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs, true));
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return list;
    }




    private RentalHistoryDTO mapResultSetToDTO(ResultSet rs, boolean includeJoinFields) throws Exception {
        RentalHistoryDTO dto = new RentalHistoryDTO();
        dto.setRentalNo(rs.getInt("RENTAL_NO"));
        dto.setEmpNo(rs.getInt("EMP_NO"));
        dto.setEqNo(rs.getInt("EQ_NO"));
        dto.setRentalDate(rs.getDate("RENTAL_DATE"));
        dto.setReturnDate(rs.getDate("RETURN_DATE"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setApprovalStep(rs.getInt("APPROVAL_STEP"));
        dto.setSign1(rs.getString("SIGN1"));
        dto.setSign2(rs.getString("SIGN2"));
        dto.setSign3(rs.getString("SIGN3"));
        dto.setSign4(rs.getString("SIGN4"));
        dto.setSign5(rs.getString("SIGN5"));
        dto.setTitle(rs.getString("TITLE"));
        dto.setContent(rs.getString("REASON"));
        dto.setSign1Date(rs.getDate("SIGN1_DATE"));
        dto.setSign2Date(rs.getDate("SIGN2_DATE"));
        dto.setSign3Date(rs.getDate("SIGN3_DATE"));
        dto.setSign4Date(rs.getDate("SIGN4_DATE"));
        dto.setSign5Date(rs.getDate("SIGN5_DATE"));
        dto.setReqCount(rs.getInt("REQ_COUNT")); 

        if (includeJoinFields) {
            try { dto.setEmpName(rs.getString("EMP_NAME")); } catch (Exception e) {}
            try { dto.setEmpLevel(rs.getInt("EMP_LEVEL")); } catch (Exception e) {}
            try { dto.setEqName(rs.getString("EQ_NAME")); } catch (Exception e) {}
            try { dto.setTotalCount(rs.getInt("TOTAL_COUNT")); } catch (Exception e) {}
            try { dto.setRemainCount(rs.getInt("REMAIN_COUNT")); } catch (Exception e) {}
        } else {
            try { dto.setEqName(rs.getString("EQ_NAME")); } catch (Exception e) {}
            try { dto.setEmpLevel(rs.getInt("EMP_LEVEL")); } catch (Exception e) {}
        }
        return dto;
    }

    private void closeResource(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
