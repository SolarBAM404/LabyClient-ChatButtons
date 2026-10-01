package me.solar.laby.chatbuttons.example;

import java.util.List;
import me.solar.laby.chatbuttons.api.ChatButton;
import me.solar.laby.chatbuttons.paper.PaperChatButtonService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ChatButtonsPlugin extends JavaPlugin {
  private PaperChatButtonService buttons;

  @Override
  public void onEnable() {
    this.buttons = new PaperChatButtonService(this);
    getLogger().info("Chat Buttons API ready. Install the LabyMod addon to see demo buttons.");
  }

  @Override
  public void onDisable() {
    if (this.buttons != null) {
      this.buttons.unregister();
    }
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("This command is for players.");
      return true;
    }

    if (args.length != 1) {
      player.sendMessage("Usage: /chatbuttons <show|remove>");
      return true;
    }

    if (args[0].equalsIgnoreCase("show")) {
      showDemoButtons(player);
      return true;
    }

    if (args[0].equalsIgnoreCase("remove")) {
      this.buttons.clearButtons(player);
      player.sendMessage("Your chat buttons were removed.");
      return true;
    }

    player.sendMessage("Usage: /chatbuttons <show|remove>");
    return true;
  }

  private void showDemoButtons(Player player) {
    this.buttons.setButtons(player, demoButtons());
    player.sendMessage(
        "Demo chat buttons sent. They are visible by default; open Chat Buttons settings to hide them.");
  }

  private static List<ChatButton> demoButtons() {
    ChatButton creative =
        new ChatButton(
                "creative",
                "Creative",
                "Switch to creative mode",
                "chatbuttons.example.creative",
                "gamemode creative")
            .withColors(0xFF388E3C, 0xFFFFFFFF)
            .withItemIcon("minecraft:emerald");

    ChatButton survival =
        new ChatButton(
                "survival",
                "Survival",
                "Switch back to survival",
                "chatbuttons.example.creative",
                "gamemode survival")
            .withColors(0xFF455A64, 0xFFFFFFFF)
            .withItemIcon("minecraft:iron_sword");

    ChatButton day =
        new ChatButton("day", "Set Day", "Set the world time to day", null, "time set day")
            .withColors(0xFFF9A825, 0xFF202020)
            .withItemIcon("minecraft:sunflower");

    ChatButton heal =
        new ChatButton(
                "heal",
                "Heal",
                "Restore your health",
                null,
                "effect give @s minecraft:instant_health 1 1 true")
            .withColors(0xFFC62828, 0xFFFFFFFF)
            .withItemIcon("minecraft:golden_apple");

    ChatButton plain =
        new ChatButton(
            "plain", "Plain", "Default styling without an icon", null, "say Plain button example");

    return List.of(creative, survival, day, heal, plain);
  }

  public PaperChatButtonService chatButtons() {
    return this.buttons;
  }
}
