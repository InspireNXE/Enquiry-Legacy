package org.inspirenxe.enquiry.api.event;

import org.inspirenxe.enquiry.api.engine.SearchEngine;
import org.spongepowered.api.event.AbstractEvent;
import org.spongepowered.api.event.Cancellable;

/**
 * Fired when Enquiry is registering the search engine.
 * <p>
 * Cancelling the event will stop the registration of the search engine.
 */
public class SearchEngineRegisterEvent extends AbstractEvent implements Cancellable {

    private final SearchEngine engine;
    private boolean cancelled;

    public SearchEngineRegisterEvent(SearchEngine engine) {
        this.engine = engine;
    }

    /**
     * Gets the {@link SearchEngine} being registered.
     * @return The engine
     */
    public SearchEngine getEngine() {
        return engine;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
