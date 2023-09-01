package io.github.sebazcrc.guilib;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.function.Consumer;

@Getter
public class InventoryPageHolder {
    /**
     * The bukkit inventory for this page
     *
     * @return Bukkit inventory
     */
    private final Inventory bukkitInventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickActions;

    public InventoryPageHolder(Inventory bukkitInventory) {
        this.bukkitInventory = bukkitInventory;
        this.clickActions = Maps.newHashMap();
    }

    public void onClick(InventoryClickEvent event) {
        if (clickActions.containsKey(event.getSlot())) {
            clickActions.get(event.getSlot()).accept(event);
        }
    }
}
