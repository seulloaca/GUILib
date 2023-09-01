package io.github.sebazcrc.guilib.api.event;

import lombok.Getter;
import io.github.sebazcrc.guilib.api.GUI;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class GUIOpenEvent extends GUIEvent {
    /**
     * Bukkit InventoryOpenEvent
     *
     * @return The Bukkit's InventoryOpenEvent
     */
    private final InventoryOpenEvent bukkitEvent;
    /**
     *  Page index for this event
     *
     * @return The page index used in this event
     */
    @Getter
    private final int page;

    public GUIOpenEvent(GUI gui, InventoryPageHolder inventory, int page, InventoryOpenEvent bukkitEvent) {
        super(gui, inventory);
        this.bukkitEvent = bukkitEvent;
        this.page = page;
    }

    /**
     * Returns the player involved in this event
     * @return Player who opened this GUI
     */
    public Player getPlayer() {
        return (Player) this.bukkitEvent.getPlayer();
    }
}
