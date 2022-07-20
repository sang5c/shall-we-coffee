package dev.gueltto.shallwecoffee.chat;

import com.slack.api.model.Message;
import com.slack.api.model.Reaction;
import dev.gueltto.shallwecoffee.chat.repository.ChatEntity;
import dev.gueltto.shallwecoffee.chat.repository.ChatRepository;
import dev.gueltto.shallwecoffee.chat.slackapi.SlackApi;
import dev.gueltto.shallwecoffee.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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
    private final ChatRepository chatRepository;

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
        LocalDate targetDate = LocalDate.now();
        log.info("now: {}", targetDate);
        log.info("헤로쿠 스케줄러 잘 나오나 보자고~");

        List<ChatEntity> targetChats = chatRepository.findByDeadlineEquals(targetDate);

        List<String> targetChannels = targetChats.stream()
                .map(ChatEntity::getChannelId)
                .toList();

        List<Message> targetMessages = targetChannels.stream()
                .map(channelId -> searchTargetMessages(channelId, targetDate))
                .flatMap(Collection::stream)
                .toList();
        // TODO:
        //  1. 타겟 메시지에 대해 쓰레드로 특정 이모지 남긴 사람들을 멘션한다.
        //  2. 메시지에 마감 이모지를 추가한다.
        //  3. DB에서 삭제한다.
        targetMessages.forEach(message -> slackApi.sendMessage(message.getChannel(), message.getText().split("\n")[0]));
    }

    /**
     * bot이 작성한 메시지는 message id로 식별이 불가능하다.
     */
    private List<Message> searchTargetMessages(String channelId, LocalDate targetDate) {
        String deadlineText = "*마감일*: " + targetDate.toString();
        return slackApi.searchMessages(channelId).stream()
                .filter(this::isBotMessage)
                .filter(message -> message.getText().contains(deadlineText))
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
        if (Objects.isNull(reactions)) {
            return false;
        }

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

    // TODO: 봇 멘션도 고려하기
    public void startCoffeeChat(CoffeeChat coffeeChat) {
        ChatEntity chatEntity = coffeeChat.toEntity();
        chatRepository.save(chatEntity);
        slackApi.sendMessage(coffeeChat.getChannelId(), coffeeChat.toMessage());
    }
}
