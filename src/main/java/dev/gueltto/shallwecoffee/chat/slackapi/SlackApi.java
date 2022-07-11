package dev.gueltto.shallwecoffee.chat.slackapi;

import dev.gueltto.shallwecoffee.chat.SlackChannel;
import dev.gueltto.shallwecoffee.chat.SlackMember;

import java.util.List;

public interface SlackApi {
    List<SlackChannel> findChannels(String prefix);

    boolean sendMessage(String channel, String message);

    List<String> findMemberIds(SlackChannel channel);

    SlackMember findUserInfo(String userId);
}
