# Genie Backend

Spring Boot 백엔드 프로젝트입니다.

## 🛠️ 사용된 기술 스택

-   **Framework**: Spring Boot
-   **Build Tool**: Maven
-   **Database**: MariaDB
-   **Language**: Java
-   **IDE**: IntelliJ IDEA
-   **ORM**: JPA/Hibernate
-   **API Documentation**: Swagger
-   **Payment**: Toss Payments

## 🚀 설치 및 실행 방법

### 1. 데이터베이스 생성

데이터베이스를 생성하세요:

```sql
-- 데이터베이스 생성
CREATE DATABASE genieqlocal;
```

### 2. properties 파일 설정

`src/main/resources/` 폴더에 파일을 추가하세요:

```properties
application.properties
application-private.properties
application-toss.properties
```

### 3. Maven JAR 빌드

IntelliJ IDEA에서:

1. Maven 탭 열기
2. Lifecycle → clean → package 순서로 실행

![maven_spring_boot_jar_build (1).png](etc/maven_spring_boot_jar_build%20%281%29.png)
