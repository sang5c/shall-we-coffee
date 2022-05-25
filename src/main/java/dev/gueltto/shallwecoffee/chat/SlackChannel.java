package dev.gueltto.shallwecoffee.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class SlackChannel {
    private String id;
    private String name;

    public static SlackChannel of(String id, String name) {
        return new SlackChannel(id, name);
    }

    public boolean startsWith(String prefix) {
        return this.name.startsWith(prefix);
    }
}
