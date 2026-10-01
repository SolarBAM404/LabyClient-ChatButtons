package me.solar.laby.chatbuttons.v26_2.mixins;

import java.nio.charset.StandardCharsets;
import me.solar.laby.chatbuttons.ChatButtonsAddon;
import me.solar.laby.chatbuttons.client.ButtonState;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {
  private static final int BUTTON_HEIGHT = 18;
  private static final int INPUT_GAP = 4;
  private static final int BUTTON_GAP = 3;

  @Shadow protected EditBox input;

  private int chatbuttons$rowY() {
    return this.input != null
        ? this.input.getY() - BUTTON_HEIGHT - INPUT_GAP
        : Minecraft.getInstance().getWindow().getGuiScaledHeight() - 36;
  }

  @Inject(method = "extractRenderState", at = @At("TAIL"))
  private void chatbuttons$renderButtons(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float partialTick,
      CallbackInfo callbackInfo) {
    ButtonRenderer.draw(graphics, mouseX, mouseY, this.chatbuttons$rowY());
  }

  @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
  private void chatbuttons$clickButton(
      MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfo) {
    chatbuttons$handleClick(event, callbackInfo, this.chatbuttons$rowY());
  }

  private void chatbuttons$handleClick(
      MouseButtonEvent event, CallbackInfoReturnable<Boolean> callbackInfo, int y) {
    if (event.button() != 0
        || !ChatButtonsAddon.shouldShowButtons()
        || ButtonState.buttons().isEmpty()) {
      return;
    }

    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.getConnection() == null) {
      return;
    }

    int x = 4;
    for (ButtonState.Button chatButton : ButtonState.buttons()) {
      int width = ButtonRenderer.buttonWidth(chatButton, minecraft);

      if (event.x() >= x
          && event.x() <= x + width
          && event.y() >= y
          && event.y() <= y + BUTTON_HEIGHT) {
        byte[] payload =
            ("{\"type\":\"click\",\"id\":" + quote(chatButton.id()) + "}")
                .getBytes(StandardCharsets.UTF_8);
        Laby.labyAPI()
            .serverController()
            .sendPayload(ResourceLocation.create("chatbuttons", "main"), payload);
        callbackInfo.setReturnValue(true);
        return;
      }

      x += width + BUTTON_GAP;
    }
  }

  private static String quote(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
