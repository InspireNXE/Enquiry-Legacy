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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SearchEngine {

    private final Enquiry enquiry;
    private final String id;
    private final List<String> aliases = Lists.newArrayList();
    private final CommandSpec commandSpec;

    public SearchEngine(Enquiry enquiry, String id, String... aliases) {
        this.enquiry = enquiry;
        this.id = id;
        Collections.addAll(this.aliases, aliases);
        commandSpec = CommandSpec.builder()
                .description(Texts.of("Searches ", getName(), " for the query provided."))
                .arguments(seq(playerOrSource(Texts.of("player"), enquiry.game)), remainingJoinedStrings(Texts.of("search")))
                .permission("enquiry.command.search." + getId())
                .executor(new Enquiry.SearchCommandExecutor(enquiry, this))
                .build();
    }

    /**
     * Registers the search engine
     * <p>
     * This method should only be fired during {@link SearchEngineRegistrationEvent}
     * @return The {@link SearchEngine}
     */
    public SearchEngine register() {
        if (!this.enquiry.game.getEventManager().post(new SearchEngineRegisterEvent(this))) {
            this.enquiry.putEngine(this);
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
