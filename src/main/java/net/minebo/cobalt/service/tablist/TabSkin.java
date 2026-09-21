package net.minebo.cobalt.service.tablist;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public final class TabSkin {

    public static final TabSkin DEFAULT = new TabSkin("eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=", "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw=");

    private final String value;
    private final String signature;

    public TabSkin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    public static TabSkin of(String value, String signature) {
        if (value == null || value.isEmpty()) {
            return DEFAULT;
        }
        return new TabSkin(value, signature);
    }

    public static TabSkin of(Player player) {
        if (player == null) {
            return DEFAULT;
        }
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) {
            return DEFAULT;
        }
        UserProfile profile = user.getProfile();
        if (profile == null) {
            return DEFAULT;
        }
        List<TextureProperty> properties = profile.getTextureProperties();
        if (properties == null || properties.isEmpty()) {
            return DEFAULT;
        }
        TextureProperty textures = properties.get(0);
        return new TabSkin(textures.getValue(), textures.getSignature());
    }

    public String value() {
        return value;
    }

    public String signature() {
        return signature;
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    public void apply(UserProfile profile) {
        if (isEmpty()) {
            return;
        }
        profile.getTextureProperties().clear();
        profile.getTextureProperties().add(new TextureProperty("textures", value, signature));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TabSkin other)) {
            return false;
        }
        return Objects.equals(value, other.value) && Objects.equals(signature, other.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, signature);
    }
}