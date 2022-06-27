package dev.gueltto.shallwecoffee.chat;

import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@ToString
public class Groups {

    private List<Group> groups;

    public Groups(List<Group> groups) {
        this.groups = groups;
    }

    /**
     * @param memberIds 전체 멤버
     * @param headcount 그룹 구성원 수
     *                  <br>
     *                  그룹 지정 인원수로 나누어 떨어지지 않는 인원은 구성된 조에 한명씩 추가된다.
     */
    public static Groups of(List<String> memberIds, int headcount) {
        List<String> shuffledIds = getShuffledIds(memberIds);
        List<Group> groups = createGroups(shuffledIds, headcount);
        appendUngroupedMembers(groups, shuffledIds, headcount);

        return new Groups(groups);
    }

    // TODO: 분리
    private static List<String> getShuffledIds(List<String> memberIds) {
        List<String> shuffled = new ArrayList<>(memberIds);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    private static List<Group> createGroups(List<String> memberIds, int headcount) {
        List<Group> groups = new ArrayList<>();

        IntStream.range(0, calculateGroupSize(memberIds, headcount))
                .forEach(i -> {
                            int fromIndex = i * headcount;
                            groups.add(
                                    Group.of(memberIds.subList(fromIndex, fromIndex + headcount)) // sublist는 unmodifiable 이다.
                            );
                        }
                );
        return groups;
    }

    private static void appendUngroupedMembers(List<Group> groupList, List<String> memberIds, int headcount) {
        List<String> ungroupedMemberIds = extractUngroupedMemberIds(headcount, memberIds, groupList.size());
        for (int i = 0; i < ungroupedMemberIds.size(); i++) {
            String id = ungroupedMemberIds.get(i);
            groupList.get(i % groupList.size())
                    .addMember(id);
        }
    }

    /**
     * 3명이서 구성원을 이루었으나 한명의 불참으로 2명이 되는 케이스
     * 또는 2인 이하로 조가 구성되지 않기 위해 내림 처리를 한다.
     */
    private static int calculateGroupSize(List<String> memberIds, int headcount) {
        return memberIds.size() / headcount;
    }

    private static List<String> extractUngroupedMemberIds(int headcount, List<String> channelMemberIds, int groupSize) {
        int ungroupedMemberIndex = headcount * groupSize;
        return new ArrayList<>(channelMemberIds.subList(ungroupedMemberIndex, channelMemberIds.size()));
    }

    public List<String> toMentions() {
        return groups.stream()
                .map(Group::toMentionStr)
                .toList();
    }
}
