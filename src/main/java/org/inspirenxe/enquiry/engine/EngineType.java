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
package org.inspirenxe.enquiry.engine;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ResettableBuilder;

import java.util.Arrays;
import java.util.List;

public class EngineType implements CatalogType {

    private final String id;
    private final String name;
    private final String url;
    private final String apiUrl;
    private final Text displayName;
    private final PluginContainer plugin;
    private final CommandSpec commandSpec;
    private final List<String> aliases;

    public EngineType(String id, Builder builder) {
        this.id = id;
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.url = builder.url;
        this.apiUrl = builder.apiUrl;
        this.plugin = builder.plugin;
        this.commandSpec = builder.commandSpec;
        this.aliases = builder.aliases;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public String getUrl() {
        return this.url;
    }

    public String getApiUrl() {
        return this.apiUrl;
    }

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    public CommandSpec getCommandSpec() {
        return this.commandSpec;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EngineType engineType = (EngineType) o;
        return java.util.Objects.equals(this.id, engineType.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", this.id)
                .add("plugin", this.plugin)
                .add("aliases", this.aliases.toString())
                .toString();
    }

    public static final class Builder implements ResettableBuilder<EngineType, Builder> {
        String name;
        Text displayName;
        String url;
        String apiUrl;
        PluginContainer plugin;
        CommandSpec commandSpec;
        List<String> aliases = Lists.newArrayList();

        public Builder() {
            this.reset();
        }

        @Override
        public Builder from(EngineType value) {
            this.name = value.name;
            this.url = value.url;
            this.apiUrl = value.apiUrl;
            this.displayName = value.displayName;
            this.plugin = value.plugin;
            this.commandSpec = value.commandSpec;
            this.aliases = value.aliases;

            return this;
        }

        @Override
        public Builder reset() {
            this.plugin = null;
            this.commandSpec = null;
            this.aliases = null;

            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder displayName(Text displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder plugin(PluginContainer plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder commandSpec(CommandSpec commandSpec) {
            this.commandSpec = commandSpec;
            return this;
        }

        public Builder aliases(String... aliases) {
            this.aliases = Arrays.asList(aliases);
            return this;
        }

        public EngineType build(String id) {
            checkNotNull(id);
            checkNotNull(this.name);
            checkNotNull(this.displayName);
            checkNotNull(this.url);
            checkNotNull(this.apiUrl);
            checkNotNull(this.plugin);
            checkNotNull(this.commandSpec);
            checkNotNull(this.aliases);

            return new EngineType(id, this);
        }
    }
}
