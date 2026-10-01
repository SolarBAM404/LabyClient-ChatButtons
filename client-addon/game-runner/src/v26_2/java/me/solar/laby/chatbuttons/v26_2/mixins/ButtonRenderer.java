package me.solar.laby.chatbuttons.v26_2.mixins;

import me.solar.laby.chatbuttons.ChatButtonsAddon;
import me.solar.laby.chatbuttons.client.ButtonState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shared HUD/chat drawing code kept outside Mixin classes. */
public final class ButtonRenderer {
  private static final int BUTTON_HEIGHT = 18;
  private static final int BUTTON_GAP = 3;
  private static final int BUTTON_PADDING = 7;
  private static List<ButtonState.Button> cachedButtonState = List.of();
  private static List<RenderedButton> renderedButtons = List.of();

  private ButtonRenderer() {}

  public static int buttonWidth(ButtonState.Button button, Minecraft minecraft) {
    int iconWidth = button.itemIcon().isBlank() ? 0 : 18;
    return Math.max(
        38, Math.min(140, minecraft.font.width(button.label()) + BUTTON_PADDING * 2 + iconWidth));
  }

  public static void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int y) {
    var buttons = ButtonState.buttons();
    if (!ChatButtonsAddon.shouldShowButtons() || buttons.isEmpty()) return;

    Minecraft minecraft = Minecraft.getInstance();
    List<RenderedButton> preparedButtons = prepareButtons(buttons, minecraft);
    int x = 4;
    for (int i = 0; i < preparedButtons.size(); i++) {
      RenderedButton renderedButton = preparedButtons.get(i);
      ButtonState.Button button = renderedButton.button();
      int width = renderedButton.width();
      boolean hovered =
          mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
      int background = hovered ? lighten(button.backgroundColor()) : button.backgroundColor();
      graphics.fill(x, y, x + width, y + BUTTON_HEIGHT, background);
      graphics.fill(x, y, x + width, y + 1, button.textColor());

      int labelX = x + BUTTON_PADDING;
      if (renderedButton.itemStack() != null) {
        graphics.item(renderedButton.itemStack(), x + 2, y + 1);
        labelX = x + 21;
      }
      graphics.text(
          minecraft.font,
          button.label(),
          labelX,
          y + (BUTTON_HEIGHT - minecraft.font.lineHeight) / 2,
          button.textColor(),
          false);
      if (hovered && renderedButton.tooltip() != null) {
        graphics.setTooltipForNextFrame(minecraft.font, renderedButton.tooltip(), mouseX, mouseY);
      }
      x += width + BUTTON_GAP;
    }
  }

  private static List<RenderedButton> prepareButtons(
      List<ButtonState.Button> buttons, Minecraft minecraft) {
    if (buttons != cachedButtonState) {
      List<RenderedButton> prepared = new ArrayList<>(buttons.size());
      for (int i = 0; i < buttons.size(); i++) {
        ButtonState.Button button = buttons.get(i);
        ItemStack itemStack = null;
        if (!button.itemIcon().isBlank()) {
          var itemId = net.minecraft.resources.Identifier.tryParse(button.itemIcon());
          var item =
              itemId == null
                  ? null
                  : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(itemId);
          if (item != null) {
            itemStack = new ItemStack(item);
          }
        }
        Component tooltip = button.tooltip().isBlank() ? null : Component.literal(button.tooltip());
        prepared.add(
            new RenderedButton(button, buttonWidth(button, minecraft), itemStack, tooltip));
      }
      cachedButtonState = buttons;
      renderedButtons = List.copyOf(prepared);
    }
    return renderedButtons;
  }

  private static int lighten(int color) {
    int a = color >>> 24;
    int r = Math.min(255, ((color >>> 16) & 0xFF) + 24);
    int g = Math.min(255, ((color >>> 8) & 0xFF) + 24);
    int b = Math.min(255, (color & 0xFF) + 24);
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Stores server-authored presentation data for one render snapshot. Labels and tooltips come from
   * the connected server's plugin at runtime, so they cannot be translated by this addon.
   */
  private record RenderedButton(
      ButtonState.Button button, int width, ItemStack itemStack, Component tooltip) {}
}
