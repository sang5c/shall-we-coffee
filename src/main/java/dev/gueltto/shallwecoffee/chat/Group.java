package dev.gueltto.shallwecoffee.chat;

import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@ToString
public class Group {
    private List<SlackMember> members;

    private Group(List<SlackMember> members) {
        this.members = members;
    }

    public static Group of(List<SlackMember> members) {
        return new Group(members);
    }

    public String toMentionStr() {
        return members.stream()
                .map(SlackMember::toMention)
                .collect(Collectors.joining(", "));
    }
}
