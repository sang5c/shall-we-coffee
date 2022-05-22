package dev.gueltto.shallwecoffee.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SlackUser {
    private String id;
    private String displayName;
    private String realName;

    public static SlackUser of(String id, String displayName, String realName) {
        return new SlackUser(id, displayName, realName);
    }
}
