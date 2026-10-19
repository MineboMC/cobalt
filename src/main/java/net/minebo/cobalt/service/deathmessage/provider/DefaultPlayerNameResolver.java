package net.minebo.cobalt.service.deathmessage.provider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.minebo.cobalt.service.deathmessage.PlayerNameResolver;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class DefaultPlayerNameResolver implements PlayerNameResolver {

    @Override
    public Component resolve(UUID playerId) {
        Player online = Bukkit.getPlayer(playerId);
        String name = online != null ? online.getName() : Bukkit.getOfflinePlayer(playerId).getName();

        PlayerHeadObjectContents.Builder head = ObjectContents.playerHead().id(playerId);
        if (name != null) head.name(name);

        // Attach skin textures when online so the head renders without a client-side lookup
        if (online != null) {
            online.getPlayerProfile().getProperties().stream()
                    .filter(p -> p.getName().equals("textures"))
                    .forEach(p -> head.profileProperty(
                            PlayerHeadObjectContents.property(p.getName(), p.getValue(), p.getSignature())));
        }

        Component display = online != null ? online.name()
                : name != null ? Component.text(name)
                  : Coloring.toComponent("Unknown");

        return Component.object(head.build()).appendSpace().append(display);
    }
}