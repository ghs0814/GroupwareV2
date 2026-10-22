package com.groupware.util;

import java.util.HashMap;
import java.util.Map;

public class StatusUtil {
    private static final Map<String, String> statusMap = new HashMap<>();

    static {
        statusMap.put("예약완료", "status.res.complete");
        statusMap.put("이용 종료", "status.res.finished");
        statusMap.put("취소됨", "status.res.canceled");
        statusMap.put("대여중", "status.rental.renting");
        statusMap.put("미반납", "status.rental.notreturned");
        statusMap.put("승인대기", "status.rental.wait");
        statusMap.put("승인완료", "status.leave.complete");
        statusMap.put("반려됨", "status.leave.rejected");
    }

    public static String getStatusKey(String dbStatus) {
        return statusMap.getOrDefault(dbStatus, "default.status");
    }
}
