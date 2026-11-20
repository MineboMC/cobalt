package net.minebo.cobalt.service.hologram.clickable;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.CobaltAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A hologram only one player sees, made of text displays. Lines with an action grow when they're looked at
 * and run it when clicked, see ClickableHolograms for how hovers and clicks are picked up.
 *
 * <p>Lines hang down from the top, so adding or removing lines at the bottom doesn't move the rest.
 */
public final class ClickableHologram {

    /** A line's text and what clicking it does, null if nothing. Text can be MiniMessage (or legacy), or a built component. */
    public record Line(Component text, Consumer<Player> action) {

        public static Line text(String text) {
            return text(Coloring.toComponent(text == null ? "" : text));
        }

        public static Line text(Component text) {
            return new Line(text, null);
        }

        public static Line clickable(String text, Consumer<Player> action) {
            return clickable(Coloring.toComponent(text == null ? "" : text), action);
        }

        public static Line clickable(Component text, Consumer<Player> action) {
            return new Line(text, action);
        }

        public boolean isClickable() {
            return action != null;
        }
    }

    private static final int MAX_LINES = 31; // the 32nd id is the click box
    private static final double PIXEL = 0.025; // a text display's pixel at scale 1
    static final double LINE_HEIGHT = 10 * PIXEL;
    private static final float HOVER_SCALE = 1.25F;
    private static final int HOVER_TICKS = 3;
    private static final int NO_WRAP = 1000;
    private static final int OBJECT_WIDTH = 9;

    private final Player viewer;
    private final int baseId;
    private final Location top;
    private final double spacing;
    private List<Line> lines = List.of();
    private int spawned;
    private int hovered = -1;
    private float boxWidth;
    private float boxHeight;

    public ClickableHologram(Player viewer, Location top, double spacing) {
        this.viewer = viewer;
        this.baseId = CobaltAPI.getHologramService().registry().nextBaseEntityId();
        this.top = top.clone();
        this.spacing = spacing;
        CobaltAPI.getHologramService().clickables().track(this);
    }

    public Player getViewer() {
        return viewer;
    }

    /** Sends only what changed: new text, new lines, and lines that are gone. */
    public void setLines(List<Line> next) {
        List<Line> previous = lines;
        lines = List.copyOf(next.subList(0, Math.min(next.size(), MAX_LINES)));
        if (hovered >= lines.size() || hovered >= 0 && !lines.get(hovered).isClickable()) hovered = -1;

        for (int index = 0; index < lines.size(); index++) {
            if (index >= spawned) {
                spawn(index);
            } else if (!previous.get(index).text().equals(lines.get(index).text())) {
                send(new WrapperPlayServerEntityMetadata(baseId + index, List.of(text(lines.get(index).text()))));
            }
        }
        if (spawned > lines.size()) {
            int[] gone = new int[spawned - lines.size()];
            for (int index = 0; index < gone.length; index++) gone[index] = baseId + lines.size() + index;
            send(new WrapperPlayServerDestroyEntities(gone));
        }
        spawned = lines.size();
        updateBox();
    }

    public void remove() {
        int[] ids = new int[spawned + 1];
        for (int index = 0; index < spawned; index++) ids[index] = baseId + index;
        ids[spawned] = boxId();
        send(new WrapperPlayServerDestroyEntities(ids));
        spawned = 0;
        lines = List.of();
        CobaltAPI.getHologramService().clickables().untrack(this);
    }

    /** The clickable line the viewer's looking at, -1 if none. Approximates each line as a flat strip facing them. */
    int lineUnderCrosshair(double reach) {
        Location eye = viewer.getEyeLocation();
        if (eye.getWorld() != top.getWorld() || eye.distanceSquared(top) > (reach + lines.size() * spacing) * (reach + lines.size() * spacing)) return -1;

        Vector origin = eye.toVector();
        Vector direction = eye.getDirection();
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int index = 0; index < lines.size(); index++) {
            Line line = lines.get(index);
            if (!line.isClickable()) continue;

            Vector center = position(index).toVector().add(new Vector(0, LINE_HEIGHT / 2, 0));
            Vector normal = origin.clone().subtract(center).setY(0);
            if (normal.lengthSquared() < 1.0E-4) continue;
            normal.normalize();

            double facing = direction.dot(normal);
            if (facing >= 0) continue; // looking away
            double distance = center.clone().subtract(origin).dot(normal) / facing;
            if (distance < 0 || distance > reach) continue;

            Vector offset = origin.clone().add(direction.clone().multiply(distance)).subtract(center);
            Vector right = new Vector(-normal.getZ(), 0, normal.getX());
            if (Math.abs(offset.dot(right)) <= width(line.text()) / 2 && Math.abs(offset.getY()) <= spacing / 2 && distance < bestDistance) {
                best = index;
                bestDistance = distance;
            }
        }
        return best;
    }

    void hover(int index) {
        if (index == hovered) return;
        if (hovered >= 0 && hovered < spawned) send(new WrapperPlayServerEntityMetadata(baseId + hovered, scale(1F)));
        hovered = index;
        if (hovered >= 0) send(new WrapperPlayServerEntityMetadata(baseId + hovered, scale(HOVER_SCALE)));
    }

    /** Runs the hovered line's action, true if there was one. */
    boolean click() {
        if (hovered < 0 || hovered >= lines.size() || !lines.get(hovered).isClickable()) return false;
        lines.get(hovered).action().accept(viewer);
        return true;
    }

    boolean owns(int entityId) {
        return entityId == boxId() || entityId >= baseId && entityId < baseId + spawned;
    }

    private void spawn(int index) {
        Location at = position(index);
        spawnEntity(baseId + index, EntityTypes.TEXT_DISPLAY, at, index);

        List<EntityData<?>> data = new ArrayList<>();
        data.add(new EntityData<>(8, EntityDataTypes.INT, 0)); // interpolation delay
        data.add(new EntityData<>(9, EntityDataTypes.INT, HOVER_TICKS)); // scale interpolation
        data.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) 3)); // billboard: always faces you
        data.add(new EntityData<>(16, EntityDataTypes.INT, 15 << 4 | 15 << 20)); // full bright, even at night
        data.add(new EntityData<>(24, EntityDataTypes.INT, NO_WRAP));
        data.add(new EntityData<>(25, EntityDataTypes.INT, 0)); // no background
        data.add(new EntityData<>(27, EntityDataTypes.BYTE, (byte) 0x01)); // shadowed text
        data.addAll(scale(index == hovered ? HOVER_SCALE : 1F));
        data.add(text(lines.get(index).text()));
        send(new WrapperPlayServerEntityMetadata(baseId + index, data));
    }

    /**
     * One invisible box over the whole hologram so a right click with an empty hand still reaches the server,
     * the client sends nothing for that unless it's aiming at an entity.
     */
    private void updateBox() {
        float width = (float) lines.stream().filter(Line::isClickable).mapToDouble(line -> width(line.text()) * HOVER_SCALE).max().orElse(0);
        float height = (float) (lines.size() * spacing);
        if (width == boxWidth && height == boxHeight) return;

        boolean exists = boxWidth > 0;
        boxWidth = width;
        boxHeight = height;
        if (width <= 0) {
            if (exists) send(new WrapperPlayServerDestroyEntities(boxId()));
            return;
        }
        if (exists) send(new WrapperPlayServerDestroyEntities(boxId())); // it grows from its feet, so it moves when the height does

        spawnEntity(boxId(), EntityTypes.INTERACTION, position(lines.size() - 1).subtract(0, spacing / 2, 0), lines.size());
        send(new WrapperPlayServerEntityMetadata(boxId(), List.of(
                new EntityData<>(8, EntityDataTypes.FLOAT, width),
                new EntityData<>(9, EntityDataTypes.FLOAT, height),
                new EntityData<>(10, EntityDataTypes.BOOLEAN, true)))); // swings their arm like a real click
    }

    private void spawnEntity(int id, EntityType type, Location at, int salt) {
        UUID uuid = UUID.nameUUIDFromBytes(("clickable:" + baseId + ":" + salt + ":" + type.getName()).getBytes());
        send(new WrapperPlayServerSpawnEntity(id, Optional.of(uuid), type, new Vector3d(at.getX(), at.getY(), at.getZ()),
                0F, 0F, 0F, 0, Optional.empty()));
    }

    private Location position(int index) {
        return top.clone().subtract(0, (index + 1) * spacing, 0);
    }

    private int boxId() {
        return baseId + MAX_LINES;
    }

    /** Grown from the middle of the line rather than its bottom, so it doesn't climb into the line above. */
    private static List<EntityData<?>> scale(float scale) {
        float shift = (float) (-(scale - 1) * LINE_HEIGHT / 2);
        return List.of(
                new EntityData<>(8, EntityDataTypes.INT, 0),
                new EntityData<>(11, EntityDataTypes.VECTOR3F, new Vector3f(0, shift, 0)),
                new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)));
    }

    private static EntityData<?> text(Component text) {
        return new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, text);
    }

    /** About how wide the text is in blocks, from the default font's character widths. Heads and sprites are a character each. */
    static double width(Component text) {
        String plain = PlainTextComponentSerializer.plainText().serialize(text);
        boolean bold = bold(text);
        int pixels = OBJECT_WIDTH * objects(text);
        for (char character : plain.toCharArray()) {
            pixels += switch (character) {
                case 'i', '!', '.', ',', ':', ';', '|', '\'' -> 2;
                case 'l', '`' -> 3;
                case 'I', 't', '[', ']', ' ' -> 4;
                case 'f', 'k', '<', '>', '"', '(', ')', '*', '{', '}' -> 5;
                case '@', '~' -> 7;
                default -> 6;
            } + (bold ? 1 : 0);
        }
        return pixels * PIXEL;
    }

    private static boolean bold(Component component) {
        return component.hasDecoration(TextDecoration.BOLD) || component.children().stream().anyMatch(ClickableHologram::bold);
    }

    private static int objects(Component component) {
        int count = component instanceof ObjectComponent ? 1 : 0;
        for (Component child : component.children()) count += objects(child);
        return count;
    }

    private void send(PacketWrapper<?> packet) {
        if (!viewer.isOnline()) return;
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(viewer, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Hologram] Failed packet to " + viewer.getName() + ": " + exception.getMessage());
        }
    }
}
