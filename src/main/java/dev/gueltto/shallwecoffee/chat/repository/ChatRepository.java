package dev.gueltto.shallwecoffee.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    List<ChatEntity> findByDeadlineEquals(LocalDate target);
}
