package io.github.sebazcrc.guilib;

import org.bukkit.plugin.Plugin;

public class GUILibCore {
    private static Plugin plugin;

    /**
     * Sets the plugin to be used to register the listeners
     * @param bukkitPlugin Your plugin instance
     */
    public static void init(Plugin bukkitPlugin) {
        plugin = bukkitPlugin;
    }

    /**
     * Returns the Bukkit plugin used
     * @return Plugin used to register the listeners
     */
    public static Plugin getPlugin() {
        if (plugin == null) {
            throw new NullPointerException("You must set the plugin calling GuiLibCore#init before creating any Gui");
        }
        return plugin;
    }
}
