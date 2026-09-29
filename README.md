# 커뮤니티 프로젝트

## 목차
- [v0.1 - 순수 자바 콘솔 앱](#v01---순수-자바-콘솔-앱)
- [v0.2 - 스프링 부트 전환](#v02---스프링-부트-전환)
- [v0.3 - REST API, 서비스 계층, DTO](#v03---rest-api-서비스-계층-dto)
- [v0.4 - JDBC + MySQL](#v04---jdbc--mysql)

## v0.1 - 순수 자바 콘솔 앱

### 문제
- 회원가입: id 자동 부여, 이메일 중복 방지, 회원 정보 저장이 필요했음
- 게시글: 회원가입 후 게시글을 작성/저장할 방법이 없었음
- 구조: MemberService, PostService가 각자 내부에서 `new`로 저장소를 직접 생성해서 사용

### 원인
- 서비스가 저장소를 스스로 생성하다 보니, 서로 다른 서비스가 같은 저장소 인스턴스를 공유할 수 없었음
- 예: PostService가 회원 존재 여부를 확인하려면 MemberRepository가 필요한데, 직접 new하면 MemberService가 쓰는 것과 다른 인스턴스가 생겨버림

### 해결
- `Member`, `MemberRepository`, `MemoryMemberRepository`, `MemberService` 구현 — HashMap 저장, id 순차 부여, 이메일 중복 시 예외 발생
- `Post`, `PostRepository`, `MemoryPostRepository`, `PostService` 구현 — 작성자는 Member 객체 대신 memberId로 참조 (추후 DB 외래키 대응 고려)
- `MemberService`, `PostService`를 생성자 주입 방식으로 전환, 재할당되지 않는 필드에 `final` 적용
- `AppConfig` 도입 — 객체 생성·연결 책임을 분리하고, 각 인스턴스를 필드에 캐싱해 항상 같은 인스턴스를 재사용하도록 구현

### 결과
- Main에서 회원가입 2건 성공 + 중복 가입 1건 예외 처리, 게시글 작성 2건 확인
- 컴파일/실행 정상 동작 확인
- 서비스 간 저장소 인스턴스 공유가 가능해져, 이후 PostService가 memberRepository()로 실제 회원 존재 여부를 검증할 수 있는 기반 마련
- 지금은 AppConfig가 수동으로 하는 객체 생성/연결을, v0.2에서 스프링 DI 컨테이너가 자동으로 대체할 예정

## v0.2 - 스프링 부트 전환

### 문제
- v0.1에서 `AppConfig`로 객체를 수동으로 만들고 연결하던 게 번거로웠음
- `Member`, `Post`의 getter/setter, 생성자 같은 보일러플레이트를 매번 손으로 작성해야 했음
- 로그가 하나도 없어서 무슨 일이 일어나는지 콘솔 출력(`println`)에만 의존하고 있었음

### 원인
- 순수 자바로 DI 컨테이너를 흉내 내다 보니 관리 부담이 컸고, 자바 자체는 반복 코드를 줄여주는 기능이 없음
- 로깅을 도입하면 "시작 로그 - 로직 - 종료 로그" 패턴이 메서드마다 반복될 수밖에 없는 구조였음

### 해결
- **스프링 부트 전환**: `build.gradle`에 스프링 부트 플러그인 + `io.spring.dependency-management` 플러그인 추가, Java 17 툴체인 설정, `spring-boot-starter` 추가. `Main`에 `@SpringBootApplication` + `SpringApplication.run()` 적용
- **컴포넌트 스캔 도입**: `MemberService`/`PostService`에 `@Service`, `MemoryMemberRepository`/`MemoryPostRepository`에 `@Repository`를 붙여 스프링이 자동으로 빈을 등록·연결하게 하고, 수동 조립하던 `AppConfig` 삭제
- **롬복 도입**: `compileOnly` + `annotationProcessor`로 의존성 추가 (IntelliJ "Add Starters" 기능이 넣어준 `implementation` 스코프는 애노테이션 프로세서로 동작하지 않아 직접 수정). `@Getter`로 getter 대체(안 쓰는 setter는 만들지 않음), `Member`는 `@AllArgsConstructor`로 생성자 대체, `Post`는 `updatedAt` 초기화에 커스텀 로직이 있어 수동 생성자 유지, 서비스 계층은 `@RequiredArgsConstructor`로 생성자 주입 코드 대체
- **로거 + AOP**: 먼저 `MemberService`, `PostService`에 로그를 수동으로 중복 작성해보며 불편함을 확인한 뒤, `TimeLoggingAspect`(`@Aspect` + `@Around`)로 로깅을 한 곳에 모음. `spring-boot-starter-aop`가 Spring Boot 4.1.1로는 배포되지 않는 걸 확인하고 `aspectjweaver`만 직접 추가. `try-finally`로 감싸 예외 발생 시에도 종료 로그가 남도록 보강. pointcut을 패키지 전체(`execution`)로 시작했다가 리포지토리 계층까지 잡히는 걸 확인하고, `@within(Service)`로 서비스 계층만 잡도록 범위를 좁힘

### 결과
- 클린 빌드 + `bootRun`으로 실행 확인, 회원가입/게시글 작성 결과가 이전과 동일하게 출력됨
- 서비스 코드에서 더 이상 반복되는 조립 코드(`AppConfig`)나 로깅 코드가 없어짐 — 새 도메인이 추가돼도 애노테이션만 붙이면 스프링과 AOP가 알아서 처리함
- 롬복이 모든 보일러플레이트를 대체해주진 않는다는 것(커스텀 로직이 있는 `Post` 생성자)과, AOP의 pointcut 범위는 의도한 만큼 정확히 좁혀야 한다는 것을 직접 겪어봄

## v0.3 - REST API, 서비스 계층, DTO

### 문제
- 콘솔 앱이라 웹에서 쓸 수 없었음
- 엔티티(`Member`, `Post`)를 그대로 요청/응답에 쓰면, 클라이언트가 몰라도 될 `id`까지 보내야 하고 `password` 같은 민감한 값이 응답에 노출됨
- v0.1부터 미뤄뒀던 "존재하지 않는 memberId로 게시글이 만들어지는 문제"도 해결 안 된 상태였음

### 원인
- 엔티티는 내부 도메인 표현이고 API 요청/응답 모양(계약)은 별개인데, 이 둘을 분리하지 않고 엔티티를 그대로 썼기 때문
- `PostService`가 `MemberRepository`에 접근할 수단이 없어서 회원 존재 여부를 검증할 방법이 없었음

### 해결
- `spring-boot-starter-web` 추가
- `member`, `post` 패키지를 각각 `controller`/`service`/`repository`/`dto`로 계층 분리 (도메인 우선 구조 유지)
- 요청 DTO(`MemberSignupRequest`, `PostCreateRequest`), 응답 DTO(`MemberSignupResponse`, `PostResponse`) 도입 — 엔티티 직접 노출 제거
- **요청 DTO는 `@NoArgsConstructor` + `@Setter`, 응답 DTO는 `@Builder`**로 다르게 구성 — Jackson이 JSON을 객체로 만드는 방식(기본 생성자로 빈 객체 생성 후 값 채움)과 우리가 코드로 직접 객체를 만드는 방식이 다르다는 걸 실제 500 에러(`Cannot construct instance... no Creators`)를 겪고 확인함
- `POST /members`, `POST /posts` 엔드포인트 구현
- `PostService`에 `MemberRepository`를 주입받아 `memberId` 존재 여부 검증, **저장 전에 검증**하도록 순서 정리 (검증 전에 저장부터 하면 잘못된 데이터가 저장소에 남는 문제를 직접 겪고 수정)
- `Main`의 콘솔 데모 코드 제거, IntelliJ HTTP Client(`http/*.http`)로 테스트하는 방식으로 전환
- 엔티티 → DTO 변환은 서비스 계층에 두기로 결정 (컨트롤러로 옮기는 대안도 검토했으나, 지금은 재사용 필요성이 없어 YAGNI 원칙에 따라 보류)

### 결과
- 회원가입 시 `password` 없이 `id`/`email`/`nickname`만 응답으로 내려오는 것 확인
- 게시글 작성 시 작성자 닉네임까지 포함된 응답 확인, 존재하지 않는 `memberId`로는 게시글이 저장되지 않는 것 확인
- 중복 이메일 가입 시도 시 `500 Internal Server Error`로 뭉뚱그려 응답되는 문제를 재확인 — 적절한 상태 코드와 에러 메시지 처리는 v0.10(Bean Validation, 전역 예외 처리)에서 다룰 예정

## v0.4 - JDBC + MySQL

### 문제
- 서버를 재시작하면 `HashMap`에 저장해뒀던 회원/게시글 데이터가 전부 사라짐

### 원인
- 저장소가 인메모리(`HashMap`)였어서 JVM이 꺼지면 데이터도 함께 사라지는 구조였음

### 해결
- 로컬 도커 MySQL에 `community` DB 생성, `members`(`id` AUTO_INCREMENT, `email` UNIQUE), `posts`(`content`는 `TEXT`, `member_id`는 `members(id)`를 참조하는 FOREIGN KEY) 테이블 설계
- `mysql-connector-j` 드라이버 추가(`runtimeOnly`), `application.yml`에 datasource 접속 정보 설정
- `MemoryMemberRepository`, `MemoryPostRepository`를 삭제하고 `JdbcMemberRepository`, `JdbcPostRepository`로 교체 — raw JDBC(`DriverManager`, `PreparedStatement`, `ResultSet`)로 직접 구현
- **일부러 커넥션 풀 없이** 매 호출마다 `DriverManager.getConnection()`으로 새로 연결하는 방식으로 구현 (v0.5에서 Spring Data JDBC + 커넥션 풀로 해결할 문제를 먼저 체감하기 위해)
- `id` 발급 책임을 애플리케이션(수동 카운터)에서 DB(`AUTO_INCREMENT`)로 이관
- INSERT 시 `Statement.RETURN_GENERATED_KEYS` + `getGeneratedKeys()`로 생성된 `id`를 받아오되, 이 `ResultSet`엔 **생성된 키 컬럼만** 들어있고 나머지 컬럼은 없다는 걸 직접 겪고 확인 — 나머지 필드는 저장 전 값을 그대로 사용
- `Connection`/`PreparedStatement`/`ResultSet`은 전부 try-with-resources(필요시 중첩)로 자동 close
- `LocalDateTime`은 `setObject`/`getObject`로 바인딩·조회
- DB 컬럼은 스네이크 케이스(`member_id`, `created_at`), 자바 필드는 카멜케이스(`memberId`, `createdAt`) — 지금은 자동 매핑이 없어서 쿼리 작성/조회 시 직접 이어줘야 한다는 것 확인

### 결과
- 회원가입/게시글 작성 API로 데이터 생성 확인, DB에서 실제 저장 확인
- 서버를 재시작한 뒤 같은 이메일로 재가입을 시도하면 "이미 존재하는 이메일입니다" 응답이 오는 것으로 데이터가 영구 저장됨을 검증
- 반복되는 `Connection`/`PreparedStatement` 보일러플레이트와 매번 새 연결을 만드는 비효율을 직접 겪음 — v0.5(Spring Data JDBC, 커넥션 풀)의 필요성을 체감