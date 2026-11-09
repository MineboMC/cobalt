package net.minebo.cobalt.service.projectile;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public record ProjectileHit(Kind kind, Location location, Entity entity, Block block, BlockFace face) {

    public enum Kind { ENTITY, BLOCK }

    public static ProjectileHit entity(Entity entity, Location location) {
        return new ProjectileHit(Kind.ENTITY, location, entity, null, null);
    }

    public static ProjectileHit block(Block block, BlockFace face, Location location) {
        return new ProjectileHit(Kind.BLOCK, location, null, block, face);
    }

    public boolean isEntity() { return kind == Kind.ENTITY; }
    public boolean isBlock() { return kind == Kind.BLOCK; }
}
