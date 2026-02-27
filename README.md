# B2B Trade - B2B 무역 구매·배송 통합 플랫폼

Spring Boot 기반 B2B 무역 거래 End-to-End 통합 관리 플랫폼

본 프로젝트는 분산 관리되던 무역 거래 프로세스를  
End-to-End로 통합 관리하기 위해 설계된 4인 팀 프로젝트입니다.

관리자 / 기업 / 개인 3계층 ROLE 기반 접근 제어와  
Spring Security FilterChain 중심 인증·인가 구조를 적용하여  
보안 정책과 비즈니스 로직의 책임을 분리하였습니다.

상품 → 견적 → 계약 → 결제(KakaoPay) → 배송까지  
상태 코드 기반 흐름 관리 구조로 설계되었습니다.

---

## 개발 프로세스 (WBS 기반 진행)

### 1단계: 분석 및 설계
- 요구사항 정의 및 기능 목록 도출
- 사용자 역할(관리자/기업/개인) 정의
- ERD 설계 및 공통 코드 테이블 구조 설계
- 화면 설계서 작성

### 2단계: 핵심 기능 개발
- 인증·인가 구조 설계 (Spring Security)
- 상품 → 견적 → 계약 → 결제 → 배송 흐름 구현
- KakaoPay API 연동
- 소셜 로그인(OAuth2) 연동

### 3단계: 모듈별 세부 구현
- 상품/재고 관리
- 주문·계약 관리
- 배송 상태 관리
- 게시판(공지/FAQ/QNA)
- 알림 시스템(AJAX 기반)

### 4단계: 테스트 및 검증
- 단위 테스트
- 역할별 접근 제어 테스트
- 주문-결제-배송 통합 시나리오 테스트
- 오류 수정 및 리팩토링

### 5단계: 배포 및 운영 환경 구성
- AWS 기반 배포
- 운영 DB 환경 구성
- 통합 테스트 및 안정화

---

## 기술 스택

### Backend
- Spring Boot 3.5.4
- Spring Security 6.5.2
- Java 21
- MySQL 8.0
- MyBatis

### Frontend
- JSP
- Bootstrap
- jQuery
- AJAX

### Infra
- AWS (Lightsail / EC2)

### External API
- Kakao Address API
- KakaoPay API
- Kakao / Naver OAuth2 Login
- JavaMail (SMTP)
- Public Holiday API

---

## 배포 환경
- Application Server: AWS Lightsail
- Database Server: AWS EC2 (MySQL 8.0)
- OS: Ubuntu

※ 프로젝트 당시 운영 환경 기준이며, 현재는 로컬 환경 기준으로 실행 가능합니다.

---

## 시스템 구조

### Request Flow (with Spring Security)
Browser(Client) → Security FilterChain → DispatcherServlet → Controller → Service → Mapper(MyBatis) → Database

### Layered Architecture
Controller → Service → Mapper(MyBatis) → Database

(아키텍처 다이어그램 삽입 요망)

---

## 주요 기능

### 1. 인증 및 권한 관리 (Spring Security)
- SecurityFilterChain 기반 인증·인가 중앙 처리
- ROLE_ADMIN / ROLE_BIZ / ROLE_PERSONAL 권한 체계 및 URL 패턴 접근 제어(/admin/**, /biz/**, /personal/**)
- DB 권한 코드 → ROLE 매핑 구조(CustomUserDetails 기반)
- 로그인 성공/실패 핸들러 기반 보안 이벤트 처리(로그인 이력 저장 등)

### 2. 회원 관리
- 일반 회원가입/로그인 및 회원 정보 관리
- Kakao / Naver OAuth2 소셜 로그인 연동
- 회원 상태 코드 기반 계정 관리(활성/정지 등)
- 역할(기업/개인)에 따른 메뉴 및 화면 분기 처리

### 3. 상품 및 재고 관리
- 상품 등록/수정/삭제
- 재고 수량 기반 상태 자동 변경(판매중/품절 등)
- 장바구니 및 찜 기능
- 상태 코드(공통 코드 테이블) 기반 상품/상태 관리

### 4. 견적 및 계약 프로세스
- 상품 기반 견적 요청 및 견적서 생성
- 계약서 작성 및 계약 단계별 상태 관리
- 계약금/잔금 분리 결제 구조
- 주문 → 견적 → 계약 → 결제 흐름을 단계별 코드로 관리

### 5. 결제 시스템
- KakaoPay API 연동
- 결제 승인/실패 처리 및 결제 내역 저장
- 결제 결과에 따른 계약 상태 자동 변경

### 6. 배송 및 컨테이너 관리
- 계약 완료 건에 대한 배송 등록 및 조회
- 컨테이너 단위 운송 정보 관리
- 배송 상태 단계별 관리(출고/운송중/도착 등)
- 교환/반품 신청 및 승인 프로세스

### 7. 게시판 및 알림
- 공지/FAQ/QNA CRUD 및 권한 기반 답변 처리
- 배송·계약 상태 변경 이벤트 기반 알림 생성
- AJAX 기반 헤더 알림 배지 및 읽음 처리

---

## ERD

(ERD 이미지 삽입 요망)

---

## 실행 방법 (로컬 환경 기준)

1. MySQL 8.0 설치 후 데이터베이스 생성
2. 프로젝트에 포함된 SQL 스크립트 실행
3. application.properties에 DB 및 API 키 정보 입력
4. Spring Boot 애플리케이션 실행
5. ROLE별 테스트 계정으로 기능 확인

---

## 팀 구성 및 역할

| 이름 | 담당 |
|------|------|
| 이현호 | 인증·인가·배송·알림·게시판 |
| 송성인 | 회원·소셜 로그인 |
| 노민혁 | 주문·결제·계약 |
| 황귀환 | 상품·재고·장바구니 |

---

## License
본 프로젝트는 교육 및 포트폴리오 목적으로 제작되었습니다.  
상업적 사용은 허용되지 않습니다.
