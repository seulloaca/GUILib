package io.github.sebazcrc.guilib.api;

import com.google.common.collect.Maps;
import io.github.sebazcrc.guilib.GUILibCore;
import io.github.sebazcrc.guilib.InventoryPageHolder;
import io.github.sebazcrc.guilib.api.event.GUITurnPageEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PagedGUI extends GUI {
    public static String pageFormat = (ChatColor.RESET + "" + ChatColor.DARK_GRAY + "({page})");

    @Getter
    private final PaginationProperties properties;

    @Getter
    private final ItemStack previousItem, nextItem;

    @Getter
    public int previousSlot;
    @Getter
    public int nextSlot;
    @Getter
    private final Map<Integer, InventoryPageHolder> pages;
    private int freePage;

    public PagedGUI(String inventoryName, int rows, ItemStack previousItem, ItemStack nextItem) {
        this(inventoryName, rows, previousItem, nextItem, new PaginationProperties());
    }

    public PagedGUI(String inventoryName, int rows, ItemStack previousItem, ItemStack nextItem, PaginationProperties properties) {
        this(null, inventoryName, rows, previousItem, nextItem, properties);
    }

    public PagedGUI(Player owner, String inventoryName, int rows, ItemStack previousItem, ItemStack nextItem) {
        this(owner, inventoryName, rows, previousItem, nextItem, new PaginationProperties());
    }

    public PagedGUI(Player owner, String inventoryName, int rows, ItemStack previousItem, ItemStack nextItem, PaginationProperties properties) {
        super(owner, inventoryName, rows);
        this.properties = properties;
        this.previousItem = previousItem;
        this.nextItem = nextItem;
        this.previousSlot = getSize() - 9;
        this.nextSlot = getSize() - 1;
        this.pages = Maps.newHashMap();
        this.freePage = -1;
    }

    @Override
    public void open(Player player) {
        if (this.pages.isEmpty()) {
            throw new UnsupportedOperationException("You have to add a page first!");
        }
        this.register();
        player.openInventory(this.getPageAt(0).getBukkitInventory());
    }

    @Override
    public PagedGUI createInventory() {
        this.addPage(true);
        return this;
    }

    @Override
    public void fillWith(Material material) {
        this.fillWith(new ItemStack(material));
    }

    @Override
    public void fillWith(ItemStack item) {
        for (int i = 0; i < getPagesSize(); i++) {
            this.fillWith(i, item);
        }
    }

    /**
     * Fills the GUI with the given material
     * @param pageIndex The page to fill
     * @param material Material
     */
    public void fillWith(int pageIndex, Material material) {
        this.fillWith(pageIndex, new ItemStack(material));
    }

    /**
     * Fills the GUI with the given ItemStack
     * @param pageIndex The page to fill
     * @param item ItemStack
     */
    public void fillWith(int pageIndex, ItemStack item) {
        InventoryPageHolder holder = this.getPageAt(pageIndex);
        for (int i = 0; i < holder.getBukkitInventory().getSize(); i++) {
            ItemStack currentItem = holder.getBukkitInventory().getItem(i);
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                holder.getBukkitInventory().setItem(i, item);
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        this.setItem(slot, item, null);
    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        this.setItem(0, slot, item, onClick);
    }

    /**
     * Sets the item in the slot in the specified page
     * @param pageIndex Page
     * @param slot Slot
     * @param item ItemStack
     */
    public void setItem(int pageIndex, int slot, ItemStack item) {
        this.setItem(pageIndex, slot, item, null);
    }

    /**
     * Sets the item in the slot in the specified page
     * @param pageIndex Page
     * @param slot Slot
     * @param item ItemStack
     * @param onClick Consumer used whenever this item is clicked
     */
    public void setItem(int pageIndex, int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (this.pages.isEmpty()) {
            throw new UnsupportedOperationException("You have to add a page first!");
        }
        InventoryPageHolder holder = this.getPageAt(pageIndex);
        holder.getBukkitInventory().setItem(slot, item);
        if (onClick != null) {
            holder.getClickActions().put(slot, onClick);
        }
    }

    @Override
    public void addItem(ItemStack item) {
        this.addItem(item, null);
    }

    @Override
    public void addItem(ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (this.freePage == -1) {
            // Try to find a free page
            this.freePage = findFirstFreePage();
        }

        // If the previous call found a free page or there was already one, continue
        if (this.freePage != -1) {
            Inventory inv = this.getPageAt(freePage).getBukkitInventory();

            // Set the item on the first empty slot
            this.setItem(this.freePage, inv.firstEmpty(), item, onClick);

            // Check if the inventory still has free slots, if so, keeps it. Otherwise, sets the freePage attribute to -1
            this.freePage = (inv.firstEmpty() == -1 ? -1 : freePage);
        }
    }

    @Override
    public void removeItem(int slot) {
        removeItem(0, slot);
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.getPagesSize(); i++) {
            this.clearPage(i);
        }
    }

    @Override
    public List<ItemStack> getContents() {
        return getContents(0);
    }

    @Override
    public void handleClick(Player player, ItemStack clicked, InventoryClickEvent e) {
        InventoryPageHolder holder = null;
        int pageIndex;
        for (pageIndex = 0; pageIndex < this.pages.size(); pageIndex++) {
            InventoryPageHolder ci = this.pages.get(pageIndex);
            if (ci.getBukkitInventory().equals(e.getClickedInventory())) {
                holder = ci;
                break;
            }
        }

        if (holder != null) {
            // One of the pages has been clicked

            if (e.getHotbarButton() == 40) {
                e.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(GUILibCore.getPlugin(), player::updateInventory, 1L);
                return;
            }

            InventoryPageHolder next = null;
            if (compareItem(clicked, this.nextItem)) {
                // Next page
                next = this.getPageAt(pageIndex + 1);
            } else if (compareItem(clicked, this.previousItem)) {
                // Previous page
                next = this.getPageAt(pageIndex - 1);
            } else if (holder.getClickActions().containsKey(e.getSlot())) {
                holder.getClickActions().get(e.getSlot()).accept(e);
            }

            if (next != null) {
                GUITurnPageEvent event = new GUITurnPageEvent(this, player, next, holder);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled() && event.getNext() != null) {
                    player.closeInventory();
                    player.openInventory(event.getNext().getBukkitInventory());
                }
            }
            e.setCancelled(true);
        }
    }

    public List<ItemStack> getContents(int pageIndex) {
        if (this.pages.isEmpty()) {
            throw new UnsupportedOperationException("You have to add a page first!");
        }
        return Arrays.stream(this.getPageAt(pageIndex).getBukkitInventory().getContents()).map(item -> item == null ? null : item.clone()).collect(Collectors.toList());
    }

    /**
     * Clears all the items in the page
     * @param pageIndex Page
     */
    public void clearPage(int pageIndex) {
        InventoryPageHolder holder = this.getPageAt(pageIndex);
        Inventory inventory = holder.getBukkitInventory();
        for (int i = 0; i < this.getSize(); i++) {
            if (i == previousSlot || i == nextSlot) continue;
            inventory.setItem(i, new ItemStack(Material.AIR));
        }
        holder.getClickActions().clear();
    }

    public void removeItem(int pageIndex, int slot) {
        if (this.pages.isEmpty()) {
            throw new UnsupportedOperationException("You have to add a page first!");
        }
        InventoryPageHolder holder = getPageAt(pageIndex);
        holder.getClickActions().remove(slot);
        holder.getBukkitInventory().setItem(slot, new ItemStack(Material.AIR));
    }

    /**
     * Returns the index of the first available page (containing empty slots)
     * @return Page index
     */
    public int findFirstFreePage() {
        for (int i = 0; i < this.pages.size(); i++) {
            Inventory inventory = this.getPageAt(i).getBukkitInventory();
            int firstEmpty = inventory.firstEmpty();
            if (firstEmpty != -1) {
                return i;
            }
        }

        // Not found
        if (this.properties.automaticallyCreatePages) {
            this.addPage(this.properties.automaticallyCreatePagesFromPrevious);
            return getPagesSize() - 1;
        }

        return -1;
    }

    /**
     * Adds a new page to the GUI
     * @return The {@link InventoryPageHolder} new added page
     */
    public InventoryPageHolder addPage() {
        return addPage(false);
    }

    /**
     * Adds a new page
     * @param copyFromPrevious Whether the new page should include the same items the previous page had or not
     * @return The new added page
     */
    public InventoryPageHolder addPage(boolean copyFromPrevious) {
        int pageIndex = this.pages.size();
        int pageNumber = pageIndex + 1;

        InventoryPageHolder inventory = new InventoryPageHolder(Bukkit.createInventory(null, this.getSize(), getInventoryName() + " " + pageFormat.replace("{page}", String.valueOf(pageNumber))));

        if (pageIndex >= 1) {
            // This means that there is more than one page
            // Get the previous page and add the "next" button item
            int previous = pageIndex - 1;
            InventoryPageHolder previousPage = this.getPageAt(previous);

            if (copyFromPrevious) {
                previousPage.getClickActions().forEach((slot, click) -> {
                    inventory.getClickActions().put(slot, click);
                });
                inventory.getBukkitInventory().setContents(
                        Arrays.stream(previousPage.getBukkitInventory().getContents())
                                .map(item -> (item == null ? null : item.clone()))
                                .collect(Collectors.toList())
                                .toArray(new ItemStack[0])
                );
            }

            previousPage.getBukkitInventory().setItem(this.nextSlot, this.nextItem.clone());

            // Set the proper item to go back
            inventory.getBukkitInventory().setItem(this.previousSlot, this.previousItem.clone());
        }
        this.pages.put(pageIndex, inventory);

        return inventory;
    }

    public InventoryPageHolder getFirstPage() {
        return getPageAt(0);
    }

    public InventoryPageHolder getLastPage() {
        return getPageAt(getPagesSize() - 1);
    }

    private InventoryPageHolder getPageAt(int pageIndex) {
        return this.pages.get(pageIndex);
    }
    
    @Override
    public InventoryPageHolder getInventoryFrom(Inventory inventory) {
        for (int i = 0; i < this.pages.size(); i++) {
            InventoryPageHolder ci = this.pages.get(i);
            if (inventory.equals(ci.getBukkitInventory())) return ci;
        }
        return null;
    }

    @Override
    protected int getInventoryIndex(InventoryPageHolder holder) {
        for (int pageIndex = 0; pageIndex < this.pages.size(); pageIndex++) {
            InventoryPageHolder ci = this.pages.get(pageIndex);
            if (ci.getBukkitInventory().equals(holder.getBukkitInventory())) {
                return pageIndex;
            }
        }
        return -1;
    }

    protected boolean compareItem(ItemStack first, ItemStack second) {
        return (first != null && second != null && first.getType().equals(second.getType()) && first.getItemMeta().equals(second.getItemMeta()));
    }

    public int getPagesSize() {
        return pages.size();
    }

    public static class PaginationProperties {
        private boolean automaticallyCreatePages;
        private boolean automaticallyCreatePagesFromPrevious;

        public PaginationProperties() {
            this.automaticallyCreatePages = true;
            this.automaticallyCreatePagesFromPrevious = false;
        }

        public PaginationProperties enableAutomaticPageCreation() {
            this.automaticallyCreatePages = true;
            return this;
        }

        public PaginationProperties disableAutomaticPageCreation() {
            this.automaticallyCreatePages = false;
            return this;
        }

        public PaginationProperties enablePageCopyingOnAutomaticAdding() {
            this.automaticallyCreatePagesFromPrevious = true;
            return this;
        }

        public PaginationProperties disablePageCopyingOnAutomaticAdding() {
            this.automaticallyCreatePagesFromPrevious = false;
            return this;
        }
    }
}
