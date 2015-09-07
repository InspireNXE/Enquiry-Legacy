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
package org.inspirenxe.enquiry.plugin.engine;


import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.engine.SearchResult;
import org.inspirenxe.enquiry.plugin.Enquiry;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;

public class DuckDuckGoEngine extends SearchEngine {

    @SerializedName("RelatedTopics")
    private CopyOnWriteArrayList<DuckDuckGoResult> results;

    public DuckDuckGoEngine(String id, String... aliases) {
        super(Enquiry.instance.container, id, aliases);
    }

    @Override
    public Text getName() {
        return Texts.of(TextColors.YELLOW, "DuckDuck", TextColors.GRAY, "Go");
    }

    @Override
    public String getUrl() {
        return "https://duckduckgo.com";
    }

    @Override
    public String getSearchUrl() {
        return "https://api.duckduckgo.com";
    }

    @Override
    public HttpRequest getRequest(String query) {
        return HttpRequest.get(getSearchUrl(), true,
                "q", query,
                "format", "json",
                "no_html", "1",
                "t", "enquiry")
                .acceptJson()
                .acceptCharset(StandardCharsets.UTF_8.name())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 " +
                        "Edge/12.10532");
    }

    @Override
    public CopyOnWriteArrayList<? extends SearchResult> getResults(String query) throws IOException {
        final HttpRequest request = getRequest(query);
        if (request.code() != 200) {
            throw new IOException("An error occurred while attempting to get results from " + Texts.toPlain(this.getName()) + ", Error: " + request
                    .code());
        } else if (request.isBodyEmpty()) {
            throw new IOException("An error occurred while attempting to get results from " + Texts.toPlain(this.getName()) + ", Error: Body is "
                    + "empty.");
        }
        return new Gson().fromJson(request.body(), DuckDuckGoEngine.class).results;
    }

    public class DuckDuckGoResult implements SearchResult {

        @SerializedName("Text")
        private String title;

        @SerializedName("")
        private String description;

        @SerializedName("FirstURL")
        private String url;

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getUrl() {
            return url;
        }
    }
}
