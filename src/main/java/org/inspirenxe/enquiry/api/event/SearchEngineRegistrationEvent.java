package org.inspirenxe.enquiry.api.event;

import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.spongepowered.api.event.AbstractEvent;

import java.util.Collections;
import java.util.Set;

/**
 * Fired when Enquiry is registering search engines.
 */
public class SearchEngineRegistrationEvent extends AbstractEvent {

    private final Set<SearchEngine> engines;

    public SearchEngineRegistrationEvent(Set<SearchEngine> engines) {
        this.engines = engines;
    }

    /**
     * Gets the {@link SearchEngine}'s to be registered.
     * @return The engines to be registered
     */
    public Set<SearchEngine> getEngines() {
        return Collections.unmodifiableSet(engines);
    }
}
