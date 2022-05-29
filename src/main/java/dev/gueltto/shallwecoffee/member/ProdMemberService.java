package dev.gueltto.shallwecoffee.member;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile("!(test|local)")
@Service
public class ProdMemberService implements MemberService {

    // TODO: findBy Bigquery
    @Override
    public List<String> findRealMembers(String channelId) {
        return null;
    }
}
