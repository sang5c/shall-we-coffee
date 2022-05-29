package dev.gueltto.shallwecoffee.member;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile("local")
@Service
public class LocalMemberService implements MemberService {
    @Override
    public List<String> findRealMembers(String channelId) {
        return List.of(
                "id1",
                "id2",
                "id3",
                "id8",
                "id9",
                "USA5B2R61"
        );
    }
}
