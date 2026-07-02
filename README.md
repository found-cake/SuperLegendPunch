# 고대도시탐방(?)
CSKUS 연합 CTF MISC 마인크래프트 문제 플러그인
- [SuperLegendPunch](https://github.com/found-cake/SuperLegendPunch)
- [DeepDarkBoss](https://github.com/found-cake/DeepDarkBoss)

# SuperLegendPunch 플러그인 기능
HIGHEST(이벤트 캔슬 유무를 지정 할 수 있는 가능 높은 우선 순위) 이벤트 우선 순위를 이용하여
`SSS...Punch!` 아이템으로 공격시 이벤트 캔슬을 무시하고 데미지를 부여
이때 `S`가 더 적을 수록 데미지가 증가함 `damage = 1000/(S 갯수)`

모루를 사용하여 아이템을 변형을 시도 할때 마다 발생하는 이벤트(PrepareAnvilEvent)가 발생할 때마다
ReDoS가 발생하는 정규표현식으로 검증하여 아이템 제작을 막는다.

단 이때 자체 제작한 php regex와 같은 step limit regex를 구현 + 캐싱을 이용하여 실제 서버가 멈출 가능성을 차단 하였다.

# 기타
jar파일 제공이며 CTF 특성상 의도적으로 dsl과 lamda를 이용하여 오버 엔지니어링을 진행하였다.
