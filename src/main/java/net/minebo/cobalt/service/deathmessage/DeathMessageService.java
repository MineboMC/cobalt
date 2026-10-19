package net.minebo.cobalt.service.deathmessage;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.deathmessage.event.PlayerDeathContributionEvent;
import net.minebo.cobalt.service.deathmessage.listener.DeathTrackingListener;
import net.minebo.cobalt.service.deathmessage.provider.DefaultDeathMessages;
import net.minebo.cobalt.service.deathmessage.provider.DefaultPlayerNameResolver;
import net.minebo.cobalt.service.deathmessage.registry.DeathMessageProviderRegistry;
import net.minebo.cobalt.service.deathmessage.registry.NameResolverRegistry;
import net.minebo.cobalt.service.deathmessage.tracker.DamageResolver;
import net.minebo.cobalt.service.deathmessage.tracker.DeathTracker;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public final class DeathMessageService extends CService {

    private DeathTracker tracker;
    private DeathMessageProviderRegistry providers;
    private NameResolverRegistry resolvers;
    private DeathMessageSettings settings;
    private DeathTrackingListener listener;

    @Override
    public String getName() {
        return "Death Messages";
    }

    @Override
    public void onEnable() {
        tracker = new DeathTracker();
        providers = new DeathMessageProviderRegistry();
        resolvers = new NameResolverRegistry(new DefaultPlayerNameResolver());
        settings = new DeathMessageSettings();
        listener = new DeathTrackingListener(this);

        DefaultDeathMessages.registerAll(providers);
        Bukkit.getPluginManager().registerEvents(listener, Cobalt.getInstance());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(listener);
        tracker.clearAll();
    }

    public DeathMessageSettings settings() {
        return settings;
    }

    /** Register (or override) the message shown for a category. Higher weight wins. */
    public void registerDeathMessage(DeathCauseCategory category, DeathMessageProvider provider, int weight) {
        providers.register(category, provider, weight);
    }

    public void unregisterDeathMessage(DeathMessageProvider provider) {
        providers.unregister(provider);
    }

    /**
     * Register a resolver that can replace raw player names with metadata
     * (nicknames, ranks, etc). Higher weight wins.
     */
    public void registerNameResolver(PlayerNameResolver resolver, int weight) {
        resolvers.register(resolver, weight);
    }

    public void unregisterNameResolver(PlayerNameResolver resolver) {
        resolvers.unregister(resolver);
    }

    /** Live damage-contribution snapshot for a player who hasn't died (yet). */
    public DeathContext peek(UUID victim) {
        return DeathContext.build(victim, tracker.log(victim), null, DeathCauseCategory.GENERIC,
                settings.assistWindowMillis(), settings.minimumAssistDamage());
    }

    public void handleQuit(Player player) {
        tracker.clear(player.getUniqueId());
    }

    public void recordDamage(UUID victim, DamageRecord record) {
        tracker.record(victim, record, settings.assistWindowMillis());
    }

    public void handleDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        UUID victimId = victim.getUniqueId();

        EntityDamageEvent killingEvent = victim.getLastDamageCause();
        DamageRecord killingBlow = killingEvent != null ? DamageResolver.resolve(killingEvent) : null;
        DeathCauseCategory category = killingBlow != null ? killingBlow.category() : DeathCauseCategory.GENERIC;

        DeathContext context = DeathContext.build(victimId, tracker.log(victimId), killingBlow, category,
                settings.assistWindowMillis(), settings.minimumAssistDamage());

        Bukkit.getPluginManager().callEvent(new PlayerDeathContributionEvent(context));

        if (!settings.broadcastGlobally()) {
            event.deathMessage(null);
        } else {
            DeathMessageProvider provider = providers.select(category);
            Component message = provider != null
                    ? provider.build(context, resolvers.select(), settings)
                    : Coloring.toComponent("<red>" + victim.getName() + " <yellow>died.");
            event.deathMessage(message);
        }

        tracker.clear(victimId);
    }
}