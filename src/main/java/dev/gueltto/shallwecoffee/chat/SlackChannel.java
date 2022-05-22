package dev.gueltto.shallwecoffee.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SlackChannel {
    private String id;
    private String name;

    public static SlackChannel of(String id, String name) {
        return new SlackChannel(id, name);
    }
}
