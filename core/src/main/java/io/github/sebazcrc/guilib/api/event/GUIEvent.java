package io.github.sebazcrc.guilib.api.event;

import io.github.sebazcrc.guilib.api.GUI;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class GUIEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final GUI gui;
    private final InventoryPageHolder inventory;

    public GUIEvent(GUI gui, InventoryPageHolder inventory) {
        this.gui = gui;
        this.inventory = inventory;
    }

    /**
     * Returns the GUI used in this event
     * @return GUI used in this event
     */
    public GUI getGui() {
        return gui;
    }

    /**
     * Returns the inventory holder for this event
     * @return InventoryHolder used in this event
     */
    public InventoryPageHolder getInventory() {
        return inventory;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
