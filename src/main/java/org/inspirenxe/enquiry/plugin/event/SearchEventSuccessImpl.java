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
package org.inspirenxe.enquiry.plugin.event;

import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.engine.SearchResult;
import org.inspirenxe.enquiry.api.event.SearchEvent;
import org.inspirenxe.enquiry.plugin.Enquiry;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collections;
import java.util.List;

@NonnullByDefault
public class SearchEventSuccessImpl extends AbstractEvent implements SearchEvent.Success {

    private final Cause cause;
    private final SearchEngine engine;
    private final String query;
    private final List<? extends SearchResult> results;

    public SearchEventSuccessImpl(Cause cause, SearchEngine engine, String query, List<? extends SearchResult> results) {
        this.cause = cause;
        this.engine = engine;
        this.query = query;
        this.results = results;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    @Override
    public SearchEngine getEngine() {
        return engine;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Enquiry getEnquiry() {
        return Enquiry.instance;
    }

    @Override
    public List<? extends SearchResult> getResults() {
        return Collections.unmodifiableList(results);
    }
}
