# Genie Backend

Spring Boot 백엔드 프로젝트입니다.

GENIE 는 GenieQ 를 리팩토링하여 개발된 서비스입니다.

GenieQ 서비스는 다음 repo에서 확인하실 수 있습니다: 
- [GenieQ OverView](https://github.com/ChunJae-Full-Stack-FinalProject/GenieQ-overview)
- [GenieQ github repo 바로가기](https://github.com/ChunJae-Full-Stack-FinalProject/2nd_GenieQ_BackEnd)

## 🛠️ 사용된 기술 스택

-   **Framework**: Spring Boot
-   **Build Tool**: Maven
-   **Database**: MariaDB
-   **Language**: Java
-   **IDE**: IntelliJ IDEA
-   **ORM**: JPA/Hibernate
-   **API Documentation**: Swagger
-   **Payment**: Toss Payments

## GENIE 아키텍처

![GENIE-아키텍처.webp](docs/GENIE-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98.webp)

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
