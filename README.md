# 커뮤니티 프로젝트

## 목차
- [v0.1 - 순수 자바 콘솔 앱](#v01---순수-자바-콘솔-앱)
- [v0.2 - 스프링 부트 전환](#v02---스프링-부트-전환)
- [v0.3 - REST API, 서비스 계층, DTO](#v03---rest-api-서비스-계층-dto)
- [v0.4 - JDBC + MySQL](#v04---jdbc--mysql)
- [v0.5 - Spring Data JDBC, 커넥션 풀](#v05---spring-data-jdbc-커넥션-풀)
- [v0.6 - JPA, 영속성 컨텍스트](#v06---jpa-영속성-컨텍스트)
- [v0.7 - 댓글, 연관관계, N+1 해결](#v07---댓글-연관관계-n1-해결)
- [v0.8 - 트랜잭션](#v08---트랜잭션)
- [v0.9 - 페이징, QueryDSL](#v09---페이징-querydsl)

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
- 
## v0.5 - Spring Data JDBC, 커넥션 풀

### 문제
- raw JDBC로 직접 구현하면서 Connection/PreparedStatement/ResultSet을 다루는 반복 코드가 많았음
- 매번 `DriverManager.getConnection()`으로 새 커넥션을 만드는 비효율

### 원인
- JDBC API를 직접 쓰면 CRUD 메서드마다 연결 생성, try-with-resources, 예외 처리가 반복됨
- 커넥션을 재사용하는 장치(풀)가 없었음

### 해결
- `spring-boot-starter-data-jdbc` 추가 — HikariCP 커넥션 풀이 기본으로 포함됨
- `Member`, `Post`에 `@Id` 적용, 실제 테이블명(`members`, `posts`)이 기본 네이밍 규칙과 달라 `@Table(name = "...")`로 명시
- `JdbcMemberRepository`/`JdbcPostRepository`(raw JDBC 구현체) 삭제, `MemberRepository`/`PostRepository`가 `CrudRepository<T, Long>`를 상속하는 인터페이스만으로 기본 CRUD를 자동 제공받음
- `existsByEmail`처럼 메서드 이름 규칙으로 쿼리 자동 생성
- `findNicknameById`처럼 특정 컬럼만 반환하는 커스텀 쿼리는 이름 규칙으로 표현이 안 된다는 것을 직접 겪음(`Couldn't find PersistentEntity for type class java.lang.String`) → `CrudRepository`가 기본 제공하는 `findById`(`Optional<Member>`)로 대체하고 `orElseThrow`로 처리

### 결과
- `Connection`/`PreparedStatement`/`ResultSet` 관련 코드가 리포지토리에서 완전히 사라지고 인터페이스 선언만 남음
- HikariCP 로그로 실제 커넥션 풀이 적용된 것 확인
- 회원가입/게시글 작성/중복 이메일 검증 모두 정상 동작 확인

## v0.6 - JPA, 영속성 컨텍스트

### 문제
- Spring Data JDBC로도 CRUD는 됐지만, 매 쿼리마다 엔티티를 새로 조회/저장하는 방식이라 객체를 관계형 데이터처럼 계속 변환해줘야 했음

### 원인
- Spring Data JDBC는 "영속성 컨텍스트"(객체를 계속 추적하고 관리하는 1차 캐시 같은 공간) 없이, 매번 DB와 직접 값을 주고받는 방식이라 객체 상태를 계속 테이블 구조에 맞춰 변환해야 함

### 해결
- `spring-boot-starter-data-jpa` 추가 (Hibernate 포함)
- `Member`, `Post`를 JPA 엔티티로 전환 (`@Entity`, `@Table`, `@Id`, `@GeneratedValue(IDENTITY)`)
- JPA 엔티티는 완전한 불변 객체로 만들 수 없다는 제약을 직접 겪음 — Hibernate가 리플렉션으로 빈 객체를 만든 뒤 필드를 채우는 방식이라 기본 생성자가 필요함. `final` 필드 제거, `@NoArgsConstructor(access = PROTECTED)`로 타협 (애플리케이션 코드에서 `new`로 빈 엔티티를 만드는 건 막되 JPA는 접근 가능하게)
- `MemberRepository`, `PostRepository`를 `CrudRepository`에서 `JpaRepository`로 전환
- **게시글 수정(`PATCH /posts/{id}`) 기능으로 영속성 컨텍스트의 변경 감지(더티 체킹) 체험**: `@Transactional` 안에서 조회한 엔티티의 필드만 직접 바꾸고, `save()`를 한 번도 호출하지 않아도 트랜잭션 커밋 시점에 자동으로 UPDATE되는 것을 확인
- 처음엔 `PUT`으로 시작했다가 "일부 필드만 수정"이 필요하다는 걸 깨닫고 `PATCH`로 전환 (PUT은 전체 교체, PATCH는 부분 수정이라는 의미 차이 정리)
- 과정에서 겪은 버그들: 빈 필드로 PATCH 시 NOT NULL 제약 위반, 변경 없는 요청에도 `updatedAt`이 갱신되는 문제 — 둘 다 직접 에러 재현 후 수정
- (곁가지) MyBatis로 raw SQL을 직접 관리하는 방식도 비교 체험 (`spike/mybatis`, main에는 merge 안 함)

### 결과
- 회원가입/게시글 작성/수정 API 정상 동작 확인
- `save()` 호출 없이 DB에 UPDATE가 반영되는 것을 직접 확인해 더티 체킹의 동작 원리를 체감
- JSON 바인딩용 DTO에 불필요하게 붙였던 `@Setter`, `@NoArgsConstructor`를 "실제로 필요한지" 하나씩 검증하며 제거 — 패턴을 무조건 따라 붙이지 않고 이유를 확인하는 습관 강화

## v0.7 - 댓글, 연관관계, N+1 해결

### 문제
- Post와 Member가 `memberId`(단순 숫자)로만 연결돼 있어서, "이 게시글을 쓴 회원"을 객체로 바로 다룰 수 없었음
- 게시글에 댓글을 다는 기능이 아예 없었음
- 게시글 목록에 작성자 정보를 같이 보여주려 하니 조회 쿼리가 게시글 수만큼 추가로 발생함

### 원인
- `memberId: Long` 필드만 있으면 매번 `memberRepository.findById(memberId)`로 직접 조회해야 하고, JPA 연관관계 매핑(지연 로딩 등)을 전혀 못 씀
- Comment 엔티티·API가 없었음
- `@ManyToOne`은 기본 전략이 EAGER라 Post를 조회하는 순간 연관된 Member도 즉시 조회되는데, `findAll()`로 여러 Post를 한 번에 가져오면 각 Post마다 그 조회가 따로 실행돼서 1(Post 조회) + N(Member 조회)번 쿼리가 발생함

### 해결
- Post의 `memberId: Long` 필드를 `member: Member`로 바꾸고 `@ManyToOne` + `@JoinColumn(name = "member_id")`로 Post-Member 연관관계 매핑
- `Comment` 엔티티(Member, Post 양쪽에 `@ManyToOne`)와 `POST /posts/{postId}/comments`, `PATCH /comments/{id}` 구현, 응답은 `CommentResponse` DTO로 분리
- `spring.jpa.show-sql` + `format_sql`로 Hibernate가 실제 보내는 쿼리를 로그로 확인(게시글 5개, 작성자 3명 기준 1 + 3번 쿼리 발생 — 중복 작성자는 영속성 컨텍스트 1차 캐시 덕에 한 번만 조회됨도 확인)
- `PostRepository`에 `@Query("SELECT p FROM Post p JOIN FETCH p.member")`로 `findAllWithMember()`를 추가해 Post와 Member를 한 번의 쿼리로 조회하도록 해결
- CommentRequest는 생성/수정용을 분리하지 않고 그대로 뒀음 — Comment 수정은 본인 확인용으로 여전히 `memberId`가 필요해서 분리해도 모양이 같아지는 상황. v0.13에서 인증이 들어오면 재검토 예정

### 결과
- Post 목록/상세에서 작성자 정보를 연관관계로 바로 꺼내 쓸 수 있게 됨
- 게시글에 댓글 작성/수정 가능, 본인 댓글만 수정되는 것 확인
- Hibernate 로그 기준 게시글 목록 조회 쿼리가 1 + N번에서 1번으로 줄어든 것 확인

## v0.8 - 트랜잭션

### 문제
- 게시글 삭제 기능이 없었음
- 댓글이 달린 게시글을 지우려 하면 FK 제약(`comments.post_id` → `posts.id`) 때문에 삭제가 막힘
- 단순히 "댓글 먼저 지우고 게시글 지우기"로 바꿔도, 중간에 예외가 나면 일부 댓글만 지워진 채로 DB에 남는 문제를 직접 재현해서 확인

### 원인
- `posts`를 지우기 전에 그걸 참조하는 `comments` 행을 먼저 지워야 하는데, 그 순서가 없었음
- 댓글을 하나씩 반복문으로 지우는 걸 `@Transactional` 없이 테스트하니, `commentRepository.delete()` 호출 하나하나가 각자 독립적인 트랜잭션이라 즉시 커밋됨 — 중간에 강제로 예외를 던져도 이미 커밋된 댓글은 롤백되지 않고 DB에 반영된 상태로 남음

### 해결
- `CommentRepository`에 `deleteByPostId(Long postId)` 추가 (메서드 이름 규칙으로 삭제 쿼리 자동 생성)
- `PostService.delete()`에서 댓글을 먼저 지우고 게시글을 지우도록 구현, 전체를 `@Transactional`로 묶어서 하나의 트랜잭션 안에서 처리
- `DELETE /posts/{id}` 엔드포인트 추가
- 강제로 `RuntimeException`을 던지는 테스트 코드로 두 가지를 직접 비교 확인: `@Transactional` 없을 때는 일부 댓글만 삭제된 채 남고, 적용했을 때는 예외 시 전부 롤백됨

### 결과
- 댓글이 달린 게시글도 FK 제약 위반 없이 정상 삭제됨
- `@Transactional` 적용 전/후 DB 상태를 직접 비교해서 트랜잭션의 원자성("전부 성공 아니면 전부 실패")을 체감
- SQL이 실행(flush)되는 시점과 커밋되는 시점이 다르다는 것, 그리고 롤백은 DB가 내부적으로 기록해둔 undo 정보로 되돌리는 것이라는 걸 로그로 확인

## v0.9 - 페이징, QueryDSL

### 문제
- 게시글 목록 API가 전체를 한 번에 반환함. 게시글 1만 개일 때 응답 219ms / 1.68MB, 100만 개일 때 41.5초 / 173.88MB
- 제목·작성자로 검색하려는데, 조건이 있을 수도 없을 수도 있어서 조합마다 쿼리 메서드가 필요해짐 (keyword, memberId 두 조건 × 첫 페이지/다음 페이지 = 8개, 조건이 하나 늘면 16개)

### 원인
- SQL은 1번이라 쿼리 횟수 문제가 아님. 서비스 메서드 자체가 약 35초 걸렸고, 100만 개 엔티티를 영속성 컨텍스트에 올리는 비용이 원인. `open-in-view`가 켜져 있어서 직렬화가 끝날 때까지 엔티티가 살아 있음
- JPQL은 문자열이라 조건 조합을 `@Query` 하나로 표현할 수 없고, 문자열을 이어 붙이면 `AND`/공백 실수가 컴파일이 아니라 실행 시점에, 그것도 특정 조건 조합이 들어왔을 때만 터짐

### 해결
- 커서 기반 페이징: `GET /posts?cursor=...&size=20`, 정렬은 `created_at DESC, id DESC`
- 응답은 `{ items, nextCursor, hasNext }` (`PostPageResponse`). `nextCursor`는 `createdAt_id` 형식이고, 마지막 `items`의 DB 값으로 만듦
- `size + 1`개를 조회해서 초과분이 있으면 `hasNext=true`, 응답에서는 마지막 한 개를 버림
- `size` 상한(50)은 서버에서 강제하고, 커서 파싱에 실패하면 `InvalidCursorException`을 던짐 (현재 500, v0.10 전역 예외 처리에서 400으로 매핑 예정)
- 코드 리뷰에서 `decodeCursor`의 `catch (IllegalArgumentException)`이 `DateTimeParseException`, `ArrayIndexOutOfBoundsException`을 못 잡는다는 걸 확인해서 세 예외를 모두 잡도록 수정
- QueryDSL 도입 (`io.github.openfeign.querydsl`, Boot 4 / Hibernate 7 호환을 위한 포크). `JPAQueryFactory` 빈 등록 후 `PostQueryRepository.findPage` 하나로 `keyword`, `memberId`, `cursor` 조건 처리
- 각 조건 메서드는 조건이 없으면 `null`을 반환하고, `where()`가 `null`을 무시해서 조건이 있는 것만 `AND`로 붙음
- 쿼리 클래스 위치는 `PostRepositoryCustom` + `Impl` 대신 별도 `PostQueryRepository`로 둠 (구조가 단순해서 먼저 이해하기 좋음)
- 사용하지 않게 된 `findFirstPage`, `findNextPage`, `findAllWithMember` 삭제

### 결과
- 게시글 100만 개에서 첫 페이지 약 856ms / 3.28kB (이전 41.5초 / 173.88MB). 같은 요청이 2078ms로 나온 적도 있어서 1회 측정값의 편차가 큼
- 정확히 `size`개만 남은 경계에서 `hasNext=false`, 마지막 페이지에서 `nextCursor=null`, `size=100000` 요청은 50개로 제한됨을 확인
- 조건 조합별 쿼리 메서드 8개가 `findPage` 하나로 줄었고, 검색 조건 + 커서 조합 요청도 정상 동작
- 남은 문제: `created_at`, `title`에 인덱스가 없어서 `EXPLAIN`이 `type=ALL`(풀 스캔)로 나옴. 20개만 가져와도 수백 ms ~ 2초가 걸리고, `LIKE '%키워드%'`는 인덱스를 못 탐. v1.2(실행 계획, 인덱스)에서 해결 예정
