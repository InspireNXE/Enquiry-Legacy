package org.inspirenxe.enquiry.engine;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import org.inspirenxe.enquiry.Commands;
import org.inspirenxe.enquiry.Enquiry;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.CatalogTypeAlreadyRegisteredException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EngineTypeRegistryModule implements AdditionalCatalogRegistryModule<EngineType> {

    final Map<String, EngineType> maps = new HashMap<>();

    private EngineTypeRegistryModule() {
    }

    public static EngineTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerAdditionalCatalog(EngineType extraCatalog) {
        checkNotNull(extraCatalog);
        for (String alias : extraCatalog.getAliases()) {
            if (this.maps.containsKey(alias)) {
                throw new CatalogTypeAlreadyRegisteredException(alias);
            }

            this.maps.put(alias, extraCatalog);

            Enquiry.instance.getLogger().info("Registered instance [{}].", alias);
        }
    }

    @Override
    public Optional<EngineType> getById(String id) {
        checkNotNull(id);
        return Optional.ofNullable(this.maps.get(id));
    }

    @Override
    public Collection<EngineType> getAll() {
        return Collections.unmodifiableCollection(this.maps.values());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("maps", this.getAll())
                .toString();
    }

    @Override
    public void registerDefaults() {
        this.registerAdditionalCatalog(EngineType.builder()
                .name("bing")
                .displayName(Text.of(TextColors.GRAY, "Bing"))
                .url("https://bing.com")
                .apiUrl("https://api.datamarket.azure.com/Bing/Search/Web")
                .plugin(Enquiry.instance.getContainer())
                .aliases("bing", "b")
                .commandSpec(Commands.rootCommand)
                .build("bing"));
    }

    private static final class Holder {

        static final EngineTypeRegistryModule INSTANCE = new EngineTypeRegistryModule();
    }
}
