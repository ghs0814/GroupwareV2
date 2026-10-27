# Groupware

사내 회의실 예약, 비품 대여, 휴가 결재와 게시판을 하나로 관리하는 웹 그룹웨어입니다.

연수원에서 진행한 JSP·Servlet 기반 프로젝트를 바탕으로, 기존 화면과 업무 흐름을 유지하면서 Spring Boot와 Oracle DB 환경으로 구성했습니다. 사원은 개인 업무 내역과 결재 상태를 확인하고, 관리자는 사원 정보와 공용 자원을 관리할 수 있습니다.

## 주요 기능

| 구분 | 기능 |
|---|---|
| 계정 | 사원 계정 등록, 로그인·로그아웃, 비밀번호 찾기·변경 |
| 대시보드 | 개인별 예약·대여·휴가 내역, 달력, 기안 문서 조회 |
| 회의실 | 층별 평면도, 시간대별 예약 조회, 5분 임시 선점, 예약 확정·취소 |
| 비품 | 비품 목록 조회, 대여 신청, 단계별 승인·반려, 반납 및 재고 관리 |
| 휴가 | 휴가 신청, 직급별 승인·반려, 주말을 제외한 사용 일수 계산, 잔여 연차 관리 |
| 게시판 | 사내소식·자유·건의 게시판, 검색, 첨부파일, 댓글·답글 |
| 주소록 | 사원 연락처 조회 |
| 관리자 | 사원 정보·부서·직급 관리, 관리자 권한 설정, 퇴사 처리, 회의실·비품 관리 |



## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 언어 | Java 17, JavaScript |
| 서버 | Spring Boot 4.1.1, Spring MVC, 내장 Tomcat |
| 화면 | JSP, JSTL, HTML, CSS |
| 데이터베이스 | Oracle Database 19c |
| DB 연동 | JDBC, Spring JDBC, HikariCP |
| 빌드 | Maven, Maven Wrapper |
| 테스트 | JUnit 5, Spring Boot Test |

## 프로젝트 구조

```text
groupware-springboot/
├─ src/main/java/com/groupware/
│  ├─ GroupwareApplication.java
│  ├─ config/          # 웹 설정, 인증·권한 확인
│  ├─ controller/      # 요청 처리와 화면 연결
│  ├─ service/         # 예약·결재·파일 등 업무 처리
│  ├─ dao/             # SQL 실행 및 데이터 조회·저장
│  ├─ dto/             # 데이터 전달 객체
│  └─ util/            # 비밀번호·상태값 처리
├─ src/main/resources/ # 애플리케이션 설정, 메시지, 정적 리소스
├─ src/main/webapp/WEB-INF/views/ # JSP 화면
├─ src/test/           # 단위·통합 테스트
├─ config/             # 로컬 DB 설정 예시
├─ sql/                # 계정·테이블·트리거·샘플 데이터
└─ docs/               # 실행 안내 및 개발 문서
```

요청은 Controller에서 받고, 예약이나 결재처럼 여러 데이터를 함께 변경하는 작업은 Service에서 처리합니다. DAO는 SQL을 실행하며, JSP는 조회한 데이터를 화면에 표시합니다.

## 실행 방법

JDK 17과 접속 가능한 Oracle 19c가 필요합니다. 처음 실행할 때는 DB 계정과 테이블을 생성하고 로컬 접속 정보를 설정합니다.

### 1. 데이터베이스 준비

SQL Developer에서 SYSTEM 계정으로 대상 PDB에 연결해 `sql/00-create-user.sql`을 실행합니다. 이후 생성된 `GROUPWARE_BOOT` 계정으로 접속해 아래 순서로 실행합니다.

1. `sql/01-schema.sql`
2. `sql/03-triggers.sql`
3. `sql/02-seed-ja.sql` 또는 `sql/02-seed-ko.sql` 중 하나
4. `sql/04-verify.sql`

SQL Developer의 **스크립트 실행(F5)** 을 사용합니다. 초기화 스크립트는 비어 있는 프로젝트 계정에 한 번만 적용합니다. 화면에는 한국어·일본어 문구가 함께 있으며, 샘플 데이터는 두 언어 중 하나를 선택할 수 있습니다.

### 2. DB 접속 정보 설정

`config/application-local.properties.example`을 같은 폴더의 `application-local.properties`로 복사하고 접속 정보를 입력합니다.

```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1522/ORCLPDB1
spring.datasource.username=GROUPWARE_BOOT
spring.datasource.password=프로젝트_계정_비밀번호
```

로컬 설정 파일은 Git에서 제외됩니다. 환경에 따라 호스트, 포트, 서비스 이름을 변경합니다.

### 3. 애플리케이션 실행

1. IntelliJ에서 `pom.xml`이 있는 프로젝트 폴더를 엽니다.
2. Project SDK를 **JDK 17**로 설정하고 Maven 의존성을 불러옵니다.
3. Oracle DB가 실행 중인지 확인합니다.
4. `GroupwareApplication.java`의 `main` 메서드를 실행합니다.
5. 브라우저에서 [http://localhost:8080/Groupware/](http://localhost:8080/Groupware/)에 접속합니다.

실행 구성의 Working directory는 프로젝트 폴더로 지정합니다. DB가 이미 준비되어 있다면 다음 실행부터는 초기 SQL을 다시 실행할 필요가 없습니다.

### 샘플 계정

| 구분 | 사번 | 비밀번호 |
|---|---:|---|
| 관리자 | 1 | 1234 |
| 일반 사원 | 5 | 1234 |
| 개발팀 사원 | 25 | 1234 |

위 계정은 초기 데이터에 포함된 테스트용 계정입니다.

## 빌드 및 테스트

프로젝트 폴더에서 실행합니다. Maven Wrapper를 포함하므로 Maven을 별도로 설치하지 않아도 됩니다.

```powershell
# 개발 서버 실행
.\mvnw.cmd spring-boot:run

# 빌드 및 단위 테스트
.\mvnw.cmd clean package

# 빌드 결과 실행
java -jar target\groupware.war
```

실제 Oracle DB를 사용하는 통합 테스트는 DB 접속 설정 후 실행합니다.

```powershell
$env:RUN_ORACLE_TESTS="true"
.\mvnw.cmd verify
```

