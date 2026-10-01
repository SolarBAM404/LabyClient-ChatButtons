package me.solar.laby.chatbuttons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Client copy of display-only button data. It intentionally has no command field. */
public final class ButtonState {
  private static final int MAX_PAYLOAD_BYTES = 16_384;
  private static final int MAX_BUTTONS = 16;

  private static volatile List<Button> buttons = List.of();

  private ButtonState() {}

  public static List<Button> buttons() {
    return buttons;
  }

  public static void clear() {
    buttons = List.of();
  }

  public static void replaceFromServer(byte[] payload) {
    if (payload.length == 0 || payload.length > MAX_PAYLOAD_BYTES) {
      return;
    }

    try {
      JsonObject root =
          JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
      if (!"buttons".equals(root.get("type").getAsString())) {
        return;
      }

      JsonArray incoming = root.getAsJsonArray("buttons");
      if (incoming == null || incoming.size() > MAX_BUTTONS) {
        return;
      }

      List<Button> parsed = new ArrayList<>();
      for (JsonElement element : incoming) {
        if (!element.isJsonObject()) {
          return;
        }

        JsonObject item = element.getAsJsonObject();
        String id = item.get("id").getAsString();
        String label = item.get("label").getAsString();
        String tooltip = item.has("tooltip") ? item.get("tooltip").getAsString() : "";
        int backgroundColor =
            item.has("backgroundColor") ? item.get("backgroundColor").getAsInt() : 0xC0303030;
        int textColor = item.has("textColor") ? item.get("textColor").getAsInt() : 0xFFFFFFFF;
        String itemIcon = item.has("itemIcon") ? item.get("itemIcon").getAsString() : "";

        if (id.isBlank()
            || id.length() > 64
            || label.isBlank()
            || label.length() > 48
            || tooltip.length() > 160) {
          return;
        }

        if (itemIcon.length() > 140 || (backgroundColor & 0xFF000000) == 0) {
          return;
        }

        parsed.add(new Button(id, label, tooltip, backgroundColor, textColor, itemIcon));
      }

      buttons = List.copyOf(parsed);
    } catch (RuntimeException ignored) {
      // Ignore malformed or unexpected server payloads and retain the last valid state.
    }
  }

  public record Button(
      String id,
      String label,
      String tooltip,
      int backgroundColor,
      int textColor,
      String itemIcon) {}
}
