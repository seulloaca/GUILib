package io.github.sebazcrc.guilib;

import io.github.sebazcrc.guilib.api.event.GUICloseEvent;
import io.github.sebazcrc.guilib.api.event.GUIOpenEvent;
import io.github.sebazcrc.guilib.api.SinglePagedGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        GUILibCore.init(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (e.isSneaking()) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta mp = previous.getItemMeta();
            mp.setDisplayName(ChatColor.GREEN + "Previous page");
            previous.setItemMeta(mp);

            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta np = next.getItemMeta();
            np.setDisplayName(ChatColor.GREEN + "Next page");
            next.setItemMeta(np);

            SinglePagedGUI gui = new SinglePagedGUI(e.getPlayer(), ChatColor.GREEN + "My inventory", 5);
            gui.createInventory();

            gui.setItem(10, new ItemStack(Material.APPLE), (click -> {
                Player player = (Player) click.getWhoClicked();
                player.sendMessage("Yay!!! Apples!!!");
            }));

            gui.setItem(11, new ItemStack(Material.DIAMOND), (click -> {
                Player player = (Player) click.getWhoClicked();
                player.sendMessage("diamond");
            }));

            gui.addItem(new ItemStack(Material.OBSIDIAN), (click -> {
                Player player = (Player) click.getWhoClicked();
                player.sendMessage("added item");
            }));

            gui.fillWith(Material.GRAY_STAINED_GLASS_PANE);

            gui.open(e.getPlayer());
        }
    }

    @EventHandler
    public void onOpen(GUIOpenEvent e) {
        Bukkit.broadcastMessage("open " + e.getGui() + " | " + e.getPlayer().getName());
    }

    @EventHandler
    public void onClose(GUICloseEvent e) {
        Bukkit.broadcastMessage("close " + e.getGui() + " | " + e.getPlayer().getName());
    }

    @Override
    public void onDisable() {

    }
}
