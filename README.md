# Shall We Coffee?

## API 호출 참고
- [SDK for java guide](https://slack.dev/java-slack-sdk/guides/web-api-basics)
- [Spring Boot + bolt](https://slack.dev/java-slack-sdk/guides/supported-web-frameworks)


## TODO
- 각 메시지 개행 -> List로 쪼개기. 전체멘트/각 조별 별도의 메시지
- modal
  - [ ] 직군별 채널에 커피챗 제안 (해당 채널에만, 제안자 공개)
    - [ ] 언제까지 신청 받을지
    - [ ] 참석 이모지 작성을 통해 참석자 식별?
      - 이렇게 하면 스케줄러가 필요할지도..?
    - [ ] 날짜 / 시간
    - [ ] 예상 장소
    - [ ] 목적(?), 본인 관심사 등 하고싶은 말
  - 참석 / 불참 판단
    - 사용자별로 참석, 불참 버튼이 나타난다. (상태 저장하려면 DB 필요할거같음)
    - 참석, 불참 이모지를 남긴다

- Chat 테이블 설계
- Entity 작성
  - id
  - audit
  - 마감일
  - 주최자 (제안자)
  - 장소
  - coffeeChat id
  - 완료 여부 (스케줄러 더 돌아야 하는지 확인용)
- 필요한 작업
  - CREATE TABLE
  - JPA
  - chat 전송하고 아이디 읽기
  - 참/불 이모지 읽기
    - 이모지 등록
  - 챗 모달창 이쁘게꾸미기 
- 스케줄러
  - 완료 여부 보고 아닌 것들 마감 일자 비교해서 당일이면 마감처리
    - 마감처리에선 참석 이모지 남긴 사람들한테 멘션과 시간/장소 알려주기
