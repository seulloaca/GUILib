package io.github.sebazcrc.guilib;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        GUILibCore.init(this);
    }

    @Override
    public void onDisable() {

    }
}
