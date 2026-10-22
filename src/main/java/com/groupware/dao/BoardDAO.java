package com.groupware.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.groupware.dto.BoardDTO;
import com.groupware.dto.BoardFileDTO;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class BoardDAO {
    private final DataSource dataSource;

    public BoardDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean insertBoard(BoardDTO board, List<BoardFileDTO> files) {
        boolean result = false;
        Connection conn = null;
        PreparedStatement pstmtBoard = null;
        PreparedStatement pstmtFile = null;
        ResultSet rs = null;

        String sqlBoard = "INSERT INTO BOARD (BOARD_NO, BOARD_TYPE, TITLE, CONTENT, EMP_NO) VALUES (SEQ_BOARD.NEXTVAL, ?, ?, ?, ?)";
        String sqlFile = "INSERT INTO BOARD_FILE (FILE_NO, BOARD_NO, ORG_NAME, SAVED_NAME, FILE_SIZE) VALUES (SEQ_BOARD_FILE.NEXTVAL, ?, ?, ?, ?)";
        
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            String[] generatedColumns = {"BOARD_NO"};
            pstmtBoard = conn.prepareStatement(sqlBoard, generatedColumns);
            pstmtBoard.setInt(1, board.getBoardType());
            pstmtBoard.setString(2, board.getTitle());
            pstmtBoard.setString(3, board.getContent());
            pstmtBoard.setInt(4, board.getEmpNo());
            
            int affectedRows = pstmtBoard.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmtBoard.getGeneratedKeys();
                if (rs.next()) {
                    int boardNo = rs.getInt(1);
                    
                    if (files != null && !files.isEmpty()) {
                        pstmtFile = conn.prepareStatement(sqlFile);
                        for (BoardFileDTO file : files) {
                            pstmtFile.setInt(1, boardNo);
                            pstmtFile.setString(2, file.getOrgName());
                            pstmtFile.setString(3, file.getSavedName());
                            pstmtFile.setLong(4, file.getFileSize());
                            pstmtFile.addBatch();
                        }
                        pstmtFile.executeBatch();
                    }
                    conn.commit();
                    result = true;
                }
            }
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmtBoard, rs);
            if (pstmtFile != null) try { pstmtFile.close(); } catch (SQLException e) {}
        }
        return result;
    }
    public List<BoardDTO> getBoardList(int type, int page, int pageSize, String searchType, String keyword) {
        List<BoardDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        int start = (page - 1) * pageSize + 1;
        int end = page * pageSize;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("  SELECT ROWNUM AS RN, A.* FROM ( ");
        sql.append("    SELECT B.*, E.EMP_NAME, (SELECT COUNT(*) FROM BOARD_FILE WHERE BOARD_NO = B.BOARD_NO) AS FILE_COUNT ");
        sql.append("    FROM BOARD B JOIN EMPLOYEE E ON B.EMP_NO = E.EMP_NO ");
        sql.append("    WHERE B.BOARD_TYPE = ? ");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("title".equals(searchType)) sql.append("AND B.TITLE LIKE ? ");
            else if ("content".equals(searchType)) sql.append("AND B.CONTENT LIKE ? ");
            else if ("author".equals(searchType)) sql.append("AND E.EMP_NAME LIKE ? ");
        }
        
        sql.append("    ORDER BY B.BOARD_NO DESC ");
        sql.append("  ) A ");
        sql.append(") WHERE RN BETWEEN ? AND ?");

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, type);
            
            int idx = 2;
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(idx++, "%" + keyword.trim() + "%");
            }
            pstmt.setInt(idx++, start);
            pstmt.setInt(idx++, end);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                BoardDTO dto = new BoardDTO();
                dto.setBoardNo(rs.getInt("BOARD_NO"));
                dto.setBoardType(rs.getInt("BOARD_TYPE"));
                dto.setTitle(rs.getString("TITLE"));
                dto.setEmpNo(rs.getInt("EMP_NO"));
                dto.setRegDate(rs.getDate("REG_DATE"));
                dto.setHit(rs.getInt("HIT"));
                dto.setEmpName(rs.getString("EMP_NAME"));
                dto.setFileCount(rs.getInt("FILE_COUNT"));
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, rs);
        }
        return list;
    }
    public int getTotalCount(int type, String searchType, String keyword) {
        int total = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM BOARD B JOIN EMPLOYEE E ON B.EMP_NO = E.EMP_NO WHERE B.BOARD_TYPE = ? ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("title".equals(searchType)) sql.append("AND B.TITLE LIKE ? ");
            else if ("content".equals(searchType)) sql.append("AND B.CONTENT LIKE ? ");
            else if ("author".equals(searchType)) sql.append("AND E.EMP_NAME LIKE ? ");
        }

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, type);
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(2, "%" + keyword.trim() + "%");
            }
            rs = pstmt.executeQuery();
            if (rs.next()) total = rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, rs);
        }
        return total;
    }
    public BoardDTO getBoardDetail(int boardNo) {
        BoardDTO dto = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT B.*, E.EMP_NAME FROM BOARD B JOIN EMPLOYEE E ON B.EMP_NO = E.EMP_NO WHERE B.BOARD_NO = ?";

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dto = new BoardDTO();
                dto.setBoardNo(rs.getInt("BOARD_NO"));
                dto.setBoardType(rs.getInt("BOARD_TYPE"));
                dto.setTitle(rs.getString("TITLE"));
                dto.setContent(rs.getString("CONTENT"));
                dto.setEmpNo(rs.getInt("EMP_NO"));
                dto.setRegDate(rs.getDate("REG_DATE"));
                dto.setHit(rs.getInt("HIT"));
                dto.setEmpName(rs.getString("EMP_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, rs);
        }
        return dto;
    }
    public void increaseHit(int boardNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE BOARD SET HIT = HIT + 1 WHERE BOARD_NO = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNo);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, null);
        }
    }
    public List<BoardFileDTO> getFilesByBoardNo(int boardNo) {
        List<BoardFileDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM BOARD_FILE WHERE BOARD_NO = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNo);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BoardFileDTO file = new BoardFileDTO();
                file.setFileNo(rs.getInt("FILE_NO"));
                file.setBoardNo(rs.getInt("BOARD_NO"));
                file.setOrgName(rs.getString("ORG_NAME"));
                file.setSavedName(rs.getString("SAVED_NAME"));
                file.setFileSize(rs.getLong("FILE_SIZE"));
                list.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, rs);
        }
        return list;
    }
    public boolean updateBoard(BoardDTO board, List<BoardFileDTO> files, List<Integer> delFiles) {
        boolean result = false;
        Connection conn = null;
        PreparedStatement pstmtBoard = null;
        PreparedStatement pstmtFile = null;
        PreparedStatement pstmtDelFile = null;

        String sqlBoard = "UPDATE BOARD SET TITLE = ?, CONTENT = ? WHERE BOARD_NO = ?";
        String sqlFile = "INSERT INTO BOARD_FILE (FILE_NO, BOARD_NO, ORG_NAME, SAVED_NAME, FILE_SIZE) VALUES (SEQ_BOARD_FILE.NEXTVAL, ?, ?, ?, ?)";
        String sqlDelFile = "DELETE FROM BOARD_FILE WHERE FILE_NO = ?";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            pstmtBoard = conn.prepareStatement(sqlBoard);
            pstmtBoard.setString(1, board.getTitle());
            pstmtBoard.setString(2, board.getContent());
            pstmtBoard.setInt(3, board.getBoardNo());
            pstmtBoard.executeUpdate();

            if (delFiles != null && !delFiles.isEmpty()) {
                pstmtDelFile = conn.prepareStatement(sqlDelFile);
                for (int fno : delFiles) {
                    pstmtDelFile.setInt(1, fno);
                    pstmtDelFile.addBatch();
                }
                pstmtDelFile.executeBatch();
            }

            if (files != null && !files.isEmpty()) {
                pstmtFile = conn.prepareStatement(sqlFile);
                for (BoardFileDTO file : files) {
                    pstmtFile.setInt(1, board.getBoardNo());
                    pstmtFile.setString(2, file.getOrgName());
                    pstmtFile.setString(3, file.getSavedName());
                    pstmtFile.setLong(4, file.getFileSize());
                    pstmtFile.addBatch();
                }
                pstmtFile.executeBatch();
            }

            conn.commit();
            result = true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            if (pstmtDelFile != null) try { pstmtDelFile.close(); } catch (SQLException e) {}
            if (pstmtFile != null) try { pstmtFile.close(); } catch (SQLException e) {}
            closeResource(conn, pstmtBoard, null);
        }
        return result;
    }
    public boolean deleteBoard(int boardNo) {
        boolean result = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM BOARD WHERE BOARD_NO = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNo);
            if (pstmt.executeUpdate() > 0) result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, null);
        }
        return result;
    }
    public BoardFileDTO getFileDetail(int fileNo) {
        BoardFileDTO file = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM BOARD_FILE WHERE FILE_NO = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, fileNo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                file = new BoardFileDTO();
                file.setFileNo(rs.getInt("FILE_NO"));
                file.setBoardNo(rs.getInt("BOARD_NO"));
                file.setOrgName(rs.getString("ORG_NAME"));
                file.setSavedName(rs.getString("SAVED_NAME"));
                file.setFileSize(rs.getLong("FILE_SIZE"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResource(conn, pstmt, rs);
        }
        return file;
    }

    private void closeResource(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {}
    }
}
