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

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import ninja.leaping.configurate.Types;
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
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
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
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Plugin(id = "enquiry", name = "Enquiry", version = "1.1")
@NonnullByDefault
public class Enquiry {

    @Inject public Game game;
    @Inject public Logger logger;

    public Storage storage;

    @DefaultConfig(sharedRoot = true)
    @Inject private File configuration;
    @DefaultConfig(sharedRoot = true)
    @Inject private ConfigurationLoader<CommentedConfigurationNode> loader;

    private final Set<SearchEngine> engines = Sets.newHashSet();

    @Subscribe
    public void onServerAboutToStart(ServerAboutToStartEvent event) throws IOException {
        storage = new Storage(configuration, loader).load();

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
        final List<String> bingAliases = storage.getChildNode("engines.bing.options.aliases").getList(Types::asString);
        new BingEngine(this, "bing", bingAliases.toArray(new String[bingAliases.size()])).register();

        final List<String> googleAliases = storage.getChildNode("engines.google.options.aliases").getList(Types::asString);
        new GoogleEngine(this, "google", googleAliases.toArray(new String[googleAliases.size()])).register();
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
        @SuppressWarnings("deprecation")
        public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
            this.enquiry.game.getAsyncScheduler().runTask(this.enquiry, () -> {
                final CommandSource target = args.<Player>getOne("player").get();
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
                                final String resultMessage = enquiry.storage.getChildNode("engines." + event.engine.getId() + ".options.style"
                                        + ".line-format").getString()
                                        .replace("${resultNumber}", Integer.toString(i++))
                                        .replace("${resultTitle}", result.getTitle())
                                        .trim();
                                target.sendMessage(Texts.fromLegacy(resultMessage, '&').builder()
                                        .onClick(new ClickAction.OpenUrl(new URL(result.getUrl())))
                                        .onHover(new HoverAction.ShowText(Texts.of(result.getDescription().replaceAll("\\p{C}", "").trim()))).build());
                            }
                        }
                    } catch (IOException e) {
                        if (!enquiry.game.getEventManager().post(new SearchFailureEvent(target, preEvent.engine, query))) {
                            if (src instanceof Player) {
                                src.sendMessage(Texts.of("An error occurred while attempting to search ", preEvent.engine.getName(), " for ",
                                        TextColors.YELLOW, query));
                            }
                            enquiry.logger.warn("An error occurred while attempting to search " + Texts.toPlain(preEvent.engine.getName()) +
                                    " for " + query, e);
                        }
                    }
                }
            });

            return CommandResult.success();
        }
    }
}
