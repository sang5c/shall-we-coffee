package dev.gueltto.shallwecoffee.bolt;

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

    private final CoffeeChatService coffeeChatService;

    @Bean
    public App initSlackApp() {
        App app = new App();
        // app.command("/hello", (req, ctx) -> ctx.ack("Hi there!"));

        app.globalShortcut(CHAN_CHAT_MESSAGE, openScheduleModal());


        app.globalShortcut(COFFEE_MESSAGE, openModal());
        app.viewSubmission(MESSAGE_SUBMIT, (req, ctx) -> {
            Map<String, Map<String, ViewState.Value>> values = req.getPayload().getView().getState().getValues();
            log.debug("values: " + values);

            String count = values.get(SELECTION_ID).get(SELECTION_ACTION_ID).getSelectedOption().getValue();
            String inputValue = values.get(INPUT_ID).get(INPUT_ACTION_ID).getValue();
            log.info("count: {}, notice: {}", count, inputValue);

            coffeeChatService.startCoffeeChat(Integer.parseInt(count), inputValue);
            return ctx.ack();
        });
        // warn 출력을 막기 위한 ack
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
                logger.info("response: {}", response);
            } catch (IOException | SlackApiException e) {
                logger.error("error: {}", e.getMessage(), e);
            }
            return ctx.ack();
        };
    }

    private View buildScheduleView() {
        return view(view -> view.callbackId(CHAN_CHAT_MESSAGE_SUBMIT)
                .type("modal")
                .title(viewTitle(titleBuilder -> titleBuilder.type(PLAIN_TEXT).text("커피 한잔 할래요? (해당 채널)").emoji(true)))
                .submit(viewSubmit(submitBuilder -> submitBuilder.type(PLAIN_TEXT).text("제출하기").emoji(true)))
                .close(viewClose(closeBuilder -> closeBuilder.type(PLAIN_TEXT).text("닫기").emoji(true)))
                .blocks(
                        asBlocks(
                                section(section -> section
                                        .blockId("CHAT_DATE_ID")
                                        .text(markdownText("모일 날짜는 언제인가요?"))
                                        .accessory(
                                                datePicker(datePickerElementBuilder -> datePickerElementBuilder
                                                        .actionId("DATE_ACTION_ID")
                                                        .initialDate(LocalDateTime.now().toLocalDate().toString())
                                                        .placeholder(plainText(pt -> pt.text("text").emoji(true)))
                                                )
                                        )
                                ),
                                section(section -> section
                                        .blockId("DEADLINE_DATE_ID")
                                        .text(markdownText("참가자를 언제까지 받을까요?"))
                                        .accessory(
                                                datePicker(datePickerElementBuilder -> datePickerElementBuilder
                                                        .actionId("DEADLINE_DATE_ACTION_ID")
                                                        .initialDate(LocalDateTime.now().toLocalDate().toString())
                                                        .placeholder(plainText(pt -> pt.text("text").emoji(true)))
                                                )
                                        )
                                ),
                                // section(section -> section
                                //         .blockId(SELECTION_ID)
                                //         .text(markdownText("몇 명이서 모일까요?"))
                                //         .accessory(staticSelect(staticSelect -> staticSelect
                                //                 .actionId(SELECTION_ACTION_ID)
                                //                 .placeholder(plainText("N명"))
                                //                 .options(asOptions(
                                //                         // option(plainText("1"), "1"),
                                //                         // option(plainText("2"), "2"),
                                //                         option(plainText("3"), "3"), // TODO: 2명 이하는 불가능해야 한다. 최대값은?
                                //                         option(plainText("4"), "4")
                                //                 )) // TODO: 옵션으로 제공할지, 숫자 입력 받을지
                                //         ))
                                // ),
                                //    - [ ] 날짜 / 시간
                                //{
                                // 					"type": "datepicker",
                                // 					"initial_date": "1990-04-28",
                                // 					"placeholder": {
                                // 						"type": "plain_text",
                                // 						"text": "Select a date",
                                // 						"emoji": true
                                // 					},
                                // 					"action_id": "actionId-0"
                                // 				},
                                //     - [ ] 예상 장소
                                //  마감시간
                                input(input -> input.blockId(INPUT_ID)
                                        .element(plainTextInput(inputBuilder -> inputBuilder.actionId(INPUT_ACTION_ID).multiline(true)))
                                        .label(plainText(pt -> pt.text("하고싶은 말").emoji(true)))
                                )
                        )
                )
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

