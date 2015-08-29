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
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.event.SearchEngineRegistrationEvent;
import org.inspirenxe.enquiry.engine.BingEngine;
import org.inspirenxe.enquiry.engine.DuckDuckGoEngine;
import org.inspirenxe.enquiry.engine.GoogleEngine;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ConstructionEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Plugin(id = "enquiry", name = "Enquiry", version = "1.2")
@NonnullByDefault
public class Enquiry {

    public static Enquiry instance;

    @Inject public Game game;
    @Inject public PluginContainer container;
    @Inject public Logger logger;

    public Storage storage;

    @DefaultConfig(sharedRoot = true)
    @Inject private File configuration;
    @DefaultConfig(sharedRoot = true)
    @Inject private ConfigurationLoader<CommentedConfigurationNode> loader;

    private final Set<SearchEngine> engines = Sets.newHashSet();

    @Subscribe
    public void onConstruct(ConstructionEvent event) {
        instance = this;
    }

    @Subscribe
    public void onServerAboutToStart(ServerAboutToStartEvent event) throws IOException {
        storage = new Storage(configuration, loader).load();

        final List<String> bingAliases = storage.getChildNode("engines.bing.options.aliases").getList(Types::asString);
        new BingEngine("bing", bingAliases.toArray(new String[bingAliases.size()])).register();

        final List<String> duckduckgoAliases = storage.getChildNode("engines.duckduckgo.options.aliases").getList(Types::asString);
        new DuckDuckGoEngine("duckduckgo", duckduckgoAliases.toArray(new String[duckduckgoAliases.size()])).register();

        final List<String> googleAliases = storage.getChildNode("engines.google.options.aliases").getList(Types::asString);
        new GoogleEngine("google", googleAliases.toArray(new String[googleAliases.size()])).register();

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

    public SearchEngine putEngine(SearchEngine engine) {
        this.engines.add(engine);
        return engine;
    }
}
