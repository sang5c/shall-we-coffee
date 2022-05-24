package dev.gueltto.shallwecoffee.chat;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.conversations.ConversationsMembersResponse;
import com.slack.api.model.User;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.element.BlockElements.asContextElements;
import static com.slack.api.model.view.Views.viewClose;
import static com.slack.api.model.view.Views.viewTitle;

@RequiredArgsConstructor
@Slf4j
@Service
public class SlackService {
    private MethodsClient slackClient = Slack.getInstance().methods(System.getenv("SLACK_BOT_TOKEN"));

    @PostConstruct
    public void tt() {
        System.out.println(System.getenv("SLACK_BOT_TOKEN"));
    }

    public List<SlackChannel> findChannels() throws SlackApiException, IOException {
        ConversationsListResponse response = slackClient.conversationsList(conversationsListRequestBuilder -> null);
        return response.getChannels().stream()
                .map(c -> SlackChannel.of(c.getId(), c.getName()))
                .toList();
    }

    public List<String> findMembers() throws SlackApiException, IOException {
        ConversationsMembersResponse response = slackClient.conversationsMembers(builder ->
                builder.channel("CSCB3M43G")
        );
        return response.getMembers();
    }

    public SlackUser findMemberName(String userId) throws SlackApiException, IOException {
        User user = slackClient.usersInfo(req -> req.user(userId))
                .getUser();

        return SlackUser.of(
                user.getId(),
                user.getProfile().getDisplayName(),
                user.getProfile().getRealName()
        );
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

    public boolean sendMessage(String channel, String message) throws SlackApiException, IOException {
        ChatPostMessageResponse response = slackClient.chatPostMessage(requestBuilder -> requestBuilder.text(message)
                .channel(channel)
        );

        return response.isOk();
    }
}
