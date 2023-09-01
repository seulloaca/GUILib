package io.github.sebazcrc.guilib.api.event;

import lombok.Getter;
import io.github.sebazcrc.guilib.api.GUI;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * <p>
 *     This event gets called when a player completely closes a GUI.
 * </p>
 * <p>
 *     Turning the pages of the GUI will fire {@link GUITurnPageEvent} instead
 * </p>
 */
@Getter
public class GUICloseEvent extends GUIEvent {
    private final InventoryCloseEvent bukkitEvent;

    public GUICloseEvent(GUI gui, InventoryPageHolder inventory, InventoryCloseEvent bukkitEvent) {
        super(gui, inventory);
        this.bukkitEvent = bukkitEvent;
    }

    /**
     * Returns the player involved in this event
     * @return Player who closed the GUI
     */
    public Player getPlayer() {
        return (Player) this.bukkitEvent.getPlayer();
    }
}
