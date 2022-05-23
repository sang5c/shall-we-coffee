package dev.gueltto.shallwecoffee.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.methods.SlackApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
public class ChatController {

    private final SlackService slackService;
    private final ObjectMapper objectMapper;

    @PostMapping("/test")
    public ResponseEntity<String> ping(@RequestParam Map<String, String> payload) throws IOException, SlackApiException {
        String payload1 = payload.get("payload");

        log.info("PARAMS: " + payload1);

        Map<String, Object> params = objectMapper.readValue(payload1, new TypeReference<>() {});
        log.info("PARAMS: " + params);

        slackService.pingPong((String) params.get("trigger_id"));
        return ResponseEntity.ok().build();
    }
}
