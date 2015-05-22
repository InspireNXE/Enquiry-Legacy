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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.engine.SearchResult;
import org.inspirenxe.enquiry.api.event.SearchFailureEvent;
import org.inspirenxe.enquiry.api.event.SearchPreEvent;
import org.inspirenxe.enquiry.api.event.SearchSuccessEvent;
import org.inspirenxe.enquiry.engine.BingEngine;
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
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Plugin(id = "enquiry", name = "Enquiry", version = "1.0")
@NonnullByDefault
public class Enquiry {

    private static final Set<SearchEngine> engines = Sets.newHashSet();

    private static Enquiry instance;

    @Inject public Game game;
    @Inject public Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    public File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public CommentedConfigurationNode rootNode;

    @Subscribe
    public void onConstruction(ConstructionEvent event) {
        instance = this;
    }

    @Subscribe
    public void onServerAboutToStart(ServerAboutToStartEvent event) throws IOException {
        // Setup configuration
        if (!defaultConfig.exists()) {
            this.defaultConfig.createNewFile();
            this.rootNode = this.configManager.load();
            this.rootNode.getNode("bing", "app-id")
                    .setValue("")
                    .setComment("The app ID from your Microsoft account <https://msdn.microsoft.com/en-us/library/dd251020.aspx>");
            this.rootNode.getNode("google", "api-key")
                    .setValue("")
                    .setComment("The API key from your Google account <https://developers.google.com/console/help/#generatingdevkeys>");
            this.rootNode.getNode("google", "search-id")
                    .setValue("")
                    .setComment("The custom search engine ID from your Google account <https://support.google"
                            + ".com/customsearch/answer/2649143?hl=en>");
            this.configManager.save(rootNode);
        }
        this.rootNode = this.configManager.load();

        // Register default engines
        registerEngines(new BingEngine(), new GoogleEngine());

        final Map<List<String>, CommandSpec> children = Maps.newHashMap();
        for (SearchEngine engine : engines) {
            this.game.getCommandDispatcher().register(this, engine.getCommandSpec(), engine.getAliases());
            children.put(engine.getAliases(), engine.getCommandSpec());
        }
        this.game.getCommandDispatcher().register(this, CommandSpec.builder().children(children).build(), "enquiry", "eq");
    }

    public static Enquiry getInstance() {
        return instance;
    }

    public static void registerEngines(SearchEngine... searchEngines) {
        for (SearchEngine engine : searchEngines) {
            engines.add(engine);
            Enquiry.getInstance().logger.info("Registered " + engine.getName());
        }
    }

    public static class SearchCommandExecutor implements CommandExecutor {
        private SearchEngine engine;

        public SearchCommandExecutor(SearchEngine engine) {
            this.engine = engine;
        }

        @Override
        public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
            getInstance().game.getAsyncScheduler().runTask(getInstance(), new Runnable() {
                @Override
                public void run() {
                    final String query = args.<String>getOne("search").get();
                    final SearchPreEvent preEvent = new SearchPreEvent(src, engine, query);
                    if (!getInstance().game.getEventManager().post(preEvent)) {
                        try {
                            final List<? extends SearchResult> results = preEvent.engine.getResults(query);
                            final SearchSuccessEvent event = new SearchSuccessEvent(src, preEvent.engine, query, results);
                            if (!getInstance().game.getEventManager().post(event)) {
                                src.sendMessage(Texts.of(
                                        "(", Texts.of(event.engine.getName()).builder()
                                                .onClick(new ClickAction.OpenUrl(new URL(event.engine.getUrl())))
                                                .onHover(new HoverAction.ShowText(Texts.of(event.engine.getUrl())))
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
                            if (!getInstance().game.getEventManager().post(new SearchFailureEvent(src, preEvent.engine, query))) {
                                src.sendMessage(Texts.of("An error occurred while attempting to search ", preEvent.engine.getName(), " for ", TextColors
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
