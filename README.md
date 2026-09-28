# 커뮤니티 프로젝트
## v0.2 - 스프링 부트 전환

---

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

---

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