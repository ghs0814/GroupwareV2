package com.groupware.service;
import com.groupware.dao.*;
import com.groupware.dto.*;
import com.groupware.util.StatusUtil;
import jakarta.servlet.http.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class DashboardService {
@Autowired private EmployeeDAO employeeDAO;
@Autowired private ReservationDAO reservationDAO;
@Autowired private RentalDAO rentalDAO;
@Autowired private LeaveDAO leaveDAO;
private int pageNumber(String value) { try { return Math.max(1,Integer.parseInt(value)); } catch(Exception e) { return 1; } }
public void prepareMain(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
HttpSession session = request.getSession();


EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
if (loginEmp == null) {
	response.sendRedirect("index.jsp");
	return;
}
EmployeeDAO empDao = employeeDAO;
EmployeeDTO updatedEmp = empDao.getEmployeeByNo(String.valueOf(loginEmp.getEmpNo()));
if (updatedEmp != null) {
	session.setAttribute("loginEmp", updatedEmp);
	loginEmp = updatedEmp;
}
int currentEmpNo = loginEmp.getEmpNo();
boolean isManagerMode = "Y".equals(loginEmp.getManager());
LocalDateTime currentDateTime = LocalDateTime.now();
String currentMapping = request.getRequestURI();


final int RECORDS_PER_PAGE = 5;
final int PAGES_PER_BLOCK = 5;
int resPage = 1;
String resPageParam = request.getParameter("resPage");
if (resPageParam != null && !resPageParam.isEmpty())
	resPage = pageNumber(resPageParam);

int rentalPage = 1;
String rentalPageParam = request.getParameter("rentalPage");
if (rentalPageParam != null && !rentalPageParam.isEmpty())
	rentalPage = pageNumber(rentalPageParam);

int leavePage = 1;
String leavePageParam = request.getParameter("leavePage");
if (leavePageParam != null && !leavePageParam.isEmpty())
	leavePage = pageNumber(leavePageParam);


ReservationDAO resDao = reservationDAO;
List<ReservationDTO> rawReserveList = resDao.getMyReservations(currentEmpNo);
List<Map<String, Object>> fullReserveList = new ArrayList<>();

if (rawReserveList != null) {
	for (ReservationDTO dto : rawReserveList) {
		Map<String, Object> m = new HashMap<>();
		m.put("resNo", dto.getResNo());
		m.put("roomId", dto.getRoomId());
		m.put("resDate", dto.getResDate() != null ? dto.getResDate().toString() : "");
		m.put("startTime", dto.getStartTime());
		m.put("endTime", dto.getEndTime());
		m.put("purpose", dto.getPurpose());
		String targetKey = StatusUtil.getStatusKey(dto.getStatus());
		if ("status.res.complete".equals(targetKey)) {
	try {
		LocalDate rDate = ((java.sql.Date) dto.getResDate()).toLocalDate();
		LocalTime eTime = LocalTime.parse(dto.getEndTime());
		if (currentDateTime.isAfter(LocalDateTime.of(rDate, eTime))) {
			targetKey = "status.res.finished";
		}
	} catch (Exception e) {
	}
		}

		m.put("statusKey", targetKey); // 정렬과 화면 출력을 위해 한국어 대신 프로퍼티 키를 바인딩
		m.put("rawDto", dto); // 풀캘린더나 확장 기능을 대비한 원본 백업
		fullReserveList.add(m);
	}
}
if (!fullReserveList.isEmpty()) {
	Collections.sort(fullReserveList, new Comparator<Map<String, Object>>() {
		@Override
		public int compare(Map<String, Object> m1, Map<String, Object> m2) {
	String k1 = (String) m1.get("statusKey");
	String k2 = (String) m2.get("statusKey");
	int p1 = "status.res.complete".equals(k1) ? 1 : 2;
	int p2 = "status.res.complete".equals(k2) ? 1 : 2;

	if (p1 != p2) {
		return Integer.compare(p1, p2); // 1순위: 상태 가중치 비교
	}
	String d1 = (String) m1.get("resDate");
	String d2 = (String) m2.get("resDate");
	return d1.compareTo(d2);
		}
	});
}
int resTotalCount = fullReserveList.size();
int resTotalPages = (int) Math.ceil((double) resTotalCount / RECORDS_PER_PAGE);
if (resTotalPages == 0)
	resTotalPages = 1;

int resStartPage = ((resPage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
int resEndPage = Math.min(resStartPage + PAGES_PER_BLOCK - 1, resTotalPages);

int resStartIdx = (resPage - 1) * RECORDS_PER_PAGE;
int resEndIdx = Math.min(resStartIdx + RECORDS_PER_PAGE, resTotalCount);
List<Map<String, Object>> resSubList = (resStartIdx < resTotalCount)
		? fullReserveList.subList(resStartIdx, resEndIdx)
		: null;


RentalDAO rentalDao = rentalDAO;
List<RentalHistoryDTO> rawRentalList = isManagerMode
		? rentalDao.getAllDocumentList()
		: rentalDao.getMyRentalList(currentEmpNo);
List<RentalHistoryDTO> filteredRentalList = new ArrayList<>();
if (rawRentalList != null) {
	for (RentalHistoryDTO item : rawRentalList) {
		if (isManagerMode) {
	boolean isRetired = (item.getEmpLevel() == 0);
	boolean isMyDoc = (item.getEmpNo() == currentEmpNo);
	boolean isTargetRetired = isRetired && ("대여중".equals(item.getStatus()) || "미반납".equals(item.getStatus()));
	if (!isTargetRetired && !isMyDoc)
		continue;
		}
		filteredRentalList.add(item);
	}
}
if (!filteredRentalList.isEmpty()) {
	Collections.sort(filteredRentalList, new Comparator<RentalHistoryDTO>() {
		@Override
		public int compare(RentalHistoryDTO r1, RentalHistoryDTO r2) {
	String k1 = StatusUtil.getStatusKey(r1.getStatus());
	String k2 = StatusUtil.getStatusKey(r2.getStatus());
	int p1 = 3; // 기본값 (반납완료, 반려됨 등 종료 상태)
	if ("status.rental.renting".equals(k1))
		p1 = 0; // 대여중 (0순위)
	else if ("status.rental.notreturned".equals(k1))
		p1 = 1; // 미반납 (1순위)
	else if ("status.rental.wait".equals(k1))
		p1 = 2; // 승인대기 (2순위)

	int p2 = 3;
	if ("status.rental.renting".equals(k2))
		p2 = 0;
	else if ("status.rental.notreturned".equals(k2))
		p2 = 1;
	else if ("status.rental.wait".equals(k2))
		p2 = 2;

	if (p1 != p2) {
		return Integer.compare(p1, p2); // 1순위: 상태 가중치 정렬
	}
	String d1 = r1.getRentalDate() != null ? r1.getRentalDate().toString() : "";
	String d2 = r2.getRentalDate() != null ? r2.getRentalDate().toString() : "";
	return d2.compareTo(d1);
		}
	});
}
int rentalTotalCount = filteredRentalList.size();
int rentalTotalPages = (int) Math.ceil((double) rentalTotalCount / RECORDS_PER_PAGE);
if (rentalTotalPages == 0)
	rentalTotalPages = 1;

int rentalStartPage = ((rentalPage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
int rentalEndPage = Math.min(rentalStartPage + PAGES_PER_BLOCK - 1, rentalTotalPages);

int rentalStartIdx = (rentalPage - 1) * RECORDS_PER_PAGE;
int rentalEndIdx = Math.min(rentalStartIdx + RECORDS_PER_PAGE, rentalTotalCount);
List<RentalHistoryDTO> rentalList = (rentalStartIdx < rentalTotalCount)
		? filteredRentalList.subList(rentalStartIdx, rentalEndIdx)
		: null;


LeaveDAO leaveDao = leaveDAO;
List<LeaveHistoryDTO> fullLeaveList = leaveDao.getMyLeaveList(currentEmpNo);
if (fullLeaveList != null && !fullLeaveList.isEmpty()) {
	Collections.sort(fullLeaveList, new Comparator<LeaveHistoryDTO>() {
		@Override
		public int compare(LeaveHistoryDTO l1, LeaveHistoryDTO l2) {
	String k1 = StatusUtil.getStatusKey(l1.getStatus());
	String k2 = StatusUtil.getStatusKey(l2.getStatus());

	int p1 = 2; // 기본값 (반려됨 등 최하단)
	if ("status.rental.wait".equals(k1))
		p1 = 0; // 승인대기 (최상단)
	else if ("status.leave.complete".equals(k1))
		p1 = 1; // 승인완료 (중간)

	int p2 = 2;
	if ("status.rental.wait".equals(k2))
		p2 = 0;
	else if ("status.leave.complete".equals(k2))
		p2 = 1;

	if (p1 != p2) {
		return Integer.compare(p1, p2); // 1순위: 휴가 승인 상태 우선순위 정렬
	}
	String d1 = l1.getStartDate() != null ? l1.getStartDate().toString() : "";
	String d2 = l2.getStartDate() != null ? l2.getStartDate().toString() : "";
	return d2.compareTo(d1);
		}
	});
}
int leaveTotalCount = (fullLeaveList != null) ? fullLeaveList.size() : 0;
int leaveTotalPages = (int) Math.ceil((double) leaveTotalCount / RECORDS_PER_PAGE);
if (leaveTotalPages == 0)
	leaveTotalPages = 1;

int leaveStartPage = ((leavePage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
int leaveEndPage = Math.min(leaveStartPage + PAGES_PER_BLOCK - 1, leaveTotalPages);

int leaveStartIdx = (leavePage - 1) * RECORDS_PER_PAGE;
int leaveEndIdx = Math.min(leaveStartIdx + RECORDS_PER_PAGE, leaveTotalCount);
List<LeaveHistoryDTO> leaveList = (fullLeaveList != null && leaveStartIdx < leaveTotalCount)
		? fullLeaveList.subList(leaveStartIdx, leaveEndIdx)
		: null;

request.setAttribute("loginEmp", loginEmp);
request.setAttribute("updatedEmp", updatedEmp);
request.setAttribute("currentEmpNo", currentEmpNo);
request.setAttribute("isManagerMode", isManagerMode);
request.setAttribute("currentDateTime", currentDateTime);
request.setAttribute("currentMapping", currentMapping);
request.setAttribute("RECORDS_PER_PAGE", RECORDS_PER_PAGE);
request.setAttribute("PAGES_PER_BLOCK", PAGES_PER_BLOCK);
request.setAttribute("resPage", resPage);
request.setAttribute("resPageParam", resPageParam);
request.setAttribute("rentalPage", rentalPage);
request.setAttribute("rentalPageParam", rentalPageParam);
request.setAttribute("leavePage", leavePage);
request.setAttribute("leavePageParam", leavePageParam);
request.setAttribute("rawReserveList", rawReserveList);
request.setAttribute("fullReserveList", fullReserveList);
request.setAttribute("resTotalCount", resTotalCount);
request.setAttribute("resTotalPages", resTotalPages);
request.setAttribute("resStartPage", resStartPage);
request.setAttribute("resEndPage", resEndPage);
request.setAttribute("resStartIdx", resStartIdx);
request.setAttribute("resEndIdx", resEndIdx);
request.setAttribute("resSubList", resSubList);
request.setAttribute("rawRentalList", rawRentalList);
request.setAttribute("filteredRentalList", filteredRentalList);
request.setAttribute("rentalTotalCount", rentalTotalCount);
request.setAttribute("rentalTotalPages", rentalTotalPages);
request.setAttribute("rentalStartPage", rentalStartPage);
request.setAttribute("rentalEndPage", rentalEndPage);
request.setAttribute("rentalStartIdx", rentalStartIdx);
request.setAttribute("rentalEndIdx", rentalEndIdx);
request.setAttribute("rentalList", rentalList);
request.setAttribute("fullLeaveList", fullLeaveList);
request.setAttribute("leaveTotalCount", leaveTotalCount);
request.setAttribute("leaveTotalPages", leaveTotalPages);
request.setAttribute("leaveStartPage", leaveStartPage);
request.setAttribute("leaveEndPage", leaveEndPage);
request.setAttribute("leaveStartIdx", leaveStartIdx);
request.setAttribute("leaveEndIdx", leaveEndIdx);
request.setAttribute("leaveList", leaveList);
}
public void prepareMyPage(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
HttpSession session = request.getSession();


EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
if (loginEmp == null) {
	response.sendRedirect("index.jsp");
	return;
}

int currentEmpNo = loginEmp.getEmpNo();
boolean isManagerMode = "Y".equals(loginEmp.getManager());
LocalDateTime currentDateTime = LocalDateTime.now();
String currentMapping = request.getRequestURI();


final int RECORDS_PER_PAGE = 5;
final int PAGES_PER_BLOCK = 5;

int resPage = 1;
String resPageParam = request.getParameter("resPage");
if (resPageParam != null && !resPageParam.isEmpty())
	resPage = pageNumber(resPageParam);

int rentalPage = 1;
String rentalPageParam = request.getParameter("rentalPage");
if (rentalPageParam != null && !rentalPageParam.isEmpty())
	rentalPage = pageNumber(rentalPageParam);

int leavePage = 1;
String leavePageParam = request.getParameter("leavePage");
if (leavePageParam != null && !leavePageParam.isEmpty())
	leavePage = pageNumber(leavePageParam);


ReservationDAO resDao = reservationDAO;
List<ReservationDTO> rawReserveList = resDao.getMyReservations(currentEmpNo);
List<Map<String, Object>> fullReserveList = new ArrayList<>();

if (rawReserveList != null) {
	for (ReservationDTO dto : rawReserveList) {
		Map<String, Object> m = new HashMap<>();
		m.put("resNo", dto.getResNo());
		m.put("roomId", dto.getRoomId());
		m.put("resDate", dto.getResDate() != null ? dto.getResDate().toString() : "");
		m.put("startTime", dto.getStartTime());
		m.put("endTime", dto.getEndTime());
		m.put("purpose", dto.getPurpose());

		String targetKey = StatusUtil.getStatusKey(dto.getStatus());

		if ("status.res.complete".equals(targetKey)) {
	try {
		LocalDate rDate = ((java.sql.Date) dto.getResDate()).toLocalDate();
		LocalTime eTime = LocalTime.parse(dto.getEndTime());
		if (currentDateTime.isAfter(LocalDateTime.of(rDate, eTime))) {
			targetKey = "status.res.finished";
		}
	} catch (Exception e) {
	}
		}

		m.put("statusKey", targetKey);
		m.put("rawDto", dto);
		fullReserveList.add(m);
	}
}

if (!fullReserveList.isEmpty()) {
	Collections.sort(fullReserveList, new Comparator<Map<String, Object>>() {
		@Override
		public int compare(Map<String, Object> m1, Map<String, Object> m2) {
	String k1 = (String) m1.get("statusKey");
	String k2 = (String) m2.get("statusKey");

	int p1 = "status.res.complete".equals(k1) ? 1 : 2;
	int p2 = "status.res.complete".equals(k2) ? 1 : 2;

	if (p1 != p2) {
		return Integer.compare(p1, p2);
	}

	String d1 = (String) m1.get("resDate");
	String d2 = (String) m2.get("resDate");
	return d1.compareTo(d2);
		}
	});
}

int resTotalCount = fullReserveList.size();
int resTotalPages = (int) Math.ceil((double) resTotalCount / RECORDS_PER_PAGE);
if (resTotalPages == 0)
	resTotalPages = 1;

int resStartPage = ((resPage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
int resEndPage = Math.min(resStartPage + PAGES_PER_BLOCK - 1, resTotalPages);

int resStartIdx = (resPage - 1) * RECORDS_PER_PAGE;
int resEndIdx = Math.min(resStartIdx + RECORDS_PER_PAGE, resTotalCount);

List<Map<String, Object>> resSubList = (resStartIdx < resTotalCount)
		? fullReserveList.subList(resStartIdx, resEndIdx)
		: null;


RentalDAO rentalDao = rentalDAO;
List<RentalHistoryDTO> rawRentalList = isManagerMode
		? rentalDao.getAllDocumentList()
		: rentalDao.getMyRentalList(currentEmpNo);
List<RentalHistoryDTO> filteredRentalList = new ArrayList<>();

if (rawRentalList != null) {
	for (RentalHistoryDTO item : rawRentalList) {
		if (isManagerMode) {
	boolean isRetired = (item.getEmpLevel() == 0);
	boolean isMyDoc = (item.getEmpNo() == currentEmpNo);
	boolean isTargetRetired = isRetired && ("대여중".equals(item.getStatus()) || "미반납".equals(item.getStatus()));
	if (!isTargetRetired && !isMyDoc)
		continue;
		}
		filteredRentalList.add(item);
	}
}

if (!filteredRentalList.isEmpty()) {
	Collections.sort(filteredRentalList, new Comparator<RentalHistoryDTO>() {
		@Override
		public int compare(RentalHistoryDTO r1, RentalHistoryDTO r2) {
	String k1 = StatusUtil.getStatusKey(r1.getStatus());
	String k2 = StatusUtil.getStatusKey(r2.getStatus());

	int p1 = 3;
	if ("status.rental.renting".equals(k1))
		p1 = 0;
	else if ("status.rental.notreturned".equals(k1))
		p1 = 1;
	else if ("status.rental.wait".equals(k1))
		p1 = 2;

	int p2 = 3;
	if ("status.rental.renting".equals(k2))
		p2 = 0;
	else if ("status.rental.notreturned".equals(k2))
		p2 = 1;
	else if ("status.rental.wait".equals(k2))
		p2 = 2;

	if (p1 != p2) {
		return Integer.compare(p1, p2);
	}

	String d1 = r1.getRentalDate() != null ? r1.getRentalDate().toString() : "";
	String d2 = r2.getRentalDate() != null ? r2.getRentalDate().toString() : "";
	return d2.compareTo(d1);
		}
	});
}

int rentalTotalCount = filteredRentalList.size();
int rentalTotalPages = (int) Math.ceil((double) rentalTotalCount / RECORDS_PER_PAGE);
if (rentalTotalPages == 0)
	rentalTotalPages = 1;

int rentalStartPage = ((rentalPage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
int rentalEndPage = Math.min(rentalStartPage + PAGES_PER_BLOCK - 1, rentalTotalPages);

int rentalStartIdx = (rentalPage - 1) * RECORDS_PER_PAGE;
int rentalEndIdx = Math.min(rentalStartIdx + RECORDS_PER_PAGE, rentalTotalCount);

List<RentalHistoryDTO> rentalList = (rentalStartIdx < rentalTotalCount)
		? filteredRentalList.subList(rentalStartIdx, rentalEndIdx)
		: null;


LeaveDAO leaveDao = leaveDAO;
List<LeaveHistoryDTO> fullLeaveList = leaveDao.getMyLeaveList(currentEmpNo);

if (fullLeaveList != null && !fullLeaveList.isEmpty()) {
	Collections.sort(fullLeaveList, new Comparator<LeaveHistoryDTO>() {
		@Override
		public int compare(LeaveHistoryDTO l1, LeaveHistoryDTO l2) {
	String k1 = StatusUtil.getStatusKey(l1.getStatus());
	String k2 = StatusUtil.getStatusKey(l2.getStatus());

	int p1 = 2;
	if ("status.rental.wait".equals(k1))
		p1 = 0;
	else if ("status.leave.complete".equals(k1))
		p1 = 1;

	int p2 = 2;
	if ("status.rental.wait".equals(k2))
		p2 = 0;
	else if ("status.leave.complete".equals(k2))
		p2 = 1;

	if (p1 != p2) {
		return Integer.compare(p1, p2);
	}

	String d1 = l1.getStartDate() != null ? l1.getStartDate().toString() : "";
	String d2 = l2.getStartDate() != null ? l2.getStartDate().toString() : "";
	return d2.compareTo(d1);
		}
	});
}

int leaveTotalCount = (fullLeaveList != null) ? fullLeaveList.size() : 0;
int leaveTotalPages = (int) Math.ceil((double) leaveTotalCount / RECORDS_PER_PAGE);
if (leaveTotalPages == 0)
	leaveTotalPages = 1;

int leaveStartPage = ((leavePage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
int leaveEndPage = Math.min(leaveStartPage + PAGES_PER_BLOCK - 1, leaveTotalPages);

int leaveStartIdx = (leavePage - 1) * RECORDS_PER_PAGE;
int leaveEndIdx = Math.min(leaveStartIdx + RECORDS_PER_PAGE, leaveTotalCount);

List<LeaveHistoryDTO> leaveList = (fullLeaveList != null && leaveStartIdx < leaveTotalCount)
		? fullLeaveList.subList(leaveStartIdx, leaveEndIdx)
		: null;

request.setAttribute("loginEmp", loginEmp);
request.setAttribute("currentEmpNo", currentEmpNo);
request.setAttribute("isManagerMode", isManagerMode);
request.setAttribute("currentDateTime", currentDateTime);
request.setAttribute("currentMapping", currentMapping);
request.setAttribute("RECORDS_PER_PAGE", RECORDS_PER_PAGE);
request.setAttribute("PAGES_PER_BLOCK", PAGES_PER_BLOCK);
request.setAttribute("resPage", resPage);
request.setAttribute("resPageParam", resPageParam);
request.setAttribute("rentalPage", rentalPage);
request.setAttribute("rentalPageParam", rentalPageParam);
request.setAttribute("leavePage", leavePage);
request.setAttribute("leavePageParam", leavePageParam);
request.setAttribute("rawReserveList", rawReserveList);
request.setAttribute("fullReserveList", fullReserveList);
request.setAttribute("resTotalCount", resTotalCount);
request.setAttribute("resTotalPages", resTotalPages);
request.setAttribute("resStartPage", resStartPage);
request.setAttribute("resEndPage", resEndPage);
request.setAttribute("resStartIdx", resStartIdx);
request.setAttribute("resEndIdx", resEndIdx);
request.setAttribute("resSubList", resSubList);
request.setAttribute("rawRentalList", rawRentalList);
request.setAttribute("filteredRentalList", filteredRentalList);
request.setAttribute("rentalTotalCount", rentalTotalCount);
request.setAttribute("rentalTotalPages", rentalTotalPages);
request.setAttribute("rentalStartPage", rentalStartPage);
request.setAttribute("rentalEndPage", rentalEndPage);
request.setAttribute("rentalStartIdx", rentalStartIdx);
request.setAttribute("rentalEndIdx", rentalEndIdx);
request.setAttribute("rentalList", rentalList);
request.setAttribute("fullLeaveList", fullLeaveList);
request.setAttribute("leaveTotalCount", leaveTotalCount);
request.setAttribute("leaveTotalPages", leaveTotalPages);
request.setAttribute("leaveStartPage", leaveStartPage);
request.setAttribute("leaveEndPage", leaveEndPage);
request.setAttribute("leaveStartIdx", leaveStartIdx);
request.setAttribute("leaveEndIdx", leaveEndIdx);
request.setAttribute("leaveList", leaveList);
}
public void prepareMyReservation(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
HttpSession session = request.getSession();

     
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp"); 
        return;
    }
    int currentEmpNo = loginEmp.getEmpNo();              
    boolean isManagerMode = "Y".equals(loginEmp.getManager()); 
    LocalDateTime currentDateTime = LocalDateTime.now();       
    String currentMapping = request.getRequestURI();           

    
    final int RECORDS_PER_PAGE = 10; 
    final int PAGES_PER_BLOCK = 10;  
    int resPage = 1;
    String resPageParam = request.getParameter("resPage");
    if (resPageParam != null && !resPageParam.isEmpty()) resPage = pageNumber(resPageParam);

    int rentalPage = 1;
    String rentalPageParam = request.getParameter("rentalPage");
    if (rentalPageParam != null && !rentalPageParam.isEmpty()) rentalPage = pageNumber(rentalPageParam);

    int leavePage = 1;
    String leavePageParam = request.getParameter("leavePage");
    if (leavePageParam != null && !leavePageParam.isEmpty()) leavePage = pageNumber(leavePageParam);

    
    ReservationDAO resDao = reservationDAO;
    List<ReservationDTO> rawReserveList = resDao.getMyReservations(currentEmpNo); 
    List<Map<String, Object>> fullReserveList = new ArrayList<>();

    if (rawReserveList != null) {
        for (ReservationDTO dto : rawReserveList) {
            Map<String, Object> m = new HashMap<>();
            m.put("resNo", dto.getResNo());
            m.put("roomId", dto.getRoomId());
            m.put("resDate", dto.getResDate() != null ? dto.getResDate().toString() : "");
            m.put("startTime", dto.getStartTime());
            m.put("endTime", dto.getEndTime());
            m.put("purpose", dto.getPurpose());
            String targetKey = StatusUtil.getStatusKey(dto.getStatus());
            if ("status.res.complete".equals(targetKey)) {
                try {
                    LocalDate rDate = ((java.sql.Date) dto.getResDate()).toLocalDate();
                    LocalTime eTime = LocalTime.parse(dto.getEndTime());
                    if (currentDateTime.isAfter(LocalDateTime.of(rDate, eTime))) {
                        targetKey = "status.res.finished";
                    }
                } catch (Exception e) {}
            }
            
            m.put("statusKey", targetKey); // 정렬과 화면 출력을 위해 한국어 대신 프로퍼티 키를 바인딩
            m.put("rawDto", dto);          // 풀캘린더나 확장 기능을 대비한 원본 백업
            fullReserveList.add(m);
        }
    }
    if (!fullReserveList.isEmpty()) {
        Collections.sort(fullReserveList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                String k1 = (String) m1.get("statusKey");
                String k2 = (String) m2.get("statusKey");
                int p1 = "status.res.complete".equals(k1) ? 1 : 2;
                int p2 = "status.res.complete".equals(k2) ? 1 : 2;
                 
                if (p1 != p2) {
                    return Integer.compare(p1, p2); // 1순위: 상태 가중치 비교
                }
                String d1 = (String) m1.get("resDate");
                String d2 = (String) m2.get("resDate");
                return d1.compareTo(d2);
            }
        });
    }
    int resTotalCount = fullReserveList.size();
    int resTotalPages = (int) Math.ceil((double) resTotalCount / RECORDS_PER_PAGE);
    if (resTotalPages == 0) resTotalPages = 1; 
     
    int resStartPage = ((resPage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
    int resEndPage = Math.min(resStartPage + PAGES_PER_BLOCK - 1, resTotalPages); 
     
    int resStartIdx = (resPage - 1) * RECORDS_PER_PAGE;
    int resEndIdx = Math.min(resStartIdx + RECORDS_PER_PAGE, resTotalCount);
    List<Map<String, Object>> resSubList = (resStartIdx < resTotalCount) 
                                         ? fullReserveList.subList(resStartIdx, resEndIdx) : null;

 
request.setAttribute("loginEmp", loginEmp);
request.setAttribute("currentEmpNo", currentEmpNo);
request.setAttribute("isManagerMode", isManagerMode);
request.setAttribute("currentDateTime", currentDateTime);
request.setAttribute("currentMapping", currentMapping);
request.setAttribute("RECORDS_PER_PAGE", RECORDS_PER_PAGE);
request.setAttribute("PAGES_PER_BLOCK", PAGES_PER_BLOCK);
request.setAttribute("resPage", resPage);
request.setAttribute("resPageParam", resPageParam);
request.setAttribute("rentalPage", rentalPage);
request.setAttribute("rentalPageParam", rentalPageParam);
request.setAttribute("leavePage", leavePage);
request.setAttribute("leavePageParam", leavePageParam);
request.setAttribute("rawReserveList", rawReserveList);
request.setAttribute("fullReserveList", fullReserveList);
request.setAttribute("resTotalCount", resTotalCount);
request.setAttribute("resTotalPages", resTotalPages);
request.setAttribute("resStartPage", resStartPage);
request.setAttribute("resEndPage", resEndPage);
request.setAttribute("resStartIdx", resStartIdx);
request.setAttribute("resEndIdx", resEndIdx);
request.setAttribute("resSubList", resSubList);
}
public void prepareMyRental(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
HttpSession session = request.getSession();

	
	EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
	if (loginEmp == null) {
		response.sendRedirect("index.jsp");
		return;
	}
    int currentEmpNo = loginEmp.getEmpNo();              
    boolean isManagerMode = "Y".equals(loginEmp.getManager()); 
    LocalDateTime currentDateTime = LocalDateTime.now();       
    String currentMapping = request.getRequestURI();  
	

    
    final int RECORDS_PER_PAGE = 10;
    final int PAGES_PER_BLOCK = 10;
    int resPage = 1;
    String resPageParam = request.getParameter("resPage");
    if (resPageParam != null && !resPageParam.isEmpty()) resPage = pageNumber(resPageParam);

    int rentalPage = 1;
    String rentalPageParam = request.getParameter("rentalPage");
    if (rentalPageParam != null && !rentalPageParam.isEmpty()) rentalPage = pageNumber(rentalPageParam);

    int leavePage = 1;
    String leavePageParam = request.getParameter("leavePage");
    if (leavePageParam != null && !leavePageParam.isEmpty()) leavePage = pageNumber(leavePageParam);


    
    
    RentalDAO rentalDao = rentalDAO;
    List<RentalHistoryDTO> rawRentalList = isManagerMode ? rentalDao.getAllDocumentList() : rentalDao.getMyRentalList(currentEmpNo);
    List<RentalHistoryDTO> filteredRentalList = new ArrayList<>();
    if (rawRentalList != null) {
        for (RentalHistoryDTO item : rawRentalList) {
            if (isManagerMode) {
                boolean isRetired = (item.getEmpLevel() == 0); 
                boolean isMyDoc = (item.getEmpNo() == currentEmpNo);
                boolean isTargetRetired = isRetired && ("대여중".equals(item.getStatus()) || "미반납".equals(item.getStatus()));
                if (!isTargetRetired && !isMyDoc) continue; 
            }
            filteredRentalList.add(item);
        }
    }
    if (!filteredRentalList.isEmpty()) {
        Collections.sort(filteredRentalList, new Comparator<RentalHistoryDTO>() {
            @Override
            public int compare(RentalHistoryDTO r1, RentalHistoryDTO r2) {
                String k1 = StatusUtil.getStatusKey(r1.getStatus());
                String k2 = StatusUtil.getStatusKey(r2.getStatus());
                int p1 = 3; // 기본값 (반납완료, 반려됨 등 종료 상태)
                if ("status.rental.renting".equals(k1)) p1 = 0;       // 대여중 (0순위)
                else if ("status.rental.notreturned".equals(k1)) p1 = 1; // 미반납 (1순위)
                else if ("status.rental.wait".equals(k1)) p1 = 2;       // 승인대기 (2순위)
                
                int p2 = 3;
                if ("status.rental.renting".equals(k2)) p2 = 0;
                else if ("status.rental.notreturned".equals(k2)) p2 = 1;
                else if ("status.rental.wait".equals(k2)) p2 = 2;
                
                if (p1 != p2) {
                    return Integer.compare(p1, p2); // 1순위: 상태 가중치 정렬
                }
                String d1 = r1.getRentalDate() != null ? r1.getRentalDate().toString() : "";
                String d2 = r2.getRentalDate() != null ? r2.getRentalDate().toString() : "";
                return d2.compareTo(d1); 
            }
        });
    }
    int rentalTotalCount = filteredRentalList.size();
    int rentalTotalPages = (int) Math.ceil((double) rentalTotalCount / RECORDS_PER_PAGE);
    if (rentalTotalPages == 0) rentalTotalPages = 1;
    
    int rentalStartPage = ((rentalPage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
    int rentalEndPage = Math.min(rentalStartPage + PAGES_PER_BLOCK - 1, rentalTotalPages);
    
    int rentalStartIdx = (rentalPage - 1) * RECORDS_PER_PAGE;
    int rentalEndIdx = Math.min(rentalStartIdx + RECORDS_PER_PAGE, rentalTotalCount);
    List<RentalHistoryDTO> rentalList = (rentalStartIdx < rentalTotalCount) ? filteredRentalList.subList(rentalStartIdx, rentalEndIdx) : null;  
            


request.setAttribute("loginEmp", loginEmp);
request.setAttribute("currentEmpNo", currentEmpNo);
request.setAttribute("isManagerMode", isManagerMode);
request.setAttribute("currentDateTime", currentDateTime);
request.setAttribute("currentMapping", currentMapping);
request.setAttribute("RECORDS_PER_PAGE", RECORDS_PER_PAGE);
request.setAttribute("PAGES_PER_BLOCK", PAGES_PER_BLOCK);
request.setAttribute("resPage", resPage);
request.setAttribute("resPageParam", resPageParam);
request.setAttribute("rentalPage", rentalPage);
request.setAttribute("rentalPageParam", rentalPageParam);
request.setAttribute("leavePage", leavePage);
request.setAttribute("leavePageParam", leavePageParam);
request.setAttribute("rawRentalList", rawRentalList);
request.setAttribute("filteredRentalList", filteredRentalList);
request.setAttribute("rentalTotalCount", rentalTotalCount);
request.setAttribute("rentalTotalPages", rentalTotalPages);
request.setAttribute("rentalStartPage", rentalStartPage);
request.setAttribute("rentalEndPage", rentalEndPage);
request.setAttribute("rentalStartIdx", rentalStartIdx);
request.setAttribute("rentalEndIdx", rentalEndIdx);
request.setAttribute("rentalList", rentalList);
}
public void prepareMyLeave(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
HttpSession session = request.getSession();

        
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp"); 
        return;
    }
    int currentEmpNo = loginEmp.getEmpNo();              
    boolean isManagerMode = "Y".equals(loginEmp.getManager()); 
    LocalDateTime currentDateTime = LocalDateTime.now();       
    String currentMapping = request.getRequestURI();           

    
    final int RECORDS_PER_PAGE = 10; 
    final int PAGES_PER_BLOCK = 10;  
    int resPage = 1;
    String resPageParam = request.getParameter("resPage");
    if (resPageParam != null && !resPageParam.isEmpty()) resPage = pageNumber(resPageParam);

    int rentalPage = 1;
    String rentalPageParam = request.getParameter("rentalPage");
    if (rentalPageParam != null && !rentalPageParam.isEmpty()) rentalPage = pageNumber(rentalPageParam);

    int leavePage = 1;
    String leavePageParam = request.getParameter("leavePage");
    if (leavePageParam != null && !leavePageParam.isEmpty()) leavePage = pageNumber(leavePageParam);

    
    LeaveDAO leaveDao = leaveDAO;
    List<LeaveHistoryDTO> fullLeaveList = leaveDao.getMyLeaveList(currentEmpNo); 
    if (fullLeaveList != null && !fullLeaveList.isEmpty()) {
        Collections.sort(fullLeaveList, new Comparator<LeaveHistoryDTO>() {
            @Override
            public int compare(LeaveHistoryDTO l1, LeaveHistoryDTO l2) {
                String k1 = StatusUtil.getStatusKey(l1.getStatus());
                String k2 = StatusUtil.getStatusKey(l2.getStatus());
                
                int p1 = 2; // 기본값 (반려됨 등 최하단)
                if ("status.rental.wait".equals(k1)) p1 = 0;       // 승인대기 (최상단)
                else if ("status.leave.complete".equals(k1)) p1 = 1; // 승인완료 (중간)
                
                int p2 = 2;
                if ("status.rental.wait".equals(k2)) p2 = 0;
                else if ("status.leave.complete".equals(k2)) p2 = 1;
                
                if (p1 != p2) {
                    return Integer.compare(p1, p2); // 1순위: 휴가 승인 상태 우선순위 정렬
                }
                String d1 = l1.getStartDate() != null ? l1.getStartDate().toString() : "";
                String d2 = l2.getStartDate() != null ? l2.getStartDate().toString() : "";
                return d2.compareTo(d1); 
            }
        });
    }
    int leaveTotalCount = (fullLeaveList != null) ? fullLeaveList.size() : 0;
    int leaveTotalPages = (int) Math.ceil((double) leaveTotalCount / RECORDS_PER_PAGE);
    if (leaveTotalPages == 0) leaveTotalPages = 1;
    
    int leaveStartPage = ((leavePage - 1) / PAGES_PER_BLOCK) * PAGES_PER_BLOCK + 1;
    int leaveEndPage = Math.min(leaveStartPage + PAGES_PER_BLOCK - 1, leaveTotalPages);
    
    int leaveStartIdx = (leavePage - 1) * RECORDS_PER_PAGE;
    int leaveEndIdx = Math.min(leaveStartIdx + RECORDS_PER_PAGE, leaveTotalCount);
    List<LeaveHistoryDTO> leaveList = (fullLeaveList != null && leaveStartIdx < leaveTotalCount) ? fullLeaveList.subList(leaveStartIdx, leaveEndIdx) : null;

request.setAttribute("loginEmp", loginEmp);
request.setAttribute("currentEmpNo", currentEmpNo);
request.setAttribute("isManagerMode", isManagerMode);
request.setAttribute("currentDateTime", currentDateTime);
request.setAttribute("currentMapping", currentMapping);
request.setAttribute("RECORDS_PER_PAGE", RECORDS_PER_PAGE);
request.setAttribute("PAGES_PER_BLOCK", PAGES_PER_BLOCK);
request.setAttribute("resPage", resPage);
request.setAttribute("resPageParam", resPageParam);
request.setAttribute("rentalPage", rentalPage);
request.setAttribute("rentalPageParam", rentalPageParam);
request.setAttribute("leavePage", leavePage);
request.setAttribute("leavePageParam", leavePageParam);
request.setAttribute("fullLeaveList", fullLeaveList);
request.setAttribute("leaveTotalCount", leaveTotalCount);
request.setAttribute("leaveTotalPages", leaveTotalPages);
request.setAttribute("leaveStartPage", leaveStartPage);
request.setAttribute("leaveEndPage", leaveEndPage);
request.setAttribute("leaveStartIdx", leaveStartIdx);
request.setAttribute("leaveEndIdx", leaveEndIdx);
request.setAttribute("leaveList", leaveList);
}
}
