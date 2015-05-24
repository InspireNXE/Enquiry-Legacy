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
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.engine.SearchResult;
import org.inspirenxe.enquiry.api.event.SearchEngineRegistrationEvent;
import org.inspirenxe.enquiry.api.event.SearchFailureEvent;
import org.inspirenxe.enquiry.api.event.SearchPreEvent;
import org.inspirenxe.enquiry.api.event.SearchSuccessEvent;
import org.inspirenxe.enquiry.engine.BingEngine;
import org.inspirenxe.enquiry.engine.GoogleEngine;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
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

    @Inject public Game game;
    @Inject public Logger logger;

    public CommentedConfigurationNode rootNode;

    @DefaultConfig(sharedRoot = true)
    @Inject private File defaultConfig;

    @DefaultConfig(sharedRoot = true)
    @Inject private ConfigurationLoader<CommentedConfigurationNode> loader;

    private final Set<SearchEngine> engines = Sets.newHashSet();

    @Subscribe
    public void onServerAboutToStart(ServerAboutToStartEvent event) throws IOException {
        // Setup configuration
        if (!defaultConfig.exists()) {
            this.defaultConfig.createNewFile();
            this.rootNode = this.loader.createEmptyNode(ConfigurationOptions.defaults());
            this.rootNode.getNode("bing").setComment("For help getting the api key and search engine id, please reference the wiki guide for "
                    + "setting up Bing <https://github.com/InspireNXE/Enquiry/wiki/Bing-(Setup)>");
            this.rootNode.getNode("bing", "app-id").setValue("");
            this.rootNode.getNode("google").setComment("For help getting the api key and search engine id, please reference the wiki guide for "
                    + "setting up Google <https://github.com/InspireNXE/Enquiry/wiki/Google-(Setup)>");
            this.rootNode.getNode("google", "api-key").setValue("");
            this.rootNode.getNode("google", "search-id").setValue("");
            this.loader.save(rootNode);
        }
        this.rootNode = this.loader.load();

        // Fire SearchEngineRegistrationEvent to register search engines
        this.game.getEventManager().post(new SearchEngineRegistrationEvent(engines));

        // Register commands for registered engines
        final Map<List<String>, CommandSpec> children = Maps.newHashMap();
        for (SearchEngine engine : engines) {
            this.game.getCommandDispatcher().register(this, engine.getCommandSpec(), engine.getAliases());
            children.put(engine.getAliases(), engine.getCommandSpec());
            this.logger.info("Registered [" + Texts.toPlain(engine.getName()) + "] with aliases " + engine.getAliases());
        }
        this.game.getCommandDispatcher().register(this, CommandSpec.builder().children(children).build(), "enquiry", "eq");
    }

    @Subscribe(order = Order.PRE)
    public void onSearchEngineRegistration(SearchEngineRegistrationEvent event) {
        new BingEngine(this, "bing", "b").register();
        new GoogleEngine(this, "google", "g").register();
    }

    public SearchEngine putEngine(SearchEngine engine) {
        this.engines.add(engine);
        return engine;
    }

    public static class SearchCommandExecutor implements CommandExecutor {
        private final Enquiry enquiry;
        private final SearchEngine engine;

        public SearchCommandExecutor(Enquiry enquiry, SearchEngine engine) {
            this.enquiry = enquiry;
            this.engine = engine;
        }

        @Override
        public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
            this.enquiry.game.getAsyncScheduler().runTask(this.enquiry, new Runnable() {
                @Override
                public void run() {
                    final CommandSource target = args.<CommandSource>getOne("player").get();
                    final String query = args.<String>getOne("search").get();
                    final SearchPreEvent preEvent = new SearchPreEvent(target, engine, query);
                    if (!enquiry.game.getEventManager().post(preEvent)) {
                        try {
                            final List<? extends SearchResult> results = preEvent.engine.getResults(query);
                            final SearchSuccessEvent event = new SearchSuccessEvent(target, preEvent.engine, query, results);
                            if (!enquiry.game.getEventManager().post(event)) {
                                target.sendMessage(Texts.of(
                                        "(", Texts.of(event.engine.getName()).builder()
                                                .onClick(new ClickAction.OpenUrl(new URL(event.engine.getUrl())))
                                                .onHover(new HoverAction.ShowText(Texts.of(event.engine.getUrl())))
                                                .build(),
                                        TextColors.RESET, ") Result(s) for: ", TextColors.YELLOW, query));
                                int i = 1;
                                for (SearchResult result : event.results) {
                                    target.sendMessage(Texts.of(i++, ". ", TextColors.GRAY, result.getTitle()).builder()
                                            .onClick(new ClickAction.OpenUrl(new URL(result.getUrl())))
                                            .onHover(new HoverAction.ShowText(
                                                    Texts.of(result.getDescription().replaceAll("(.{1,70})( +|$\\n?)|(.{1,70})", "$1$3\n").trim())))
                                            .build());
                                }
                            }
                        } catch (IOException e) {
                            if (!enquiry.game.getEventManager().post(new SearchFailureEvent(target, preEvent.engine, query))) {
                                target.sendMessage(
                                        Texts.of("An error occurred while attempting to search ", preEvent.engine.getName(), " for ", TextColors
                                                .YELLOW, query));
                                enquiry.logger.warn("An error occurred while attempting to search " + Texts.toPlain(preEvent.engine
                                        .getName()) + " for " + query, e);
                            }
                        }
                    }
                }
            });

            return CommandResult.success();
        }
    }
}
