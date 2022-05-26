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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
@Service
public class CoffeeChatService {
    private static final String GROUP_MESSAGE_FORMAT = "\n%d조: %s";

    // TODO: SlackService 분리
    private static final MethodsClient slackClient = Slack.getInstance()
            .methods(System.getenv("SLACK_BOT_TOKEN"));
    private static final int FIRST_GROUP_INDEX = 1;

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

    public List<String> findMemberIds(SlackChannel channel) {
        // try {
        //     ConversationsMembersResponse response = slackClient.conversationsMembers(builder ->
        //             builder.channel(channel.getId())
        //     );
        //     return response.getMembers();
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }
        return List.of(
                "id1", "id2", "id3", "id4", "id5",
                "id6", "id7", "id8", "id9", "USA5B2R61", "id10"
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
        // TODO: 지정한 채널에 뿌릴지, 전체 채널에 뿌릴지 받으면 좋을듯!
        //  아니면 봇이 들어있는 채널만 출력한다던지?
        List<SlackChannel> channels = findChannels("3_");
        log.info("channels = " + channels);

        channels.forEach(channel -> sendCoffeeMessage(channel, count, message));
    }

    private void sendCoffeeMessage(SlackChannel channel, int headcount, String message) {
        // TODO: 진짜 구성원인지, 타 채널 참여자인지 확인 필요
        List<String> channelMemberIds = findMemberIds(channel);

        Groups groups = Groups.of(headcount, channelMemberIds);

        log.info("groups = " + groups);
        String chatMessage = generateChatMessage(message, groups);
        sendMessage(channel.getId(), chatMessage);
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
