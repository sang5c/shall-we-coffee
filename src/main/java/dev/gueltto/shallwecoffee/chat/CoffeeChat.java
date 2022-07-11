package dev.gueltto.shallwecoffee.chat;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CoffeeChat {

    private final SlackMember slackMember;
    private final String channelId;
    private final LocalDate chatDate;
    private final LocalDate deadline;
    private final String place;
    private final String announcement;

    public CoffeeChat(SlackMember slackMember, String channelId, LocalDate chatDate, LocalDate deadline, String place, String announcement) {
        this.slackMember = slackMember;
        this.channelId = channelId;
        this.chatDate = chatDate;
        this.deadline = deadline;
        this.place = place;
        this.announcement = announcement;
    }

    public static CoffeeChat create(SlackMember slackMember, String selectedChannel, String selectedDate, String deadline, String place, String announcement) {
        return new CoffeeChat(
                slackMember,
                selectedChannel,
                LocalDate.parse(selectedDate),
                LocalDate.parse(deadline),
                place,
                announcement
        );
    }

    public String toMessage() {
        StringBuilder message = new StringBuilder();
        message.append(announcement);
        message.append("\n- *커피챗 희망 날짜:* " + chatDate);
        message.append("\n- *모집 마감일:* " + deadline);
        message.append("\n- *예상 장소:* " + place);
        message.append("\n- *요청자:* " + slackMember.toMention());
        message.append("\n\n참석 / 불참 이모지를 남겨주세요!");

        return message.toString();
    }
}
