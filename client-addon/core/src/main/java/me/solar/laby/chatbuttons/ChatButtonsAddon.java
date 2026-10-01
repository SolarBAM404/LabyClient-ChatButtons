package me.solar.laby.chatbuttons;

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

  @Override
  protected void enable() {
    showButtons = () -> this.configuration().showButtons().get();
    ResourceLocation channel = ResourceLocation.create("chatbuttons", "main");
    Laby.references().payloadRegistry().registerPayloadChannel(channel);
    this.registerListener(this);

    this.logger().info("Chat Buttons enabled");
  }

  public static boolean shouldShowButtons() {
    return showButtons.get();
  }

  @Subscribe
  public void onPayload(NetworkPayloadEvent event) {
    if (event.side() != NetworkPayloadEvent.Side.RECEIVE
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
