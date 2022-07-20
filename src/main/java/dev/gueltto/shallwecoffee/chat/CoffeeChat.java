package dev.gueltto.shallwecoffee.chat;

import dev.gueltto.shallwecoffee.chat.repository.ChatEntity;
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

    /**
     * 리스트 표현이 불가능하여 '•' 문자를 직접 출력하는 형태로 사용
     * <a href="https://api.slack.com/reference/surfaces/formatting#block-formatting">참고</a>
     */
    public String toMessage() {
        StringBuilder message = new StringBuilder();
        message.append(announcement);
        message.append("\n\n• *커피챗 희망 날짜:* " + chatDate);
        message.append("\n• *모집 마감일:* " + deadline);
        message.append("\n• *예상 장소:* " + place);
        message.append("\n• *요청자:* " + slackMember.toMention());
        message.append("\n\n참석 이모지를 남겨주세요!");

        return message.toString();
    }

    public ChatEntity toEntity() {
        return ChatEntity.create(slackMember.getId(), channelId, chatDate, deadline);
    }
}
