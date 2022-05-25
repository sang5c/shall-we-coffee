package dev.gueltto.shallwecoffee.chat;

import lombok.Data;

@Data
public class SlackMember {
    private static final String MENTION_FORMAT = "<@%s>";

    private String id;
    private String displayName;
    private String realName;

    public SlackMember(String id, String displayName, String realName) {
        this.id = id;
        this.displayName = displayName;
        this.realName = realName;
    }

    public static SlackMember of(String id, String displayName, String realName) {
        return new SlackMember(id, displayName, realName);
    }

    public String toMention() {
        return String.format(MENTION_FORMAT, this.id);
    }
}
