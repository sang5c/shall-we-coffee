package dev.gueltto.shallwecoffee.chat;

import dev.gueltto.shallwecoffee.chat.slackapi.SlackApi;
import dev.gueltto.shallwecoffee.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
@Service
public class CoffeeChatService {
    private static final String GROUP_MESSAGE_FORMAT = "\n%d조: %s";
    private static final int FIRST_GROUP_INDEX = 1;
    private final MemberService memberService;
    private final SlackApi slackApi;

    @Async
    public void startCoffeeChat(int count, String message) {
        // TODO: 지정한 채널에 뿌릴지, 전체 채널에 뿌릴지 받으면 좋을듯!
        //  아니면 봇이 들어있는 채널만 출력한다던지?
        List<SlackChannel> channels = slackApi.findChannels("3_");
        log.info("channels = " + channels);

        channels.forEach(channel -> sendCoffeeMessage(channel, count, message));
    }

    private void sendCoffeeMessage(SlackChannel channel, int headcount, String message) {
        List<String> channelMemberIds = slackApi.findMemberIds(channel);
        List<String> realMemberIds = excludeVisitors(channel, channelMemberIds);
        log.debug("realMemberIds = " + realMemberIds);

        Groups groups = Groups.of(realMemberIds, headcount);
        log.info("groups = " + groups);

        String chatMessage = generateChatMessage(message, groups);
        // 임시로 운영진 채널에만 전송하도록 막는다.
        // slackApi.sendMessage(channel.getId(), chatMessage);
        slackApi.sendMessage("C03CXBNFNEM", chatMessage);
    }

    private List<String> excludeVisitors(SlackChannel slackChannel, List<String> memberIds) {
        List<String> realMemberIds = memberService.findRealMembers(slackChannel.getId());
        return memberIds.stream()
                .filter(realMemberIds::contains)
                .toList();
    }

    private String generateChatMessage(String message, Groups groups) {
        StringBuilder coffeeMessage = new StringBuilder(message);
        coffeeMessage.append("\n");

        List<String> mentions = groups.toMentions();

        AtomicInteger count = new AtomicInteger(FIRST_GROUP_INDEX);
        mentions.forEach(mentionStr -> coffeeMessage.append(
                String.format(GROUP_MESSAGE_FORMAT, count.getAndIncrement(), mentionStr))
        );
        return coffeeMessage.toString();
    }
}
