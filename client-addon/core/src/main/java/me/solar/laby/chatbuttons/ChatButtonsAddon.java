package me.solar.laby.chatbuttons;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import me.solar.laby.chatbuttons.client.ButtonState;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class ChatButtonsAddon extends LabyAddon<ChatButtonsConfig> {
  private static volatile Supplier<Boolean> showButtons = () -> true;
  private static volatile boolean active;

  private static final byte[] SYNC_REQUEST = "{\"type\":\"sync\"}".getBytes(StandardCharsets.UTF_8);

  private ResourceLocation channel;

  @Override
  protected void enable() {
    ButtonState.clear();
    showButtons =
        () -> this.configuration().enabled().get() && this.configuration().showButtons().get();
    active = this.configuration().enabled().get();
    this.channel = ResourceLocation.create("chatbuttons", "main");
    Laby.references().payloadRegistry().registerPayloadChannel(this.channel);
    this.registerListener(this);

    this.logger().info("Chat Buttons enabled");
  }

  public static boolean shouldShowButtons() {
    return active && showButtons.get();
  }

  @Override
  protected void onDeactivated() {
    active = false;
    ButtonState.clear();
  }

  @Override
  protected void onActivated() {
    ButtonState.clear();
    active = true;

    if (Laby.labyAPI().serverController().isConnected()) {
      Laby.labyAPI().serverController().sendPayload(this.channel, SYNC_REQUEST);
    }
  }

  @Subscribe
  public void onPayload(NetworkPayloadEvent event) {
    if (!active
        || event.side() != NetworkPayloadEvent.Side.RECEIVE
        || !"chatbuttons".equals(event.identifier().getNamespace())
        || !"main".equals(event.identifier().getPath())) {
      return;
    }

    ButtonState.replaceFromServer(event.getPayload());
  }

  @Subscribe
  public void onServerDisconnect(ServerDisconnectEvent event) {
    ButtonState.clear();
  }

  @Override
  protected Class<ChatButtonsConfig> configurationClass() {
    return ChatButtonsConfig.class;
  }
}
