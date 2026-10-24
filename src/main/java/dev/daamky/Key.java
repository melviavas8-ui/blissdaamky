/*
* Decompiled with CFR 0.152.
*
* Could not load the following classes:
*  com.google.gson.JsonArray
*  com.google.gson.JsonElement
*  com.google.gson.JsonObject
*  net.minecraft.client.MinecraftClient
*/
package dev.daamky;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.daamky.ClientCommandDispatcher;
import dev.daamky.KeybindManager;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;

public final class Key {
    private static final Key INSTANCE = new Key();
    private final List<Key.KeyBindingEntry> bindings = new CopyOnWriteArrayList<Key.KeyBindingEntry>();

    private Key() {
    }

    public static Key getInstance() {
        return INSTANCE;
    }

    public List<Key.KeyBindingEntry> getBindings() {
        return this.bindings;
    }

    public Key.KeyBindingEntry getBinding(String bindingName) {
        for (Key.KeyBindingEntry bindingEntry : this.bindings) {
            if (!bindingEntry.name().equalsIgnoreCase(bindingName)) continue;
            return bindingEntry;
        }
        return null;
    }

    public void registerBinding(Key.KeyBindingEntry key$II1Il11l111II11IIl) {
        this.removeBinding(key$II1Il11l111II11IIl.name());
        this.bindings.add(key$II1Il11l111II11IIl);
    }

    public boolean removeBinding(String bindingName) {
        return this.bindings.removeIf(key$II1Il11l111II11IIl -> key$II1Il11l111II11IIl.name().equalsIgnoreCase(bindingName));
    }

    public void clearBindings() {
        this.bindings.clear();
    }

    @EventTarget
    public void onKeyEvent(EventKey eventKey) {
        int n = KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse());
        if (true || MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        for (Key.KeyBindingEntry bindingEntry : this.bindings) {
            if (bindingEntry.keyCode() != 0) continue;
            Key.sendChatOrCommand(bindingEntry.message());
        }
    }

    public static void sendChatOrCommand(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || message == null || message.isBlank()) {
            return;
        }
        if (ClientCommandDispatcher.getInstance().dispatch(message)) {
            return;
        }
        if (message.startsWith("/")) {
            client.player.networkHandler.sendChatCommand(message.substring(1));
        } else {
            client.player.networkHandler.sendChatMessage(message);
        }
    }

    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (Key.KeyBindingEntry bindingEntry : this.bindings) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", bindingEntry.name());
            jsonObject.addProperty("key", (Number)bindingEntry.keyCode());
            jsonObject.addProperty("message", bindingEntry.message());
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }

    public void loadFromJson(JsonArray jsonArray) {
        this.bindings.clear();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject asJsonObject;
            if (!jsonElement.isJsonObject() || !(asJsonObject = jsonElement.getAsJsonObject()).has("name") || !asJsonObject.has("key") || !asJsonObject.has("message")) continue;
            this.bindings.add(new Key.KeyBindingEntry(asJsonObject.get("name").getAsString(), asJsonObject.get("key").getAsInt(), asJsonObject.get("message").getAsString()));
        }
    }


    public record KeyBindingEntry(String name, int keyCode, String message) {
    }
}


