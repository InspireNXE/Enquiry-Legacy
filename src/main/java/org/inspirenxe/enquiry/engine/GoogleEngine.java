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
package org.inspirenxe.enquiry.engine;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.inspirenxe.enquiry.Enquiry;
import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.inspirenxe.enquiry.api.engine.SearchResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GoogleEngine implements SearchEngine {

    @SerializedName("items")
    private List<GoogleResult> results;

    @Override
    public Text getName() {
        return Texts.of(TextColors.BLUE, "G",
                TextColors.RED, "o",
                TextColors.YELLOW, "o",
                TextColors.BLUE, "g",
                TextColors.GREEN, "l",
                TextColors.RED, "e");
    }

    @Override
    public String getUrl() {
        return "https://www.google.com";
    }

    @Override
    public String getSearchUrl() {
        return "https://www.googleapis.com/customsearch/v1";
    }

    @Override
    public HttpRequest getRequest(String query) {
        return HttpRequest.get(getSearchUrl(), true,
                "key", Enquiry.getInstance().googleApiKey,
                "cx", Enquiry.getInstance().googleSearchId,
                "fields", "items(title,link,snippet)",
                "q", query)
                .acceptJson()
                .acceptCharset(StandardCharsets.UTF_8.name());
    }

    @Override
    public List<? extends SearchResult> getResults(String query) throws IOException {
        if (Enquiry.getInstance().googleApiKey.isEmpty()) {
            throw new IOException("google.api-key in " + Enquiry.getInstance().defaultConfig.getCanonicalPath() + " must be set in order to"
                    + "search with Google!");
        }
        if (Enquiry.getInstance().googleSearchId.isEmpty()) {
            throw new IOException("google.search-id in " + Enquiry.getInstance().defaultConfig.getCanonicalPath() + " must be set in order to "
                    + "search with Google!");
        }
        return new Gson().fromJson(getRequest(query).body(), GoogleEngine.class).results;
    }

    public class GoogleResult implements SearchResult {
        private String title;

        @SerializedName("snippet")
        private String description;

        @SerializedName("link")
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
