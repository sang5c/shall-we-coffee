package dev.gueltto.shallwecoffee.member;

import java.util.List;

public interface MemberService {
    List<String> findRealMembers(String channelId);
}
