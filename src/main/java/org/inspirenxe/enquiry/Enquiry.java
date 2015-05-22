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
import org.inspirenxe.enquiry.api.event.SearchFailureEvent;
import org.inspirenxe.enquiry.api.event.SearchPreEvent;
import org.inspirenxe.enquiry.api.event.SearchSuccessEvent;
import org.inspirenxe.enquiry.engine.BingEngine;
import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.engine.SearchResult;
import org.inspirenxe.enquiry.engine.GoogleEngine;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ConstructionEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
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
import java.util.List;

import javax.xml.ws.http.HTTPException;

@Plugin(id = "enquiry", name = "Enquiry", version = "1.0")
@NonnullByDefault
public class Enquiry {

    public static final BingEngine BING_SEARCH_ENGINE = new BingEngine();
    public static final GoogleEngine GOOGLE_SEARCH_ENGINE = new GoogleEngine();

    private static Enquiry instance;

    @Inject public Game game;
    @Inject public Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true) public File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public CommentedConfigurationNode bingConfigurationNode;
    public String bingAppId;

    public CommentedConfigurationNode googleConfigurationNode;
    public String googleApiKey;
    public String googleSearchId;

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
            config.getNode("bing", "app-id")
                    .setValue("")
                    .setComment("The app ID from your Microsoft account <https://msdn.microsoft.com/en-us/library/dd251020.aspx>");
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
        bingConfigurationNode = config.getNode("bing");
        bingAppId = bingConfigurationNode.getNode("app-id").getString("");

        googleConfigurationNode = config.getNode("google");
        googleApiKey = googleConfigurationNode.getNode("api-key").getString("");
        googleSearchId = googleConfigurationNode.getNode("search-id").getString("");

        // Register commands
        final CommandSpec bingCommand = CommandSpec.builder()
                .description(Texts.of("Searches ", BING_SEARCH_ENGINE.getName(), " for the query provided."))
                .arguments(GenericArguments.seq(GenericArguments.playerOrSource(Texts.of(TextColors.AQUA, "player"), game),
                        GenericArguments.remainingJoinedStrings(Texts.of(TextColors.GOLD, "search"))))
                .permission("enquiry.command.search.bing")
                .executor(new SearchCommandExecutor(BING_SEARCH_ENGINE))
                .build();
        final CommandSpec googleCommand = CommandSpec.builder()
                .description(Texts.of("Searches ", GOOGLE_SEARCH_ENGINE.getName(), " for the query provided."))
                .arguments(GenericArguments.seq(GenericArguments.playerOrSource(Texts.of(TextColors.AQUA, "player"), game),
                        GenericArguments.remainingJoinedStrings(Texts.of(TextColors.GOLD, "search"))))
                .permission("enquiry.command.search.google")
                .executor(new SearchCommandExecutor(GOOGLE_SEARCH_ENGINE))
                .build();
        game.getCommandDispatcher().register(this,
                CommandSpec.builder()
                        .child(bingCommand, "bing", "b")
                        .child(googleCommand, "google", "g")
                        .build(),
                "enquiry", "eq");
        game.getCommandDispatcher().register(this, bingCommand, "bing", "b", "eqb");
        game.getCommandDispatcher().register(this, googleCommand, "google", "g", "eqg");
    }

    public static Enquiry getInstance() {
        return instance;
    }

    private class SearchCommandExecutor implements CommandExecutor {
        private SearchEngine engine;

        private SearchCommandExecutor(SearchEngine engine) {
            this.engine = engine;
        }

        @Override
        public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
            Enquiry.getInstance().game.getAsyncScheduler().runTask(Enquiry.getInstance(), new Runnable() {
                @Override
                public void run() {
                    final String query = args.<String>getOne("search").get();
                    final SearchPreEvent preEvent = new SearchPreEvent(src, engine, query);
                    if (!Enquiry.getInstance().game.getEventManager().post(preEvent)) {
                        try {
                            engine = preEvent.engine;
                            final List<? extends SearchResult> results = engine.getResults(query);
                            final SearchSuccessEvent event = new SearchSuccessEvent(src, engine, query, results);
                            if (!Enquiry.getInstance().game.getEventManager().post(event)) {
                                src.sendMessage(Texts.of(
                                        "(", Texts.of(engine.getName()).builder()
                                                .onClick(new ClickAction.OpenUrl(new URL(engine.getUrl())))
                                                .onHover(new HoverAction.ShowText(Texts.of(engine.getUrl())))
                                                .build(),
                                        TextColors.RESET, ") Result(s) for: ", TextColors.YELLOW, query));
                                int i = 1;
                                for (SearchResult result : event.results) {
                                    src.sendMessage(Texts.of(i++, ". ", TextColors.GRAY, result.getTitle()).builder()
                                            .onClick(new ClickAction.OpenUrl(new URL(result.getUrl())))
                                            .onHover(new HoverAction.ShowText(
                                                    Texts.of(result.getDescription().replaceAll("(.{1,70})( +|$\\n?)|(.{1,70})", "$1$3\n").trim())))
                                            .build());
                                }
                            }
                        } catch (IOException e) {
                            if (!Enquiry.getInstance().game.getEventManager().post(new SearchFailureEvent(src, engine, query))) {
                                src.sendMessage(Texts.of("An error occurred while attempting to search ", engine.getName(), " for ", TextColors
                                        .YELLOW, query));
                            }
                        }
                    }
                }
            });

            return CommandResult.success();
        }
    }
}
