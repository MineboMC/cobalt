package net.minebo.cobalt.service.hologram.api;

import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.HologramService;
import org.bukkit.Location;
import java.util.Collection;
import java.util.List;

public final class HologramApi {
    private final HologramService service;
    public HologramApi(HologramService service) { this.service = service; }

    public Hologram create(String id, Location location, List<String> lines) {
        return service.create(id, location, lines);
    }
    public void delete(String id) {
        Hologram hologram = service.byId(id);
        if (hologram != null) service.delete(hologram);
    }
    public void setLines(Hologram hologram, List<String> lines) { service.setLines(hologram, lines); }
    public void teleport(Hologram hologram, Location location) { service.teleport(hologram, location); }
    public Hologram byId(String id) { return service.byId(id); }
    public Collection<Hologram> all() { return service.registry().all(); }
}