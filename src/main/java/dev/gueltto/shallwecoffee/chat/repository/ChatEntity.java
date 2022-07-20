package dev.gueltto.shallwecoffee.chat.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Chat")
public class ChatEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @Column(name = "chat_id")
    private Long id;

    private String userId;
    private String channelId;
    private LocalDate chatDate;
    private LocalDate deadline;

    private ChatEntity(String userId, String channelId, LocalDate chatDate, LocalDate deadline) {
        this.userId = userId;
        this.channelId = channelId;
        this.chatDate = chatDate;
        this.deadline = deadline;
    }

    public static ChatEntity create(String userId, String channelId, LocalDate chatDate, LocalDate deadline) {
        return new ChatEntity(userId, channelId, chatDate, deadline);
    }

    public String getChannelId() {
        return channelId;
    }
}
