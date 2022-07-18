package dev.gueltto.shallwecoffee.chat.slackapi;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.conversations.ConversationsMembersResponse;
import com.slack.api.model.Message;
import com.slack.api.model.Reaction;
import com.slack.api.model.User;
import dev.gueltto.shallwecoffee.chat.SlackChannel;
import dev.gueltto.shallwecoffee.chat.SlackMember;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Profile("prod")
@RequiredArgsConstructor
@Service
public class ProdSlackApi implements SlackApi {

    private static final String SLACK_BOT_TOKEN = "SLACK_BOT_TOKEN";
    private static final MethodsClient slackClient = Slack.getInstance().methods(System.getenv(SLACK_BOT_TOKEN));

    @Override
    public List<SlackChannel> findChannels(String prefix) {
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

    @Override
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

    @Override
    public List<String> findMemberIds(SlackChannel channel) {
        try {
            ConversationsMembersResponse response = slackClient.conversationsMembers(builder ->
                    builder.channel(channel.getId())
            );
            return response.getMembers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SlackMember findUserInfo(String userId) {
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

    @Override
    public List<Message> searchMessages(String channelId) {
        try {
            ConversationsHistoryResponse history = slackClient.conversationsHistory(builder ->
                    builder.channel(channelId)
                            .includeAllMetadata(false)
                            .limit(30)
            );

            return history.getMessages();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
