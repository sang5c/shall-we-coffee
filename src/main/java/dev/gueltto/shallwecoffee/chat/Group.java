package dev.gueltto.shallwecoffee.chat;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ToString
public class Group {
    private static final String MENTION_FORMAT = "<@%s>";

    private List<String> memberIds;

    private Group(List<String> memberIds) {
        this.memberIds = new ArrayList<>(memberIds);
    }

    public static Group of(List<String> memberIds) {
        return new Group(memberIds);
    }

    public void addMember(String memberId) {
        memberIds.add(memberId);
    }

    public String toMentionStr() {
        return memberIds.stream()
                .map(id -> String.format(MENTION_FORMAT, id))
                .collect(Collectors.joining(", "));
    }
}
