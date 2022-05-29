package dev.gueltto.shallwecoffee.chat;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ToString
public class Group {
    private static final String MENTION_FORMAT = "<@%s>";
    private static final int MIN_MEMBER_COUNT = 2;

    private List<String> memberIds;

    private Group(List<String> memberIds) {
        validate(memberIds);
        this.memberIds = new ArrayList<>(memberIds);
    }

    public static Group of(List<String> memberIds) {
        return new Group(memberIds);
    }

    private static void validate(List<String> memberIds) {
        if (isEnoughMembers(memberIds)) {
            throw new RuntimeException("그룹 구성 최소 인원은 3명입니다. size: " + memberIds.size());
        }
    }

    private static boolean isEnoughMembers(List<String> memberIds) {
        return !(memberIds.size() > MIN_MEMBER_COUNT);
    }

    public void addMember(String memberId) {
        memberIds.add(memberId);
    }

    // TODO: 분리
    public String toMentionStr() {
        return memberIds.stream()
                .map(id -> String.format(MENTION_FORMAT, id))
                .collect(Collectors.joining(", "));
    }
}
