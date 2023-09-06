package io.github.sebazcrc.guilib.api;

import lombok.Getter;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SinglePagedGUI extends GUI {
    @Getter
    private InventoryPageHolder inventoryHolder;

    public SinglePagedGUI(String inventoryName, int rows) {
        super(inventoryName, rows);
    }

    public SinglePagedGUI(Player owner, String inventoryName, int rows) {
        super(owner, inventoryName, rows);
    }

    @Override
    public void open(Player player) {
        if (this.inventoryHolder == null) {
            throw new UnsupportedOperationException("You have to create the inventory first!");
        }
        this.register();
        player.openInventory(this.inventoryHolder.getBukkitInventory());
    }

    @Override
    public SinglePagedGUI createInventory() {
        this.inventoryHolder = new InventoryPageHolder(Bukkit.createInventory(null, this.getSize(), this.getInventoryName()));
        return this;
    }

    @Override
    public void cageInventory(Material material) {
        cageInventory(new ItemStack(material));
    }

    @Override
    public void cageInventory(ItemStack item) {
        for (int i = 0; i < 9; ) {
            this.setItem(i, item);
            i++;
        }
        for (int i = this.getSize() - 9; i < this.getSize(); ) {
            this.setItem(i, item);
            i++;
        }

        int last = (this.getSize() / 9 - 2);
        if (last < 1) {
            return;
        }

        for (int j = 9; j < 9 * last + 1; j += 9) {
            this.setItem(j, item);
        }
        for (int j = 17; j < 9 * (last + 1); j+= 9) {
            this.setItem(j, item);
        }
    }

    @Override
    public void fillWith(Material material) {
        this.fillWith(new ItemStack(material));
    }

    @Override
    public void fillWith(ItemStack item) {
        for (int i = 0; i < this.inventoryHolder.getBukkitInventory().getSize(); i++) {
            ItemStack currentItem = this.inventoryHolder.getBukkitInventory().getItem(i);
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                this.inventoryHolder.getBukkitInventory().setItem(i, item);
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        this.setItem(slot, item, null);
    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (this.inventoryHolder == null) {
            throw new UnsupportedOperationException("You have to create the inventory first!");
        }

        this.inventoryHolder.getBukkitInventory().setItem(slot, item);
        if (onClick != null) {
            this.inventoryHolder.getClickActions().put(slot, onClick);
        }
    }

    @Override
    public void addItem(ItemStack item) {
        this.addItem(item, null);
    }

    @Override
    public void addItem(ItemStack item, Consumer<InventoryClickEvent> onClick) {
        int slot = this.inventoryHolder.getBukkitInventory().firstEmpty();
        if (slot != -1) {
            this.setItem(slot, item, onClick);
        }
    }

    @Override
    public void removeItem(int slot) {
        if (this.inventoryHolder == null) {
            throw new UnsupportedOperationException("You need to create the inventory first!");
        }
        this.inventoryHolder.getClickActions().remove(slot);
        this.inventoryHolder.getBukkitInventory().setItem(slot, new ItemStack(Material.AIR));
    }

    @Override
    public void clear() {
        Inventory inventory = this.inventoryHolder.getBukkitInventory();
        for (int i = 0; i < this.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.AIR));
        }
        this.inventoryHolder.getClickActions().clear();
    }

    @Override
    public List<ItemStack> getContents() {
        return Arrays.stream(this.inventoryHolder.getBukkitInventory().getContents()).map(item -> item == null ? null : item.clone()).collect(Collectors.toList());
    }

    @Override
    public InventoryPageHolder getInventoryFrom(Inventory bukkitInventory) {
        return (this.inventoryHolder.getBukkitInventory().equals(bukkitInventory) ? this.inventoryHolder : null);
    }

    @Override
    protected int getInventoryIndex(InventoryPageHolder holder) {
        return 0;
    }

    @Override
    protected void handleClick(Player player, ItemStack clicked, InventoryClickEvent e) {
        if (this.inventoryHolder.getBukkitInventory().equals(e.getClickedInventory())) {
            if (this.inventoryHolder.getClickActions().containsKey(e.getSlot())) {
                this.inventoryHolder.getClickActions().get(e.getSlot()).accept(e);
            }
            e.setCancelled(true);
        }
    }
}
