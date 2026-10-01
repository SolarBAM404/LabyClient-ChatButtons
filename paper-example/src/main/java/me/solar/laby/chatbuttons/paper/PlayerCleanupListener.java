package me.solar.laby.chatbuttons.paper;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

final class PlayerCleanupListener implements Listener {
  private final PaperChatButtonService service;

  PlayerCleanupListener(PaperChatButtonService service) {
    this.service = service;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    UUID id = event.getPlayer().getUniqueId();
    this.service.forget(id);
  }
}
