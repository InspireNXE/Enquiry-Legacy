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

import com.github.kevinsawicki.http.HttpRequest;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.IOException;
import java.util.List;

public interface SearchEngine {

    /**
     * Gets the name of the engine.
     * @return The name
     */
    Text getName();

    /**
     * Gets the {@link CommandSpec} of the engine.
     * @return The {@link CommandSpec}
     */
    CommandSpec getCommandSpec();

    /**
     * Gets the aliases
     * @return The aliases
     */
    List<String> getAliases();

    /**
     * Gets the URL of the engine's website.
     * @return The engine's website URL
     */
    String getUrl();

    /**
     * Gets the URL of the engine's search API url.
     * @return The engine's search API url
     */
    String getSearchUrl();

    /**
     * Gets the {@link HttpRequest} of the engine.
     * @param query The query
     * @return The request
     */
    HttpRequest getRequest(String query);

    /**
     * Gets a list of {@link SearchResult}.
     * @param query The query
     * @return The list of results
     */
    List<? extends SearchResult> getResults(String query) throws IOException;
}
