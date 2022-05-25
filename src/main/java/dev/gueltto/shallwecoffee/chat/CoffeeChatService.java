package dev.gueltto.shallwecoffee.chat;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.User;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.element.BlockElements.asContextElements;
import static com.slack.api.model.view.Views.viewClose;
import static com.slack.api.model.view.Views.viewTitle;

@RequiredArgsConstructor
@Slf4j
@Service
public class CoffeeChatService {
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
        //             .map(this::findUserInfo)
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

    /**
     * <a href="https://api.slack.com/methods/views.open/code">API DOCS</a>
     */
    public String pingPong(String triggerId) throws SlackApiException, IOException {
        View modalView = Views.view(v -> v
                .type("modal")
                .title(viewTitle(vt -> vt.type("plain_text").text("My App")))
                .close(viewClose(vc -> vc.type("plain_text").text("Close")))
                .blocks(asBlocks(
                        section(s -> s.text(markdownText(mt ->
                                mt.text("About the simplest modal you could conceive of :smile:\\n\\nMaybe <https://api.slack.com/reference/block-kit/interactive-components|*make the modal interactive*> or <https://api.slack.com/surfaces/modals/using#modifying|*learn more advanced modal use cases*>.")))),
                        context(c -> c.elements(asContextElements(
                                markdownText("Psssst this modal was designed using <https://api.slack.com/tools/block-kit-builder|*Block Kit Builder*>")
                        )))
                ))
        );
        var result = slackClient.viewsOpen(r ->
                r.triggerId(triggerId)
                        .view(modalView)
        );
        // Print result
        log.info("result: {}", result);
        return result.isOk() + "";
    }

    public boolean sendMessage(String channel, String message) {
        try {
            ChatPostMessageResponse response = slackClient.chatPostMessage(requestBuilder -> requestBuilder.text(message)
                    .channel(channel)
            );
            return response.isOk();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: async
    public void startCoffeeChat(int count, String message) {
        // TODO
        //  1. 방 목록 조회
        //  2. 방별 인원 조회
        //  3. 인원을 받은 수로 나눠서 그룹으로 만듦
        //  4. 메시지: N조: @user1, @user2, @user3
        //  5. 전체 완료되면 통합 메시지: 커피 챗 부탁드립니다 :)

        List<SlackChannel> channels = findChannels("");
        log.info("channels = " + channels);

        channels.forEach(channel -> sendCoffeeMessage(channel, count, message));
    }

    private void sendCoffeeMessage(SlackChannel channel, int headcount, String message) {
        List<SlackMember> channelMembers = findMembers(channel);

        int numberOfGroups = channelMembers.size() / headcount; // TODO: 반올림?

        // TODO: 구성원을 랜덤하게 섞는 기능도 있어야 함
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            int startIndex = headcount * i;
            groups.add(
                    Group.of(channelMembers.subList(startIndex, startIndex + headcount))
            );
        }

        log.info("groups = " + groups);

        // TODO: 나눠떨어지지 않는 인원을 어떻게 포함시킬 것인지

        StringBuilder coffeeMessageBuilder = new StringBuilder(message);
        coffeeMessageBuilder.append("\n");

        List<String> mentions = groups.stream()
                .map(Group::toMentionStr)
                .toList();

        AtomicInteger count = new AtomicInteger(1);
        mentions.forEach(
                m -> coffeeMessageBuilder.append("\n")
                        .append(count.getAndIncrement())
                        .append("조: ")
                        .append(m)
        );

        sendMessage(channel.getId(), coffeeMessageBuilder.toString());
    }
}
