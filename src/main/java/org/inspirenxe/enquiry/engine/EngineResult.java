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

import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ResettableBuilder;

public class EngineResult {

    private final Text title;
    private final Text description;
    private final String url;

    public EngineResult(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.url = builder.url;
    }

    public static final class Builder implements ResettableBuilder<EngineResult, Builder> {
        Text title;
        Text description;
        String url;

        public Builder() {
            this.reset();
        }

        @Override
        public Builder from(EngineResult value) {
            this.title = value.title;
            this.description = value.description;
            this.url = value.url;
            return this;
        }

        @Override
        public Builder reset() {
            this.title = null;
            this.description = Text.EMPTY;
            this.url = null;
            return this;
        }

        public Builder title(Text title) {
            this.title = title;
            return this;
        }

        public Builder description(Text description) {
            this.description = description;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public EngineResult build() {
            checkNotNull(this.title);
            checkNotNull(this.description);
            checkNotNull(this.url);

            return new EngineResult(this);
        }
    }
}
