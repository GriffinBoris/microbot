package net.runelite.client.plugins.microbot.util.inventory;

import lombok.Getter;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;

public class Rs2Item {
    public  int id;
    public  int quantity;
    @Getter
    public int slot = -1;
    public String name;
    String[] actions;
    boolean isStackable;
    public Rs2Item(Item item, ItemComposition itemComposition, int slot) {
        this.id = item.getId();
        this.quantity = item.getQuantity();
        this.name = itemComposition.getName();
        this.actions = itemComposition.getInventoryActions();
        this.slot = slot;
        this.isStackable = itemComposition.isStackable();
    }
    public Rs2Item(Widget item, int slot) {
        this.id = item.getItemId();
        this.quantity = item.getItemQuantity();
        this.slot = slot;
        this.name = item.getName().split(">")[1].split("</")[0];
        this.actions = item.getActions();
    }
}
