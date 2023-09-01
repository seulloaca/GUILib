package io.github.sebazcrc.guilib.api.event;

import io.github.sebazcrc.guilib.api.GUI;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player turns a GUI page
 */
public class GUITurnPageEvent extends GUIEvent implements Cancellable {
    /**
     * Next page
     *
     * @param next The next page
     * @return The next page
     */
    @Getter @Setter
    private InventoryPageHolder next;
    /**
     * Player involved in this event
     *
     * @return The player who turned pages
     */
    @Getter
    private final Player player;
    private boolean cancel;

    public GUITurnPageEvent(GUI gui, Player player, InventoryPageHolder next, InventoryPageHolder previous) {
        super(gui, previous);
        this.player = player;
        this.next = next;
    }

    /**
     * Previous page
     *
     * @return The previous page
     */
    public InventoryPageHolder getPrevious() {
        return getInventory();
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
