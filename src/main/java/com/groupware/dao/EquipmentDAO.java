package com.groupware.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.groupware.dto.EquipmentDTO;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class EquipmentDAO {
    private final DataSource dataSource;

    public EquipmentDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }


	public List<EquipmentDTO> getAllEquipments() {
		List<EquipmentDTO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM EQUIPMENT WHERE 1=1 AND ISCHECKED = 'Y' ORDER BY EQ_NO ASC";
		System.out.println("전체 : 장비 list 출력 : getAllEquipments"+sql);
		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();

				while (rs.next()) {
					EquipmentDTO dto = new EquipmentDTO();
					String ischecked_ch = rs.getString("ischecked");
					dto.setEqNo(rs.getInt("EQ_NO"));
					dto.setEqName(rs.getString("EQ_NAME"));
					dto.setTotalCount(rs.getInt("TOTAL_COUNT"));
					dto.setRemainCount(rs.getInt("REMAIN_COUNT"));
					dto.setIschecked(ischecked_ch.charAt(0));
					list.add(dto);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(conn, pstmt, rs);
		}
		return list;
	}
	public boolean insertEquipment(EquipmentDTO dto) {
		boolean result = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO EQUIPMENT (EQ_NO, EQ_NAME, TOTAL_COUNT, REMAIN_COUNT) "
				+ "VALUES (SEQ_EQUIPMENT.NEXTVAL, ?, ?, ?)";

		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, dto.getEqName());
				pstmt.setInt(2, dto.getTotalCount());
				pstmt.setInt(3, dto.getTotalCount()); // 초기 등록 시 잔여량 = 총수량

				int count = pstmt.executeUpdate();
				if (count > 0)
					result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(conn, pstmt, null);
		}
		return result;
	}

	public boolean updateEquipment(EquipmentDTO dto) {
		boolean result = false;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "UPDATE EQUIPMENT SET EQ_NAME = ?, TOTAL_COUNT = ?, REMAIN_COUNT = ? WHERE EQ_NO = ?";

		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, dto.getEqName());
				pstmt.setInt(2, dto.getTotalCount());
				pstmt.setInt(3, dto.getRemainCount());
				pstmt.setInt(4, dto.getEqNo());

				int count = pstmt.executeUpdate();
				if (count > 0)
					result = true;
			}
			System.out.println("비품 정보 수정 : "+sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(conn, pstmt, null);
		}
		return result;
	}

	public boolean deleteEquipment(int eqNo) {
		boolean result = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "UPDATE EQUIPMENT SET ISCHECKED = 'N' WHERE EQ_NO = ?";
		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, eqNo);

				int count = pstmt.executeUpdate();
				if (count > 0)
					result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(conn, pstmt, null);
		}
		return result;
	}

	public EquipmentDTO getEquipmentDetail(int eqNo) {
		EquipmentDTO dto = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT * FROM EQUIPMENT WHERE EQ_NO = ?";

		try {
			conn = dataSource.getConnection();
			if (conn != null) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, eqNo);

				rs = pstmt.executeQuery();

				if (rs.next()) {
					dto = new EquipmentDTO();
					dto.setEqNo(rs.getInt("EQ_NO"));
					dto.setEqName(rs.getString("EQ_NAME"));
					dto.setTotalCount(rs.getInt("TOTAL_COUNT"));
					dto.setRemainCount(rs.getInt("REMAIN_COUNT"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(conn, pstmt, rs);
		}
		return dto;
	}
	
	public int getCheckEquipment(int eqNo) {
        int count = 0;
        String sql = "SELECT CASE WHEN TOTAL_COUNT = REAMIN_COUNT THEN '1' ELSE '0' END 'COUNT' FROM EQUIPMENT WHERE 1=1 AND EQ_NO = ?";
		
        try {
        	System.out.println(sql);
        	Connection conn = dataSource.getConnection();
        	PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eqNo);
        		
            ResultSet rs = pstmt.executeQuery(); 
            if (rs.next()) count = rs.getInt(1);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

	public int getTotalCount() {
		int count = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT COUNT(*) FROM EQUIPMENT";
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

	public List<EquipmentDTO> getEquipmentsPaging(int startRow, int endRow) {
		List<EquipmentDTO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM ("
				   + "  SELECT ROWNUM AS RN, A.* FROM ("
				   + "    SELECT * FROM EQUIPMENT ORDER BY EQ_NO ASC"
				   + "  ) A"
				   + ") WHERE RN BETWEEN ? AND ?";

		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, startRow);
			pstmt.setInt(2, endRow);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				EquipmentDTO dto = new EquipmentDTO();
				dto.setEqNo(rs.getInt("EQ_NO"));
				dto.setEqName(rs.getString("EQ_NAME"));
				dto.setTotalCount(rs.getInt("TOTAL_COUNT"));
				dto.setRemainCount(rs.getInt("REMAIN_COUNT"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(conn, pstmt, rs);
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
			e.printStackTrace();
		}
	}
}
