# 커뮤니티 프로젝트

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