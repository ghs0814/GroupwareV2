# Groupware — Spring Boot + JSP + Oracle

연수원 JSP·Servlet 그룹웨어를 Spring Boot로 전환한 프로젝트입니다. 기존 JSP 화면, CSS, 평면도 이미지, `.do` 요청 주소와 주요 기능을 유지합니다.

- Java 17 / Spring Boot 4.1.1 / Maven Wrapper
- JSP / JSTL / 내장 Tomcat
- Oracle 19c / JDBC / HikariCP
- Controller → Service → DAO → DTO 구조

## 지금 PC에서 실행하기

이 PC의 전용 계정과 초기 데이터는 준비되어 있습니다. `config/application-local.properties`도 로컬에 만들어져 있습니다.

1. Docker Desktop을 실행합니다.
2. PowerShell에서 Oracle 컨테이너를 켭니다.

   ```powershell
   docker start oracle19c
   ```

   실제 컨테이너 이름은 `oracle11g`가 아니라 `oracle19c`입니다. DB가 준비되기까지 잠시 걸릴 수 있습니다.

3. IntelliJ에서 **이 README와 pom.xml이 있는 `groupware-springboot` 폴더**를 엽니다. Maven 프로젝트로 가져오고, Project SDK는 **JDK 17**을 선택합니다. 처음에는 라이브러리 다운로드가 끝날 때까지 기다립니다.
4. `src/main/java/com/groupware/GroupwareApplication.java`를 열고 `main` 왼쪽의 **▶ Run**을 누릅니다. 저장된 `Groupware` 실행 구성을 선택해도 됩니다.
5. 브라우저에서 다음 주소를 엽니다.

   **http://localhost:8080/Groupware/**

SQL Developer를 별도로 켜 둘 필요는 없습니다. Oracle 컨테이너는 켜져 있어야 합니다. 다음 실행부터는 DB 스크립트를 다시 실행하지 않습니다.

## 테스트 로그인

| 용도 | 사번 | 비밀번호 | 직급 |
|---|---:|---|---:|
| 관리자·대표 | 1 | 1234 | 5 |
| 일반 사원 | 5 | 1234 | 1 |
| 개발팀 사원 | 25 | 1234 | 1 |

위 값은 공개 가능한 샘플 계정입니다. 샘플 비밀번호는 첫 로그인 때 BCrypt 해시로 바뀝니다. 사전 등록된 사원의 비밀번호가 비어 있으면 계정 등록 화면에서 등록할 수 있습니다.

기존 화면에는 일본어로 직접 작성된 문구와 한국어·일본어 메시지 파일이 함께 있습니다. 화면의 언어 구성은 원본을 유지했습니다. 기본 데이터는 원본 일본어 샘플을 사용하며 한국어 샘플도 제공합니다.

## 다른 PC에서 처음 설정하기

### 1. 준비

- JDK 17, IntelliJ, 접속 가능한 Oracle 19c
- 처음 Maven 라이브러리를 받기 위한 인터넷 연결
- 기본 DB 주소: `localhost:1522/ORCLPDB1`

현재 SQL 스크립트는 실제 확인한 **Oracle 19c**를 기준으로 합니다.

### 2. 전용 DB 계정과 테이블 만들기

SQL Developer에서 **SYSTEM 계정으로 ORCLPDB1에 연결**한 뒤 `sql/00-create-user.sql`을 **스크립트 실행(F5)** 합니다. 프로젝트 계정에 쓸 비밀번호를 입력합니다.

생성하는 계정은 `GROUPWARE_BOOT`이며, 권한은 `CREATE SESSION`, `CREATE TABLE`, `CREATE SEQUENCE`, `CREATE TRIGGER`입니다. `USERS` 테이블스페이스 사용 한도는 50MB입니다. 기존 계정을 삭제하거나 비밀번호를 변경하지 않습니다.

다음으로 SQL Developer에 `GROUPWARE_BOOT` 연결을 만들고, **그 연결에서** 아래 순서로 실행합니다.

1. `sql/01-schema.sql`
2. `sql/03-triggers.sql`
3. `sql/02-seed-ja.sql` 또는 `sql/02-seed-ko.sql` 중 **하나만**
4. `sql/04-verify.sql`

초기 스크립트는 비어 있는 프로젝트 계정에 한 번만 적용합니다. 실행 중인 데이터베이스를 자동으로 비우는 기능은 없습니다.

### 3. 로컬 접속 설정

`config/application-local.properties.example`을 같은 폴더의 `application-local.properties`로 복사하고 새 프로젝트 계정의 비밀번호를 입력합니다.

```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1522/ORCLPDB1
spring.datasource.username=GROUPWARE_BOOT
spring.datasource.password=프로젝트_계정_비밀번호
```

이 파일은 Git에서 제외됩니다. 환경변수를 사용하려면 로컬 설정 파일 대신 `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`를 설정하면 됩니다.

## 제공 기능

- 계정 등록, 로그인·로그아웃, 비밀번호 찾기·변경
- 회의실 평면도, 예약 현황, 5분 임시 선점, 예약 확정·취소
- 비품 조회·대여, 단계별 승인·반려, 반납과 재고 복구
- 주말을 제외한 휴가 신청, 직급별 승인·반려, 최종 승인 시 연차 차감
- 기안 문서함, 달력, 개인별 예약·대여·휴가 내역
- 사내소식·자유·건의 게시판, 검색, 파일 첨부·다운로드, 댓글·답글
- 사원·부서·직급·관리자 권한·퇴사 처리, 주소록, 회의실·비품 관리
- 휴가·대여 이력의 Oracle 백업 트리거

## 코드 읽는 순서

1. `GroupwareApplication.java`: 실행 시작점
2. `controller`: 화면 요청과 응답
3. `service/ReservationService.java`: 예약 선점·소유자 확인
4. `service/WorkflowService.java`: 결재·재고·연차 트랜잭션
5. `dao`: 기존 SQL 기반 조회와 기본 저장
6. `dto`: 데이터 전달 객체
7. `src/main/webapp/WEB-INF/views`: JSP 화면

원본에서 발견한 문제와 수정 내역은 [전환 내역](MIGRATION.md)에 정리했습니다.

## 명령으로 실행·빌드하기

프로젝트 폴더에서 실행합니다. 별도로 Maven을 설치할 필요는 없습니다.

```powershell
.\mvnw.cmd spring-boot:run
```

실행 가능한 WAR 만들기:

```powershell
.\mvnw.cmd clean package
java -jar target\groupware.war
```

WAR 실행도 프로젝트 폴더에서 시작하면 `config`와 `data` 경로를 그대로 사용합니다. 이 프로젝트는 IntelliJ의 main 실행과 내장 Tomcat 실행을 기준으로 검증합니다.

## 테스트

일반 빌드에서는 비밀번호 단위 테스트를 실행합니다. 실제 Oracle을 사용하는 통합 테스트는 다음과 같이 실행합니다.

```powershell
$env:RUN_ORACLE_TESTS="true"
.\mvnw.cmd verify
```

통합 테스트는 별도의 테스트 사원·비품·회의실·문서를 만들고 자신이 만든 행을 정리합니다. 검증 항목과 결과는 [검증 기록](VERIFICATION.md)을 참고하세요.

## GitHub에 올리기

**원본 폴더가 아닌 이 `groupware-springboot` 폴더를 새 저장소로 올리면 됩니다.**

Git으로 커밋하면 로컬 비밀번호 파일(`config/application-local.properties`), 첨부파일 저장소(`data`), 빌드 결과(`target`), IntelliJ 개인 설정은 `.gitignore`에 따라 제외됩니다. `.run/Groupware.run.xml`과 Maven Wrapper는 함께 올립니다.

GitHub 웹 화면에 파일을 직접 끌어놓는 방식은 `.gitignore`를 자동 적용하지 않습니다. Git 또는 GitHub Desktop으로 올리세요. GitHub 업로드 자체는 별도 요청을 받기 전까지 수행하지 않습니다.

## 실행이 안 될 때

- **DB 접속 실패**: Docker Desktop과 `oracle19c`가 켜졌는지, 포트 1522와 서비스 이름 `ORCLPDB1`, 로컬 비밀번호 파일을 확인합니다.
- **8080 포트 사용 중**: 이미 실행한 서버를 IntelliJ의 ■ Stop으로 종료하거나 환경변수 `SERVER_PORT`를 바꿉니다.
- **JDK 오류**: IntelliJ의 Project SDK와 Maven 실행 JDK를 17로 맞춥니다.
- **설정 파일을 못 찾음**: 실행 구성의 Working directory를 이 프로젝트 폴더로 설정합니다.
- **첨부파일**: 실제 파일은 `data/uploads/board`에 저장됩니다. 원본 저장소에 파일 본체가 없어서 기존 더미 첨부 목록 대신 실제 다운로드 가능한 샘플 파일 하나를 제공합니다.
