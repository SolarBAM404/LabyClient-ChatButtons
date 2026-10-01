package me.solar.laby.chatbuttons.api;

import java.util.Collection;
import org.bukkit.entity.Player;

/** API for server plugins to manage buttons for connected players. */
public interface ChatButtonService {
  void setButtons(Player player, Collection<ChatButton> buttons);

  void addButton(Player player, ChatButton button);

  boolean removeButton(Player player, String id);

  void clearButtons(Player player);

  void clearAll();

  boolean isSupported(Player player);

  void unregister();
}
