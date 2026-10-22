package com.groupware;
import com.groupware.util.CryptoUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {
    @Test
    void hashesAreSaltedAndCanVerifyUnicodePasswords() {
        String password="비밀번호-日本語-1234";
        String first=CryptoUtil.encrypt(password),second=CryptoUtil.encrypt(password);
        assertNotEquals(first,second);assertNotEquals(password,first);
        assertTrue(CryptoUtil.matches(password,first));assertFalse(CryptoUtil.matches("wrong",first));
        assertFalse(CryptoUtil.matches(password,null));assertFalse(CryptoUtil.matches(password,"$2bad"));
    }
    @Test
    void originalDemoPasswordsCanBeUpgradedAtLogin() {
        assertTrue(CryptoUtil.matches("1234","1234"));
        String upgraded=CryptoUtil.encrypt("1234");assertTrue(CryptoUtil.matches("1234",upgraded));
        assertFalse(CryptoUtil.matches("5678",upgraded));
    }
}
