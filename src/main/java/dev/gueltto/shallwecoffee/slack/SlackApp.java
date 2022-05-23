package dev.gueltto.shallwecoffee.slack;

import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.GlobalShortcutHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.view.View;
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

@Slf4j
@Configuration
public class SlackApp {
    @Bean
    public App initSlackApp() {
        App app = new App();
        app.command("/hello", (req, ctx) -> ctx.ack("Hi there!"));
        app.globalShortcut("coffee_message", openModal());

        app.viewSubmission("coffee_message_submit", (req, ctx) -> {
            // Sent inputs: req.getPayload().getView().getState().getValues()
            ctx.logger.info("req.getPayload().getView().getState().getValues() = " + req.getPayload().getView().getState().getValues());


            // TODO: send message
            app.getClient().chatPostMessage(requestBuilder -> requestBuilder.text("hello world!")
                    .channel("CSCB3M43G") // TODO: find channel
            );
            return ctx.ack();
        });

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
                .callbackId("coffee_message_submit")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Meeting Arrangement").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .privateMetadata("{\"response_url\":\"https://hooks.slack.com/actions/T1ABCD2E12/330361579271/0dAEyLY19ofpLwxqozy3firz\"}")
                .blocks(asBlocks(
                        section(section -> section
                                .blockId("category-block")
                                .text(markdownText("Select a category of the meeting!"))
                                .accessory(staticSelect(staticSelect -> staticSelect
                                        .actionId("category-selection-action")
                                        .placeholder(plainText("Select a category"))
                                        .options(asOptions(
                                                option(plainText("Customer"), "customer"),
                                                option(plainText("Partner"), "partner"),
                                                option(plainText("Internal"), "internal")
                                        ))
                                ))
                        ),
                        input(input -> input
                                .blockId("agenda-block")
                                .element(plainTextInput(pti -> pti.actionId("agenda-action").multiline(true)))
                                .label(plainText(pt -> pt.text("Detailed Agenda").emoji(true)))
                        )
                ))
        );
    }
}
