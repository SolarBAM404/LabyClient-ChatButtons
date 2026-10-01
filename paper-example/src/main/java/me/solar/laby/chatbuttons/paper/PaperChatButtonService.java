package me.solar.laby.chatbuttons.paper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import me.solar.laby.chatbuttons.api.ChatButton;
import me.solar.laby.chatbuttons.api.ChatButtonService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

/** Paper transport and click validator. Commands always remain server-side. */
public final class PaperChatButtonService implements ChatButtonService, PluginMessageListener {
  public static final String CHANNEL = "chatbuttons:main";
  private static final Gson GSON = new Gson();
  private final Plugin plugin;
  private final Map<UUID, Map<String, ChatButton>> buttons = new LinkedHashMap<>();

  public PaperChatButtonService(Plugin plugin) {
    this.plugin = plugin;
    Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
    Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
    Bukkit.getPluginManager().registerEvents(new PlayerCleanupListener(this), plugin);
  }

  @Override
  public void setButtons(Player player, Collection<ChatButton> newButtons) {
    Map<String, ChatButton> state = new LinkedHashMap<>();
    for (ChatButton button : newButtons) {
      if (state.putIfAbsent(button.id(), button) != null)
        throw new IllegalArgumentException("Duplicate button id: " + button.id());
    }
    this.buttons.put(player.getUniqueId(), state);
    sendState(player, state.values());
  }

  @Override
  public void addButton(Player player, ChatButton button) {
    Map<String, ChatButton> state =
        this.buttons.computeIfAbsent(player.getUniqueId(), ignored -> new LinkedHashMap<>());
    state.put(button.id(), button);
    sendState(player, state.values());
  }

  @Override
  public boolean removeButton(Player player, String id) {
    Map<String, ChatButton> state = this.buttons.get(player.getUniqueId());
    if (state == null || state.remove(id) == null) return false;
    sendState(player, state.values());
    return true;
  }

  @Override
  public void clearButtons(Player player) {
    this.buttons.remove(player.getUniqueId());
    sendState(player, java.util.List.of());
  }

  @Override
  public void clearAll() {
    for (Player player : Bukkit.getOnlinePlayers()) clearButtons(player);
    this.buttons.clear();
  }

  @Override
  public boolean isSupported(Player player) {
    return player.isOnline();
  }

  private void sendState(Player player, Collection<ChatButton> state) {
    if (!player.isOnline()) return;
    JsonObject root = new JsonObject();
    root.addProperty("type", "buttons");
    JsonArray array = new JsonArray();
    for (ChatButton button : state) {
      JsonObject item = new JsonObject();
      item.addProperty("id", button.id());
      item.addProperty("label", button.label());
      if (button.tooltip() != null) item.addProperty("tooltip", button.tooltip());
      if (button.backgroundColor() != null)
        item.addProperty("backgroundColor", button.backgroundColor());
      if (button.textColor() != null) item.addProperty("textColor", button.textColor());
      if (button.itemIcon() != null) item.addProperty("itemIcon", button.itemIcon());
      array.add(item);
    }
    root.add("buttons", array);
    player.sendPluginMessage(
        this.plugin, CHANNEL, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void onPluginMessageReceived(
      @NotNull String channel, @NotNull Player player, byte @NotNull [] payload) {
    if (!CHANNEL.equals(channel) || payload.length == 0 || payload.length > 1024) return;
    try {
      JsonObject request =
          GSON.fromJson(new String(payload, StandardCharsets.UTF_8), JsonObject.class);
      if (request == null
          || !request.has("type")
          || !"click".equals(request.get("type").getAsString())) return;
      if (!request.has("id") || !request.get("id").isJsonPrimitive()) return;
      String id = request.get("id").getAsString();
      if (id.length() > 64) return;
      Map<String, ChatButton> state = this.buttons.get(player.getUniqueId());
      ChatButton button = state == null ? null : state.get(id);
      if (button == null) return;
      if (button.permission() != null && !player.hasPermission(button.permission())) {
        player.sendMessage("You do not have permission to use that button.");
        return;
      }
      Bukkit.dispatchCommand(player, button.command());
    } catch (RuntimeException exception) {
      this.plugin
          .getLogger()
          .warning("Ignored malformed chat button packet from " + player.getName());
    }
  }

  @Override
  public void unregister() {
    clearAll();
    Bukkit.getMessenger().unregisterIncomingPluginChannel(this.plugin, CHANNEL, this);
    Bukkit.getMessenger().unregisterOutgoingPluginChannel(this.plugin, CHANNEL);
  }

  void forget(UUID playerId) {
    this.buttons.remove(playerId);
  }
}
