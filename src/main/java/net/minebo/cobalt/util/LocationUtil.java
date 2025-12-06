package net.minebo.cobalt.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtil {

    public static List<Player> getNearbyPlayers(LivingEntity player, double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public static List<Player> getNearbyPlayers(Location location, double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity entity : location.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public static Player getNearestPlayer(LivingEntity source, double radius) {
        Player nearest = null;
        double nearestDistSq = radius * radius;
        Location srcLoc = source.getLocation();

        List<Player> players = getNearbyPlayers(source, radius);
        for (Player player : players) {
            double distSq = player.getLocation().distanceSquared(srcLoc);
            if (distSq <= nearestDistSq) {
                if (nearest == null || distSq < nearest.getLocation().distanceSquared(srcLoc)) {
                    nearest = player;
                    nearestDistSq = distSq;
                }
            }
        }
        return nearest;
    }

    public static Player getNearestPlayer(Location source, double radius) {
        Player nearest = null;
        double nearestDistSq = radius * radius;
        Location srcLoc = source;

        List<Player> players = getNearbyPlayers(source, radius);
        for (Player player : players) {
            double distSq = player.getLocation().distanceSquared(srcLoc);
            if (distSq <= nearestDistSq) {
                if (nearest == null || distSq < nearest.getLocation().distanceSquared(srcLoc)) {
                    nearest = player;
                    nearestDistSq = distSq;
                }
            }
        }
        return nearest;
    }

    public static Location getForwardOffset(Location from, Vector direction, double distance) {
        return from.clone().add(direction.clone().normalize().multiply(distance));
    }
}
