package com.groupware.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.groupware.dto.RoomDTO;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class RoomDAO {
    private final DataSource dataSource;

    public RoomDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public RoomDTO getRoomDetail(String roomId) {
        RoomDTO dto = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM ROOM WHERE ROOM_ID = ?";

        try {
            conn = dataSource.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, roomId);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    dto = new RoomDTO();
                    dto.setRoomId(rs.getString("ROOM_ID"));
                    dto.setRoomName(rs.getString("ROOM_NAME"));
                    dto.setCapacity(rs.getInt("CAPACITY"));
                    dto.setHasBeam(rs.getString("HAS_BEAM"));
                    dto.setDescription(rs.getString("DESCRIPTION"));
                    dto.setEnable(rs.getString("ENABLE"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dto;
    }
    public boolean updateRoom(RoomDTO dto) {
        boolean result = false;
        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "UPDATE ROOM SET ROOM_NAME=?, CAPACITY=?, HAS_BEAM=?, DESCRIPTION=?, ENABLE=? WHERE ROOM_ID=?";

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dto.getRoomName());
            pstmt.setInt(2, dto.getCapacity());
            pstmt.setString(3, dto.getHasBeam());
            pstmt.setString(4, dto.getDescription());
            pstmt.setString(5, dto.getEnable());
            pstmt.setString(6, dto.getRoomId());

            if (pstmt.executeUpdate() > 0) result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }
}
