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

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class EngineManager {

    private static final Map<String, SearchEngine> engines = Maps.newHashMap();

    private EngineManager() {
    }

    public static Map<String, SearchEngine> getEngines() {
        return Collections.unmodifiableMap(engines);
    }

    public static void put(SearchEngine engine) {
        // TODO: Post registration event
        for (String alias : engine.getAliases()) {
            engines.put(alias, engine);
        }
    }

    public static void putAll(Collection<? extends SearchEngine> engines) {
        engines.forEach(EngineManager::put);
    }

    public static void remove(SearchEngine engine) {
        // TODO: Post unregistration event
        engine.getAliases().forEach(engines::remove);
    }

    public static void removeAll(Collection<? extends SearchEngine> engines) {
        engines.forEach(EngineManager::remove);
    }
}
