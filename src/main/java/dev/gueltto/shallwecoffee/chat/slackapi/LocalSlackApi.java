package dev.gueltto.shallwecoffee.chat.slackapi;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import dev.gueltto.shallwecoffee.chat.SlackChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile("local")
@RequiredArgsConstructor
@Service
public class LocalSlackApi implements SlackApi {

    private static final String SLACK_BOT_TOKEN = "SLACK_BOT_TOKEN";
    private static final MethodsClient slackClient = Slack.getInstance().methods(System.getenv(SLACK_BOT_TOKEN));

    @Override
    public List<SlackChannel> findChannels(String prefix) {
        return List.of(
                SlackChannel.of("ch1", "ch_name1"),
                SlackChannel.of("ch2", "ch_name2"),
                SlackChannel.of("ch3", "ch_name3"),
                SlackChannel.of("ch4", "ch_name4"),
                SlackChannel.of("ch5", "ch_name5"),
                SlackChannel.of("ch6", "ch_name6")
        );
    }

    @Override
    public boolean sendMessage(String channel, String message) {
        return true;
    }

    @Override
    public List<String> findMemberIds(SlackChannel channel) {
        return List.of(
                "id1", "id2", "id3", "id4", "id5",
                "id6", "id7", "id8", "id9", "USA5B2R61", "id10"
        );
    }
}
