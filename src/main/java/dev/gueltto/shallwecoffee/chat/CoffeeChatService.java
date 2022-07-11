package dev.gueltto.shallwecoffee.chat;

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
        messages.forEach(chatMessage -> slackApi.sendMessage("C03NU0V4BC2", chatMessage)); // 0_테스트용공개채널
        // messages.forEach(chatMessage -> slackApi.sendMessage("C03H0UGJGGM", chatMessage));
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
        slackApi.sendMessage(coffeeChat.getChannelId(), coffeeChat.toMessage());
    }
}
