package io.github.sebazcrc.guilib.api;

import io.github.sebazcrc.guilib.GUILibCore;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import io.github.sebazcrc.guilib.api.event.GUICloseEvent;
import io.github.sebazcrc.guilib.api.event.GUIOpenEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a GUI object
 */
public abstract class GUI implements Listener {
    /**
     * Returns the owning player if any
     *
     * @return The owner of this Gui or null if not set
     */
    @Getter
    private final Player owner;
    /**
     * Returns the inventory name of this GUI
     *
     * @return String the inventory name
     */
    @Getter
    private final String inventoryName;
    /**
     * Returns the inventory size
     *
     * @return Inventory size
     */
    @Getter
    private final int size;
    private static int guiCount = -1;
    /**
     * Returns the unique id assigned to this GUI
     *
     * @return The id
     */
    @Getter
    private final int id;
    private boolean registered;

    public GUI(String inventoryName, int rows) {
        this(null, inventoryName, rows);
    }

    public GUI(Player owner, String inventoryName, int rows) {
        this.owner = owner;
        this.inventoryName = inventoryName;
        this.size = Math.max(9, Math.min(54, rows * 9));
        this.registered = false;

        this.id = ++guiCount;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerOpen(InventoryOpenEvent e) {
        if (e.isCancelled()) {
            return;
        }

        InventoryPageHolder holder = getInventoryFrom(e.getInventory());
        if (holder != null) {
            // We are talking about this GUI!
            GUIOpenEvent openEvent = new GUIOpenEvent(this, e, holder, getInventoryIndex(holder));
            Bukkit.getServer().getPluginManager().callEvent(openEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(GUILibCore.getPlugin(), () -> {
                if (this.isPlayerOwned() && isOwner(player)) {
                    // Check if the player has really closed the Gui (just to make sure we don't unregister it because the player turned the page)
                    InventoryPageHolder holder = this.getInventoryFrom(e.getPlayer().getOpenInventory().getTopInventory());
                    if (holder == null) {
                        // The player has closed this gui, unregister every listener
                        GUICloseEvent openEvent = new GUICloseEvent(this, e, holder);
                        Bukkit.getServer().getPluginManager().callEvent(openEvent);

                        this.unregister();
                    }
                }

                // Prevent a client de-sync when swapping to offhand in survival
                player.updateInventory();
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;
        // We only want the right player to click here!
        if (!isOwner(player)) return;

        ItemStack clicked;
        if (e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            clicked = e.getInventory().getItem(e.getHotbarButton() == -1 ? 40 : e.getHotbarButton());
        } else {
            clicked = e.getCurrentItem();
        }
        this.handleClick(player, clicked, e);
    }

    protected void register() {
        if (!this.registered) {
            Bukkit.getServer().getPluginManager().registerEvents(this, GUILibCore.getPlugin());
            this.registered = true;
        }
    }

    protected void unregister() {
        if (this.registered) {
            HandlerList.unregisterAll(this);
            this.registered = false;
        }
    }

    /**
     * Returns whether this GUI belongs to a player or not
     * @return <code>true</code> if this GUI belongs to someone
     */
    public boolean isPlayerOwned() {
        return this.owner != null;
    }

    /**
     * Returns whether this player may use this GUI or not
     * @param player Player
     * @return <code>true</code> if the player is allowed to use this GUI
     */
    public boolean isOwner(Player player) {
        // If the owner is null, then anyone would be able to click
        // In the other hand, the clicker must be the owner
        return (this.owner == null || this.owner.equals(player));
    }

    /**
     * Opens the GUI
     * @param player Player
     */
    public abstract void open(Player player);

    /**
     * Registers the inventory, it must be called before performing any action in the GUI
     * @return this GUI
     */
    public abstract GUI createInventory();

    /**
     * Fills the GUI with the given material
     * @param material Material
     */
    public abstract void fillWith(Material material);

    /**
     * Fills the GUI with the given ItemStack
     * @param item ItemStack
     */
    public abstract void fillWith(ItemStack item);

    /**
     * Sets the item in the slot
     * @param slot Slot
     * @param item ItemStack
     */
    public abstract void setItem(int slot, ItemStack item);

    /**
     * Sets the item in the slot
     * @param slot Slot
     * @param item ItemStack
     * @param onClick Consumer used whenever this item is clicked
     */
    public abstract void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick);

    /**
     * Adds the item in the first empty slot
     * @param item ItemStack
     */
    public abstract void addItem(ItemStack item);

    /**
     * Adds the item on the first empty slot
     * @param item ItemStack
     * @param onClick Consumer used whenever this item is clicked
     */
    public abstract void addItem(ItemStack item, Consumer<InventoryClickEvent> onClick);

    /**
     * Removes an item from the GUI
     * @param slot Slot
     */
    public abstract void removeItem(int slot);

    /**
     * Clears the entire inventory, except for the special items (like the previous or next icons)
     */
    public abstract void clear();

    /**
     * Returns a copy of this Gui contents
     * @return A list containing a copy of each item in this Gui, including nulls
     */
    public abstract List<ItemStack> getContents();

    /**
     * Returns the {@link InventoryPageHolder} representation for the given Bukkit inventory
     * @param bukkitInventory Bukkit inventory
     * @return If found, the {@link InventoryPageHolder} for this Bukkit inventory
     */
    public abstract InventoryPageHolder getInventoryFrom(Inventory bukkitInventory);
    protected abstract int getInventoryIndex(InventoryPageHolder holder);
    protected abstract void handleClick(Player player, ItemStack clicked, InventoryClickEvent e);

    @Override
    public boolean equals(Object object) {
        return object instanceof GUI g && g.getId() == this.getId();
    }
}
