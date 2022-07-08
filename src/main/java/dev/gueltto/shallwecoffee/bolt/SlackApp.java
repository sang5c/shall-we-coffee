package dev.gueltto.shallwecoffee.bolt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.app_backend.interactive_components.payload.GlobalShortcutPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.GlobalShortcutHandler;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import dev.gueltto.shallwecoffee.chat.CoffeeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class SlackApp {

    // 전체 커피챗 이벤트 ID
    private static final String COFFEE_MESSAGE = "coffee_message";
    private static final String MESSAGE_SUBMIT = "coffee_message_submit";
    private static final String PLAIN_TEXT = "plain_text";
    private static final String INPUT_ID = "input_id";
    private static final String INPUT_ACTION_ID = "input_action_id";
    private static final String SELECTION_ID = "count-block";
    private static final String SELECTION_ACTION_ID = "count-selection-action";

    // 특정 채널 커피챗 이벤트 ID
    private static final String CHAN_CHAT_MESSAGE = "channel_coffee_chat";
    private static final String CHAN_CHAT_MESSAGE_SUBMIT = "channel_coffee_chat_submit";
    private static final String PLACE_INPUT_ID = "place_input";
    private static final String PLACE_INPUT_ACTION_ID = "place_input_action";
    private static final String ANNOUNCEMENT_INPUT_ID = "announcement_input";
    private static final String ANNOUNCEMENT_INPUT_ACTION_ID = "announcement_input_action";

    private static final String CHAT_DATE_ID = "CHAT_DATE_ID";
    private static final String CHAT_DATE_ACTION_ID = "CHAT_DATE_ACTION_ID";
    private static final String DEADLINE_DATE_ID = "DEADLINE_DATE_ID";
    private static final String DEADLINE_DATE_ACTION_ID = "DEADLINE_DATE_ACTION_ID";
    private static final String SELECT_CHANNEL_ID = "SELECT_CHANNEL_ID";
    private static final String SELECT_CHANNEL_ACTION_ID = "SELECT_CHANNEL_ACTION_ID";

    private final CoffeeChatService coffeeChatService;

    @Bean
    public App initSlackApp() {
        App app = new App();
        // 채널 선택하여 커피챗
        app.globalShortcut(CHAN_CHAT_MESSAGE, openScheduleModal());
        app.viewSubmission(CHAN_CHAT_MESSAGE_SUBMIT, (req, ctx) -> {
            Map<String, Map<String, ViewState.Value>> values = req.getPayload().getView().getState().getValues();
            // TODO: Object.fromValues() 변환해서 사용하기
            StringBuilder message = new StringBuilder();
            String requestUserId = req.getPayload().getUser().getId();
            message.append("user: " + requestUserId + "\n");
            String selectedChannel = values.get(SELECT_CHANNEL_ID).get(SELECT_CHANNEL_ACTION_ID).getSelectedChannel();
            message.append("선택한 채널: " + selectedChannel + "\n");
            String selectedMeetingDate = values.get(CHAT_DATE_ID).get(CHAT_DATE_ACTION_ID).getSelectedDate();
            message.append("모임일: " + selectedMeetingDate + "\n");
            String selectedDeadlineDate = values.get(DEADLINE_DATE_ID).get(DEADLINE_DATE_ACTION_ID).getSelectedDate();
            message.append("마감일: " + selectedDeadlineDate + "\n");
            String place = values.get(PLACE_INPUT_ID).get(PLACE_INPUT_ACTION_ID).getValue();
            message.append("장소: " + place + "\n");
            String announcement = values.get(ANNOUNCEMENT_INPUT_ID).get(ANNOUNCEMENT_INPUT_ACTION_ID).getValue();
            message.append("하고싶은말: " + announcement + "\n");

            coffeeChatService.startCoffeeChat(selectedChannel, message.toString());
            // payload={"team":{"id":"T03CBP0L89L","domain":"geultto7"},
            // "user":{"id":"U03CM3U0LP6","username":"skah321","name":"skah321","team_id":"T03CBP0L89L"},
            return ctx.ack();
        });

        // 전체 "3_" 채널별 커피챗
        app.globalShortcut(COFFEE_MESSAGE, openModal());
        app.viewSubmission(MESSAGE_SUBMIT, (req, ctx) -> {
            Map<String, Map<String, ViewState.Value>> values = req.getPayload().getView().getState().getValues();
            log.debug("values: " + values);

            String count = values.get(SELECTION_ID).get(SELECTION_ACTION_ID).getSelectedOption().getValue();
            String inputValue = values.get(INPUT_ID).get(INPUT_ACTION_ID).getValue();
            log.info("count: {}, notice: {}", count, inputValue);

            coffeeChatService.startManagedCoffeeChat(Integer.parseInt(count), inputValue);
            return ctx.ack();
        });

        // warn 출력을 막기 위한 ack
        app.blockAction(SELECT_CHANNEL_ACTION_ID, (req, ctx) -> ctx.ack());
        app.blockAction(SELECTION_ACTION_ID, (req, ctx) -> ctx.ack());

        return app;
    }

    private GlobalShortcutHandler openScheduleModal() {
        return (req, ctx) -> {
            var logger = ctx.logger;
            try {
                GlobalShortcutPayload payload = req.getPayload();
                // Call the conversations.create method using the built-in WebClient
                ViewsOpenResponse response = ctx.client()
                        .viewsOpen(r -> r.triggerId(payload.getTriggerId())
                                .view(buildScheduleView())
                        );
                // Print response
                // logger.info("response: {}", response);
            } catch (IOException | SlackApiException e) {
                logger.error("error: {}", e.getMessage(), e);
            }
            return ctx.ack();
        };
    }

    // TODO: 아이디 정리
    private View buildScheduleView() {
        return view(view -> view.callbackId(CHAN_CHAT_MESSAGE_SUBMIT)
                .type("modal")
                .title(viewTitle(titleBuilder -> titleBuilder.type(PLAIN_TEXT).text("커피 한잔 할래요? (해당 채널)").emoji(true)))
                .submit(viewSubmit(submitBuilder -> submitBuilder.type(PLAIN_TEXT).text("제출하기").emoji(true)))
                .close(viewClose(closeBuilder -> closeBuilder.type(PLAIN_TEXT).text("닫기").emoji(true)))
                .blocks(asBlocks(
                        input(sectionBuilder -> sectionBuilder.blockId(SELECT_CHANNEL_ID)
                                .label(plainText("커피챗 채널"))
                                .element(channelsSelect(channelsSelectBuilder -> channelsSelectBuilder.actionId(SELECT_CHANNEL_ACTION_ID)
                                                .placeholder(plainText("채널을 선택해주세요!"))
                                        )
                                )),
                        // 모임 날짜
                        input(input -> input.blockId(CHAT_DATE_ID)
                                .label(plainText(pt -> pt.text("모임 날짜는 언제인가요?").emoji(true)))
                                .element(datePicker(datePickerBuilder -> datePickerBuilder
                                                .actionId(CHAT_DATE_ACTION_ID)
                                                .initialDate(LocalDateTime.now().toLocalDate().toString())
                                        )
                                )
                        ),
                        // 마감일
                        input(input -> input.blockId(DEADLINE_DATE_ID)
                                .label(plainText(pt -> pt.text("참가자를 언제까지 받을까요?").emoji(true)))
                                .element(datePicker(datePickerBuilder -> datePickerBuilder
                                                .actionId(DEADLINE_DATE_ACTION_ID)
                                                .initialDate(LocalDateTime.now().plusDays(1).toLocalDate().toString())
                                        )
                                )
                        ),
                        // 장소
                        input(input -> input.blockId(PLACE_INPUT_ID)
                                .label(plainText(pt -> pt.text("예상 장소").emoji(true)))
                                .element(plainTextInput(inputBuilder -> inputBuilder.actionId(PLACE_INPUT_ACTION_ID)))
                        ),
                        // 하고싶은 말
                        input(input -> input.blockId(ANNOUNCEMENT_INPUT_ID)
                                .label(plainText(pt -> pt.text("하고싶은 말").emoji(true)))
                                .element(plainTextInput(inputBuilder -> inputBuilder.actionId(ANNOUNCEMENT_INPUT_ACTION_ID).multiline(true)))
                        )
                ))
        );
    }

    /**
     * <a href="https://api.slack.com/methods/views.open/code">API DOCS</a>
     */
    private GlobalShortcutHandler openModal() {
        return (req, ctx) -> {
            var logger = ctx.logger;
            try {
                GlobalShortcutPayload payload = req.getPayload();
                // Call the conversations.create method using the built-in WebClient
                ViewsOpenResponse response = ctx.client()
                        .viewsOpen(r -> r.triggerId(payload.getTriggerId())
                                .view(buildView())
                        );
                // Print response
                logger.info("response: {}", response);
            } catch (IOException | SlackApiException e) {
                logger.error("error: {}", e.getMessage(), e);
            }
            return ctx.ack();
        };
    }

    private View buildView() {
        return view(view -> view
                .callbackId(MESSAGE_SUBMIT)
                .type("modal")
                // .notifyOnClose(true)
                .title(viewTitle(titleBuilder -> titleBuilder.type(PLAIN_TEXT)
                        .text("커피 한잔 할래요?")
                        .emoji(true))
                )
                .submit(viewSubmit(submitBuilder -> submitBuilder.type(PLAIN_TEXT)
                        .text("제출하기")
                        .emoji(true))
                )
                .close(viewClose(closeBuilder -> closeBuilder.type(PLAIN_TEXT)
                        .text("닫기")
                        .emoji(true))
                )
                // .privateMetadata("{\"response_url\":\"https://hooks.slack.com/actions/T1ABCD2E12/330361579271/0dAEyLY19ofpLwxqozy3firz\"}")
                .blocks(asBlocks(
                        section(section -> section
                                .blockId(SELECTION_ID)
                                .text(markdownText("몇 명이서 모일까요?"))
                                .accessory(staticSelect(staticSelect -> staticSelect
                                        .actionId(SELECTION_ACTION_ID)
                                        .placeholder(plainText("N명"))
                                        .options(asOptions(
                                                // option(plainText("1"), "1"),
                                                // option(plainText("2"), "2"),
                                                option(plainText("3"), "3"), // TODO: 2명 이하는 불가능해야 한다. 최대값은?
                                                option(plainText("4"), "4")
                                        )) // TODO: 옵션으로 제공할지, 숫자 입력 받을지
                                ))
                        ),
                        input(input -> input
                                .blockId(INPUT_ID)
                                .element(plainTextInput(inputBuilder -> inputBuilder.actionId(INPUT_ACTION_ID)
                                        .multiline(true))
                                )
                                .label(plainText(pt -> pt.text("하고싶은 말").emoji(true)))
                        )
                ))
        );
    }
}

