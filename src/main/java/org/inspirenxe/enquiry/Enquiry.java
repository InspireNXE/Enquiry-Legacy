/**
 * This file is part of Enquiry, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <http://github.com/InspireNXE/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.enquiry;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.inspirenxe.enquiry.api.SearchFailureEvent;
import org.inspirenxe.enquiry.api.SearchPreEvent;
import org.inspirenxe.enquiry.api.SearchSuccessEvent;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ConstructionEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Plugin(id = "enquiry", name = "Enquiry", version = "1.0")
@NonnullByDefault
public class Enquiry {

    public static final String GOOGLE_URL = "https://www.googleapis.com/customsearch/v1";

    private static Enquiry instance;

    @Inject public Game game;
    @Inject public Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private int googleConnectionTimeout;
    private String googleApiKey;
    private String googleSearchId;

    @Subscribe
    public void onConstruction(ConstructionEvent event) {
        instance = this;
    }

    @Subscribe
    public void onServerAboutToStart(ServerAboutToStartEvent event) throws IOException {
        // Setup configuration
        CommentedConfigurationNode config;
        if (!defaultConfig.exists()) {
            defaultConfig.createNewFile();
            config = configManager.load();
            config.getNode("google", "api-key")
                    .setValue("")
                    .setComment("The API key from your Google account <https://developers.google.com/console/help/#generatingdevkeys>");
            config.getNode("google", "search-id")
                    .setValue("")
                    .setComment("The custom search engine ID from your Google account <https://support.google"
                            + ".com/customsearch/answer/2649143?hl=en>");
            configManager.save(config);
        }
        config = configManager.load();
        googleConnectionTimeout = config.getNode("connection-timeout").getInt(100);
        googleApiKey = config.getNode("google", "api-key").getString("");
        googleSearchId = config.getNode("google", "search-id").getString("");

        // Register commands
        game.getCommandDispatcher().register(this,
                CommandSpec.builder()
                        .child(CommandSpec.builder()
                                        .description(Texts.of("Searches Google for the query provided"))
                                        .arguments(GenericArguments.remainingJoinedStrings(Texts.of("search")))
                                        .permission("enquiry.command.search.google")
                                        .executor(new GoogleCommandExecutor())
                                        .build(),
                                "google", "g")
                        .build(),
                "enquiry", "eq");
    }

    public static Enquiry getInstance() {
        return instance;
    }

    private class GoogleCommandExecutor implements CommandExecutor {

        @Override
        public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
            Enquiry.getInstance().game.getAsyncScheduler().runTask(Enquiry.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (!Enquiry.getInstance().game.getEventManager().post(new SearchPreEvent(src))) {
                        final String query = args.<String>getOne("search").get();
                        try {
                            final List<Text> results = searchGoogle(query);
                            final SearchSuccessEvent event = new SearchSuccessEvent(src, results);
                            if (!Enquiry.getInstance().game.getEventManager().post(event)) {
                                src.sendMessage(Texts.of("Results for: ", TextColors.YELLOW, query));
                                for (Text text : results) {
                                    src.sendMessage(text);
                                }
                            }
                        } catch (IOException e) {
                            if (!Enquiry.getInstance().game.getEventManager().post(new SearchFailureEvent(src))) {
                                src.sendMessage(Texts.of(TextColors.RED, "Unable to search for: ", TextColors.YELLOW, query, TextColors.RESET,
                                        TextColors.RED, "\nError: ", TextColors.GRAY, e.getLocalizedMessage()));
                            }
                        }
                    }
                }
            });

            return CommandResult.success();
        }
    }

    private List<Text> searchGoogle(String query) throws IOException {
        if (googleApiKey.isEmpty() || googleSearchId.isEmpty()) {
            throw new IOException("The Google API or search ID are required in order to perform a Google search.");
        }

        final String jsonResponse = HttpRequest.get(GOOGLE_URL, true,
                "key", googleApiKey,
                "cx", googleSearchId,
                "fields", "items(title,link,snippet)",
                "q", query)
                .accept("application/json")
                .acceptCharset(StandardCharsets.UTF_8.name())
                .body();
        final GoogleResponse response = new Gson().fromJson(jsonResponse, GoogleResponse.class);
        final List<Text> results = new CopyOnWriteArrayList<>();
        int i = 1;
        for (GoogleResult result : response.results) {
            results.add(Texts.of(i++, ". ", TextColors.GRAY, result.title).builder()
                    .onClick(new ClickAction.OpenUrl(new URL(result.url)))
                    .onHover(new HoverAction.ShowText(Texts.of(result.description)))
                    .build());
        }
        return results;
    }
}
