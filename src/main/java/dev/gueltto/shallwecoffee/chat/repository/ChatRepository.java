package dev.gueltto.shallwecoffee.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
}
