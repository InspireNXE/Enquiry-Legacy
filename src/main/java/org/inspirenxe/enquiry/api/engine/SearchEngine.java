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
package org.inspirenxe.enquiry.api.engine;

import static org.spongepowered.api.util.command.args.GenericArguments.playerOrSource;
import static org.spongepowered.api.util.command.args.GenericArguments.remainingJoinedStrings;
import static org.spongepowered.api.util.command.args.GenericArguments.seq;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Lists;
import org.inspirenxe.enquiry.Enquiry;
import org.inspirenxe.enquiry.api.event.SearchEngineRegisterEvent;
import org.inspirenxe.enquiry.api.event.SearchEngineRegistrationEvent;
import org.inspirenxe.enquiry.api.event.SearchFailureEvent;
import org.inspirenxe.enquiry.api.event.SearchPreEvent;
import org.inspirenxe.enquiry.api.event.SearchSuccessEvent;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SearchEngine {

    private final PluginContainer plugin;
    private final String id;
    private final List<String> aliases = Lists.newArrayList();
    private final CommandSpec commandSpec;

    @SuppressWarnings("deprecation")
    public SearchEngine(PluginContainer plugin, String id, String... aliases) {
        this.plugin = plugin;
        this.id = id;
        Collections.addAll(this.aliases, aliases);
        final SearchEngine engine = this;
        commandSpec = CommandSpec.builder()
                .description(Texts.of("Searches ", getName(), " for the query provided."))
                .arguments(seq(playerOrSource(Texts.of("player"), Enquiry.instance.game)), remainingJoinedStrings(Texts.of("search")))
                .permission(plugin.getId() + ".command.search." + getId())
                .executor((src, args) -> {
                    Enquiry.instance.game.getScheduler().createTaskBuilder().async().execute(() -> {
                        final CommandSource target = args.<Player>getOne("player").get();
                        final String query = args.<String>getOne("search").get();
                        final SearchPreEvent preEvent = new SearchPreEvent(target, engine, query);

                        if (!Enquiry.instance.game.getEventManager().post(preEvent)) {
                            try {
                                final List<? extends SearchResult> results = preEvent.engine.getResults(query);
                                final SearchSuccessEvent event = new SearchSuccessEvent(target, preEvent.engine, query, results);
                                if (!Enquiry.instance.game.getEventManager().post(event)) {
                                    target.sendMessage(Texts.of(
                                            "(", Texts.of(event.engine.getName()).builder()
                                                    .onClick(TextActions.openUrl(new URL(event.engine.getUrl())))
                                                    .onHover(TextActions.showText(Texts.of(event.engine.getUrl())))
                                                    .build(),
                                            TextColors.RESET, ") Result(s) for: ", TextColors.YELLOW, query));
                                    int i = 1;
                                    for (SearchResult result : event.results) {
                                        final String resultMessage = Enquiry.instance.storage.getChildNode("engines." + event.engine.getId() +
                                                ".options.style.line-format").getString()
                                                .replace("${resultNumber}", Integer.toString(i++))
                                                .replace("${resultTitle}", result.getTitle())
                                                .trim();
                                        final Text message = Texts.legacy('&').from(resultMessage).builder()
                                                .onClick(TextActions.openUrl(new URL(result.getUrl())))
                                                .onHover(result.getDescription() != null && !result.getDescription().isEmpty() ?
                                                        TextActions.showText(Texts.of(result.getDescription().replaceAll("[^\\x20-\\x7e]", ""))) :
                                                        null)
                                                .build();
                                        System.out.println(result.getDescription());
                                        target.sendMessage(message);
                                    }
                                }
                            } catch (IOException | TextMessageException e) {
                                if (!Enquiry.instance.game.getEventManager().post(new SearchFailureEvent(target, preEvent.engine, query))) {
                                    if (src instanceof Player) {
                                        src.sendMessage(Texts.of("An error occurred while attempting to search ", preEvent.engine.getName(), " for ",
                                                TextColors.YELLOW, query));
                                    }
                                    Enquiry.instance.logger
                                            .warn("An error occurred while attempting to search " + Texts.toPlain(preEvent.engine.getName()) +
                                                    " for " + query, e);
                                }
                            }
                        }
                    }).submit(plugin);

                    return CommandResult.success();
                })
                .build();
    }

    /**
     * Registers the search engine
     * <p>
     * This method should only be fired during {@link SearchEngineRegistrationEvent}
     * @return The {@link SearchEngine}
     */
    public SearchEngine register() {
        if (!Enquiry.instance.game.getEventManager().post(new SearchEngineRegisterEvent(this))) {
            Enquiry.instance.putEngine(this);
        }
        return this;
    }

    /**
     * Gets the id of the engine
     * @return The ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the aliases
     * @return The aliases
     */
    public List<String> getAliases() {
        return this.aliases;
    }

    /**
     * Gets the {@link CommandSpec} of the engine.
     * @return The {@link CommandSpec}
     */
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    /**
     * Gets the {@link PluginContainer} of the plugin creating the engine.
     * @return The {@link PluginContainer}
     */
    public PluginContainer getPlugin() {
        return plugin;
    }

    /**
     * Gets the name of the engine.
     * @return The name
     */
    public abstract Text getName();

    /**
     * Gets the URL of the engine's website.
     * @return The engine's website URL
     */
    public abstract String getUrl();

    /**
     * Gets the URL of the engine's search API url.
     * @return The engine's search API url
     */
    public abstract String getSearchUrl();

    /**
     * Gets the {@link HttpRequest} of the engine.
     * @param query The query
     * @return The {@link HttpRequest}
     */
    public abstract HttpRequest getRequest(String query);

    /**
     /**
     * Gets a list of {@link SearchResult}.
     * @param query The query
     * @return The list of results
     * @throws IOException Thrown when an error occurred while getting results
     */
    public abstract CopyOnWriteArrayList<? extends SearchResult> getResults(String query) throws IOException;

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || !(obj == null || getClass() != obj.getClass()) && this.getName().equals(((SearchEngine) obj).getName());
    }
}
