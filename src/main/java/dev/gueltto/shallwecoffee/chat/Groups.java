package dev.gueltto.shallwecoffee.chat;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@ToString
public class Groups {

    private List<Group> groups;

    public Groups(List<Group> groups) {
        this.groups = groups;
    }

    // TODO: headcount validation 필요. 몫이 2 이상,
    // TODO: headcount로 member size가 나눠지는지 검증
    // TODO: 구성원을 랜덤하게 섞는 기능도 있어야 함
    /**
     * 그룹 지정 인원수로 나누어 떨어지지 않는 인원은 구성된 조에 한명씩 추가된다.
     */
    public static Groups of(int headcount, List<String> channelMemberIds) {
        List<Group> groupList = divideMembersIntoGroups(headcount, channelMemberIds);
        List<String> ungroupedMemberIds = extractUngroupedMemberIds(headcount, channelMemberIds, groupList.size());
        appendUngroupedMembers(groupList, ungroupedMemberIds);

        return new Groups(groupList);
    }

    private static List<Group> divideMembersIntoGroups(int headcount, List<String> channelMemberIds) {
        List<Group> groupList = new ArrayList<>();
        IntStream.range(0, headcount)
                .forEach(i -> {
                            int fromIndex = i * headcount;
                            groupList.add(
                                    Group.of(channelMemberIds.subList(fromIndex, fromIndex + headcount)) // sublist는 unmodifiable 이다.
                            );
                        }
                );
        return groupList;
    }

    private static List<String> extractUngroupedMemberIds(int headcount, List<String> channelMemberIds, int groupSize) {
        int ungroupedMemberIndex = headcount * groupSize;
        return channelMemberIds.subList(ungroupedMemberIndex, channelMemberIds.size());
    }

    private static void appendUngroupedMembers(List<Group> groupList, List<String> ungroupedMemberIds) {
        for (int i = 0; i < ungroupedMemberIds.size(); i++) {
            groupList.get(i).addMember(ungroupedMemberIds.get(i));
        }
    }

    public List<String> toMentions() {
        return groups.stream()
                .map(Group::toMentionStr)
                .toList();
    }
}
