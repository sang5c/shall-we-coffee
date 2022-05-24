package dev.gueltto.shallwecoffee.slack;

import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.GlobalShortcutHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import dev.gueltto.shallwecoffee.chat.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class SlackApp {

    private static final String COFFEE_MESSAGE = "coffee_message";
    private static final String MESSAGE_SUBMIT = "coffee_message_submit";
    private static final String PLAIN_TEXT = "plain_text";
    private static final String INPUT_ID = "input_id";
    private static final String INPUT_ACTION_ID = "input_action_id";
    private static final String SELECTION_ID = "count-block";
    private static final String SELECTION_ACTION_ID = "count-selection-action";

    private final SlackService slackService;
    @Bean
    public App initSlackApp() {
        App app = new App();
        app.command("/hello", (req, ctx) -> ctx.ack("Hi there!"));

        app.globalShortcut(COFFEE_MESSAGE, openModal());
        app.viewSubmission(MESSAGE_SUBMIT, (req, ctx) -> {
            // Sent inputs: req.getPayload().getView().getState().getValues()
            Map<String, Map<String, ViewState.Value>> values = req.getPayload().getView().getState().getValues();
            ctx.logger.info("values: " + values.get(SELECTION_ID));
            String count = values.get(SELECTION_ID).get(SELECTION_ACTION_ID).getSelectedOption().getValue();
            ctx.logger.info("values: " + count);

            ctx.logger.info("values: " + values.get(INPUT_ID));
            String inputValue = values.get(INPUT_ID).get(INPUT_ACTION_ID).getValue();
            ctx.logger.info("values: " + inputValue);

            slackService.sendMessage("CSCB3M43G", count + " hello world! " + inputValue);
            return ctx.ack();
        });
        app.blockAction(SELECTION_ACTION_ID, (req, ctx) -> ctx.ack());

        app.command("/meeting", (req, ctx) -> {
            ViewsOpenResponse viewsOpenRes = ctx.client().viewsOpen(r -> r
                    .triggerId(ctx.getTriggerId())
                    .view(createSubmitSample()));
            if (viewsOpenRes.isOk()) return ctx.ack();
            else return Response.builder().statusCode(500).body(viewsOpenRes.getError()).build();
        });
        app.command("/ping", (req, ctx) -> {
            return ctx.ack(asBlocks(
                    section(section -> section.text(markdownText(":wave: pong"))),
                    actions(actions -> actions
                            .elements(asElements(
                                    button(b -> b.actionId("ping-again").text(plainText(pt -> pt.text("Ping"))).value("ping"))
                            ))
                    )
            ));
        });
        return app;
    }

    @NotNull
    private GlobalShortcutHandler openModal() {
        return (req, ctx) -> {
            var logger = ctx.logger;
            try {
                var payload = req.getPayload();
                // Call the conversations.create method using the built-in WebClient
                var result = ctx.client()
                        .viewsOpen(r -> r.triggerId(payload.getTriggerId())
                                .view(buildView())
                        );
                // Print result
                logger.info("result: {}", result);
            } catch (IOException | SlackApiException e) {
                logger.error("error: {}", e.getMessage(), e);
            }
            return ctx.ack();
        };
    }

    private View createSubmitSample() {
        return view(v -> v
                .type("modal")
                .submit(viewSubmit(vs -> vs.type("plain_text").text("Start")))
                .blocks(asBlocks(
                        section(s -> s
                                .text(plainText("The channel we'll post the result"))
                                .accessory(conversationsSelect(conv -> conv
                                        .actionId("notification_conv_id")
                                        .responseUrlEnabled(true)
                                        .defaultToCurrentConversation(true)
                                ))
                        )
                )));
    }

    private View createModalSample() {
        return view(v -> v
                .type("modal")
                .title(viewTitle(vt -> vt.type("plain_text").text("My App")))
                .close(viewClose(vc -> vc.type("plain_text").text("Close")))
                .blocks(asBlocks(
                        section(s -> s.text(markdownText(mt ->
                                mt.text("About the simplest modal you could conceive of :smile:\\n\\nMaybe <https://api.slack.com/reference/block-kit/interactive-components|*make the modal interactive*> or <https://api.slack.com/surfaces/modals/using#modifying|*learn more advanced modal use cases*>.")))),
                        context(c -> c.elements(asContextElements(
                                markdownText("Psssst this modal was designed using <https://api.slack.com/tools/block-kit-builder|*Block Kit Builder*>")
                        )))
                ))
        );
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
                                                option(plainText("1"), "1"),
                                                option(plainText("2"), "2"),
                                                option(plainText("3"), "3"),
                                                option(plainText("4"), "4")
                                        ))
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

