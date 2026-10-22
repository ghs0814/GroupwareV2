package com.groupware.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.groupware.dto.ReservationDTO;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationDAO {
    private final DataSource dataSource;

    public ReservationDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cleanUpGhostReservations() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM RESERVATION WHERE STATUS = '임시선점' AND EXPIRE_TIME < LOCALTIMESTAMP";
        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, null); }
    }



    public boolean checkDuplicate(String roomId, java.sql.Date date, String reqStart, String reqEnd) {
        boolean isDuplicate = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String sql = "SELECT COUNT(*) FROM RESERVATION "
                   + "WHERE ROOM_ID = ? AND RES_DATE = ? AND STATUS IN ('예약완료', '임시선점') "
                   + "AND START_TIME < ? AND END_TIME > ?";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, roomId);
                pstmt.setDate(2, date);
                pstmt.setString(3, reqEnd);   
                pstmt.setString(4, reqStart); 

                rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1) > 0) isDuplicate = true; 
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return isDuplicate;
    }
    public List<ReservationDTO> getMyReservations(int empNo) {
        cleanUpGhostReservations(); 
        
        List<ReservationDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM RESERVATION WHERE EMP_NO = ? AND STATUS != '임시선점' ORDER BY RES_DATE DESC, START_TIME DESC";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, empNo);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    ReservationDTO dto = new ReservationDTO();
                    dto.setResNo(rs.getInt("RES_NO"));
                    dto.setEmpNo(rs.getInt("EMP_NO"));
                    dto.setRoomId(rs.getString("ROOM_ID"));
                    dto.setResDate(rs.getDate("RES_DATE"));
                    dto.setStartTime(rs.getString("START_TIME"));
                    dto.setEndTime(rs.getString("END_TIME"));
                    dto.setPurpose(rs.getString("PURPOSE"));
                    dto.setStatus(rs.getString("STATUS"));
                    list.add(dto);
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return list;
    }



    public List<String> getReservedTimes(String roomId, String resDate) {
        cleanUpGhostReservations(); 
        
        List<String> reservedTimes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT START_TIME, END_TIME FROM RESERVATION WHERE ROOM_ID = ? AND RES_DATE = ? AND STATUS IN ('예약완료', '임시선점')";

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roomId);
            pstmt.setDate(2, java.sql.Date.valueOf(resDate));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String start = rs.getString("START_TIME");
                String end = rs.getString("END_TIME");
                
                int startHour = Integer.parseInt(start.split(":")[0]);
                int endHour = Integer.parseInt(end.split(":")[0]);
                
                for (int i = startHour; i < endHour; i++) {
                    String timeStr = (i < 10 ? "0" + i : i) + ":00";
                    if (!reservedTimes.contains(timeStr)) {
                        reservedTimes.add(timeStr);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { closeResource(conn, pstmt, rs); }
        return reservedTimes;
    }

    private void closeResource(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
