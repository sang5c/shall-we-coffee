package dev.gueltto.shallwecoffee.chat;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
@Service
public class CoffeeChatService {

    // TODO: SlackService 분리
    private static final MethodsClient slackClient = Slack.getInstance()
            .methods(System.getenv("SLACK_BOT_TOKEN"));

    private List<SlackChannel> findChannels(String prefix) {
        try {
            ConversationsListResponse response = slackClient.conversationsList(ConversationsListRequest.builder().build());
            return response.getChannels().stream()
                    .map(c -> SlackChannel.of(c.getId(), c.getName()))
                    .filter(c -> c.startsWith(prefix))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<SlackMember> findMembers(SlackChannel channel) {
        // try {
        //     ConversationsMembersResponse response = slackClient.conversationsMembers(builder ->
        //             builder.channel(channel.getId())
        //     );
        //     return response.getMembers().stream()
        //             .map(this::findUserInfo)  // 현재 기준으로 userId만 필요하다. 변환하지 않고 사용해도 됨
        //             .toList();
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }
        return List.of(
                SlackMember.of("USA5B2R61", "disname1", "realname1"),
                SlackMember.of("id1", "disname1", "realname1"),
                SlackMember.of("id2", "disname2", "realname2"),
                SlackMember.of("id3", "disname3", "realname3"),
                SlackMember.of("id4", "disname4", "realname4"),
                SlackMember.of("id5", "disname5", "realname5"),
                SlackMember.of("id6", "disname6", "realname6"),
                SlackMember.of("id7", "disname7", "realname7"),
                SlackMember.of("id8", "disname8", "realname8"),
                SlackMember.of("id9", "disname9", "realname9"),
                SlackMember.of("id10", "disname10", "realname10")
        );
    }

    private SlackMember findUserInfo(String userId) {
        try {
            User user = slackClient.usersInfo(req -> req.user(userId))
                    .getUser();

            return SlackMember.of(
                    user.getId(),
                    user.getProfile().getDisplayName(),
                    user.getProfile().getRealName()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean sendMessage(String channel, String message) {
        try {
            ChatPostMessageResponse response = slackClient.chatPostMessage(requestBuilder ->
                    requestBuilder.text(message)
                            .channel(channel)
            );
            return response.isOk();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: async
    public void startCoffeeChat(int count, String message) {
        List<SlackChannel> channels = findChannels("");
        log.info("channels = " + channels);

        channels.forEach(channel -> sendCoffeeMessage(channel, count, message));
    }

    private void sendCoffeeMessage(SlackChannel channel, int headcount, String message) {
        // TODO: 진짜 구성원인지, 타 채널 참여자인지 확인 필요
        List<SlackMember> channelMembers = findMembers(channel);

        int numberOfGroups = channelMembers.size() / headcount; // TODO: 반올림?

        // TODO: 구성원을 랜덤하게 섞는 기능도 있어야 함
        // TODO: headcount validation 필요. 몫이 2 이상,
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            int startIndex = headcount * i;
            groups.add(
                    Group.of(channelMembers.subList(startIndex, startIndex + headcount)) // TODO: 나눠떨어지지 않는 인원을 어떻게 포함시킬 것인지
            );
        }
        log.info("groups = " + groups);
        String chatMessage = generateChatMessage(message, groups);
        sendMessage(channel.getId(), chatMessage);
    }

    private String generateChatMessage(String message, List<Group> groups) {
        StringBuilder coffeeMessageBuilder = new StringBuilder(message);
        coffeeMessageBuilder.append("\n");

        List<String> mentions = groups.stream()
                .map(Group::toMentionStr)
                .toList();

        AtomicInteger count = new AtomicInteger(1);
        mentions.forEach(
                mentionStr -> coffeeMessageBuilder.append("\n")
                        .append(count.getAndIncrement())
                        .append("조: ")
                        .append(mentionStr)
        );
        return coffeeMessageBuilder.toString();
    }
}
