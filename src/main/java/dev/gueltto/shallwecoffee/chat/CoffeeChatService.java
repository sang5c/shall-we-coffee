package dev.gueltto.shallwecoffee.chat;

import com.slack.api.model.Message;
import com.slack.api.model.Reaction;
import dev.gueltto.shallwecoffee.chat.slackapi.SlackApi;
import dev.gueltto.shallwecoffee.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
@Service
public class CoffeeChatService {
    private static final String GROUP_MESSAGE_FORMAT = "%d조: %s";
    private static final int FIRST_GROUP_INDEX = 1;
    private final MemberService memberService;
    private final SlackApi slackApi;

    /**
     * 운영진 관리에 의한 커피챗 시도
     */
    @Async
    public void startManagedCoffeeChat(int count, String message) {
        // TODO: 지정한 채널에 뿌릴지, 전체 채널에 뿌릴지 받으면 좋을듯!
        //  아니면 봇이 들어있는 채널만 출력한다던지?
        List<SlackChannel> channels = slackApi.findChannels("3_");
        log.info("channels = " + channels);

        channels.forEach(channel -> sendCoffeeMessageByChannel(channel, count, message));
    }

    @Scheduled(cron = "0 35 09 * * ?", zone = "Asia/Seoul")
    public void schedule() {
        log.info("now: {}", LocalDateTime.now());
        log.info("헤로쿠 스케줄러 잘 나오나 보자고~");

        // TODO: 등록시에 "마감일"과 "채널 정보"를 DB에 담는다.
        // 한 채널에 마감되지 않은 메시지가 여러개일때 구분해낼 방안이 필요하다.
        // 가능하면 등록시에 뭔가 방법이 있으면 좋은데..
        // 등록시에 셀프 멘션으로 정보를 받을 수 있는지 확인해보자.

        // 아니면 그냥 어렵게 생각하지 말고 "마감일: yyyy-MM-dd" equals 검사해서 처리하기.

        // TODO: 1. DB에서 관리대상 채널 조회
        //       2. 메시지를 읽어서 관련 정보 확인
        //       3. 마감 처리
        //       4. DB 값 조정
        /**
         * bot이 작성한 메시지는 message id로 식별이 불가능하다.
         */
        List<Message> messages = slackApi.searchMessages("channel").stream()
                .filter(this::isBotMessage)
                .filter(message -> Objects.nonNull(message.getReactions()))
                .filter(message -> notClosed(message.getReactions()))
                .toList();
    }

    /**
     * 봇이 작성한 메시지의 경우 bot id가 null이 아니다.
     * user가 작성한 경우 user id가 null이 아니다.
     */
    private boolean isBotMessage(Message message) {
        return Objects.nonNull(message.getBotId());
    }

    /**
     * 마감 이모지가 없는 경우 스케줄러 관리 대상이다.
     */
    private boolean notClosed(List<Reaction> reactions) {
        return reactions.stream()
                .noneMatch(reaction -> Objects.equals(reaction.getName(), "best"));
    }

    private void sendCoffeeMessageByChannel(SlackChannel channel, int headcount, String announcement) {
        List<String> channelMemberIds = slackApi.findMemberIds(channel);
        List<String> realMemberIds = excludeVisitors(channel, channelMemberIds);
        log.debug("realMemberIds = " + realMemberIds);

        Groups groups = Groups.of(realMemberIds, headcount);
        log.info("groups = " + groups);

        List<String> messages = generateChatMessages(announcement, groups);
        log.info("messages: " + messages);
        // 임시로 운영진 채널에만 전송하도록 막는다.
        // messages.forEach(chatMessage -> slackApi.sendMessage(channel.getId(), chatMessage));
        // messages.forEach(chatMessage -> slackApi.sendMessage("C03NU0V4BC2", chatMessage)); // 0_테스트용공개채널
        messages.forEach(chatMessage -> slackApi.sendMessage("C03H0UGJGGM", chatMessage));
    }

    private List<String> excludeVisitors(SlackChannel slackChannel, List<String> memberIds) {
        List<String> realMemberIds = memberService.findRealMembers(slackChannel.getId());
        return memberIds.stream()
                .filter(realMemberIds::contains)
                .toList();
    }

    private List<String> generateChatMessages(String message, Groups groups) {
        List<String> coffeeMessages = new ArrayList<>();
        coffeeMessages.add(message + "\n");
        // StringBuilder coffeeMessage = new StringBuilder(message);
        // coffeeMessage.append("\n");

        List<String> mentions = groups.toMentions();

        AtomicInteger count = new AtomicInteger(FIRST_GROUP_INDEX);
        mentions.forEach(mentionStr -> coffeeMessages.add(
                String.format(GROUP_MESSAGE_FORMAT, count.getAndIncrement(), mentionStr))
        );
        return coffeeMessages;
    }

    public void startCoffeeChat(CoffeeChat coffeeChat) {
        // TODO: 이 정보 DB 저장, 스케주럴에서 확인해서 종료 처리해줘야 함.
        // 1. 채널 아이디만 저장하거나
        // 2. 채널 아이디로 메시지 보낸거 읽어서 메시지 ID를 저장한다.

        // TODO:
        // 메시지를 전송한 후
        // 채널에서 메시지 ID를 읽어서
        // 커피챗 마감 정보와 함께 DB에 저장한다.
        // 스케줄러에서 마감 정보를 읽어 마감 처리를 시도한다.
        // 마감 처리에서 해야하는 작업은 리마인드 & 참가자 멘션 메시지 전송

        // TODO 2:
        //  user.conversations 읽어서 들어가있는 채널만 탐색.
        //  1. 메시지 작성자가 봇의 ID와 일치하고
        //  2. 특정 이모지가 달려있고
        //  3. 마감 이모지가 달려있지 않은 경

        // 메시지 읽으려면 conversation.history로 읽는듯?
        // 참고 이어서 하기 https://api.slack.com/messaging/retrieving#other_individual_messages
        // 앱 멘션도 고려하기
        slackApi.sendMessage(coffeeChat.getChannelId(), coffeeChat.toMessage());
    }
}
