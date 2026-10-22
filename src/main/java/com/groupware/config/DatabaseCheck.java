package com.groupware.config;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCheck implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    public DatabaseCheck(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override
    public void run(ApplicationArguments args) {
        try { jdbc.queryForObject("SELECT COUNT(*) FROM EMPLOYEE",Integer.class); }
        catch(Exception exception) {
            throw new IllegalStateException("Oracle 컨테이너, config/application-local.properties 및 README의 최초 DB 설정을 확인해주세요.",exception);
        }
    }
}
