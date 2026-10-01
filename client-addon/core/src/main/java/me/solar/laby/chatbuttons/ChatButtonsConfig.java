package me.solar.laby.chatbuttons;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("settings")
public class ChatButtonsConfig extends AddonConfig {
  @SwitchSetting private final ConfigProperty<Boolean> showButtons = new ConfigProperty<>(true);

  @SwitchSetting private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Boolean> showButtons() {
    return this.showButtons;
  }
}
