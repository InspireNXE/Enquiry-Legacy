/*
 * This file is part of Enquiry, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <http://github.com/InspireNXE>
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

import com.google.inject.Inject;
import org.inspirenxe.enquiry.engine.EngineType;
import org.inspirenxe.enquiry.engine.EngineResult;
import org.inspirenxe.enquiry.engine.EngineTypeRegistryModule;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = Constants.Meta.ID,
        name = Constants.Meta.NAME,
        version = Constants.Meta.VERSION,
        authors = Constants.Meta.AUTHORS,
        url = Constants.Meta.URL,
        description = Constants.Meta.DESCRIPTION)
@NonnullByDefault
public final class Enquiry {

    public static Enquiry instance;

    @Inject private PluginContainer container;
    @Inject private Logger logger;
    @Inject @ConfigDir(sharedRoot = false) private Path configPath;

    @Listener
    public void onConstruct(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) throws IOException {
        Sponge.getRegistry().registerModule(EngineTypeRegistryModule.getInstance());
        Sponge.getRegistry().registerBuilderSupplier(EngineType.Builder.class, EngineType.Builder::new);
        Sponge.getRegistry().registerBuilderSupplier(EngineResult.Builder.class, EngineResult.Builder::new);

        EngineTypeRegistryModule.getInstance().registerDefaults();

        Sponge.getCommandManager().register(this.container, Commands.rootCommand, Constants.Meta.ID, Constants.Meta.ABBREVIATION);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public PluginContainer getContainer() {
        return this.container;
    }
}
