package me.solar.laby.chatbuttons.api;

import java.util.Objects;

/** Immutable server-owned action shown as a button in the LabyMod chat screen. */
public record ChatButton(
    String id,
    String label,
    String tooltip,
    String permission,
    String command,
    Integer backgroundColor,
    Integer textColor,
    String itemIcon) {
  public ChatButton {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(command, "command");
    if (!id.matches("[a-zA-Z0-9_.:-]{1,64}"))
      throw new IllegalArgumentException("Invalid button id");
    if (label.isBlank() || label.length() > 48)
      throw new IllegalArgumentException("Label must be 1-48 characters");
    if (tooltip != null && tooltip.length() > 160)
      throw new IllegalArgumentException("Tooltip is too long");
    if (itemIcon != null && !itemIcon.matches("[a-z0-9_.-]+:[a-z0-9_./-]{1,128}")) {
      throw new IllegalArgumentException(
          "Item icon must be a namespaced item id, such as minecraft:diamond");
    }
    if (permission != null && permission.isBlank()) permission = null;
    String normalized = command.startsWith("/") ? command.substring(1) : command;
    if (normalized.isBlank()
        || normalized.length() > 256
        || normalized.indexOf('\n') >= 0
        || normalized.indexOf('\r') >= 0) {
      throw new IllegalArgumentException(
          "Command must be a single non-empty command of at most 256 characters");
    }
    command = normalized;
  }

  public ChatButton(String id, String label, String tooltip, String permission, String command) {
    this(id, label, tooltip, permission, command, null, null, null);
  }

  public ChatButton withColors(int backgroundColor, int textColor) {
    return new ChatButton(
        this.id,
        this.label,
        this.tooltip,
        this.permission,
        this.command,
        backgroundColor,
        textColor,
        this.itemIcon);
  }

  public ChatButton withItemIcon(String itemIcon) {
    return new ChatButton(
        this.id,
        this.label,
        this.tooltip,
        this.permission,
        this.command,
        this.backgroundColor,
        this.textColor,
        itemIcon);
  }

  public ChatButton(String id, String label, String command) {
    this(id, label, null, null, command);
  }
}
