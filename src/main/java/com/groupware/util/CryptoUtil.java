package com.groupware.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

/** 비밀번호는 복호화하지 않고 일치 여부만 확인합니다. */
public class CryptoUtil {
    public static String encrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean matches(String password, String stored) {
        if (password == null || stored == null || stored.isBlank()) return false;
        if (stored.startsWith("$2")) {
            try { return BCrypt.checkpw(password, stored); }
            catch (IllegalArgumentException e) { return false; }
        }
        // 원본의 테스트 데이터는 최초 로그인 후 해시로 바뀝니다.
        return password.equals(stored);
    }
}
