package de.ricosw.orbismod.inventory;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.bson.BsonDocument;

public class ItemData {

    private int slot;
    private String itemId;
    private int quantity;
    private double durability;
    private double maxDurability;
    private String metadata;

    public ItemData(int slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemId = itemStack.getItemId();
        this.quantity = itemStack.getQuantity();
        this.durability = itemStack.getDurability();
        this.maxDurability = itemStack.getMaxDurability();
        BsonDocument metadata = itemStack.getMetadata();
        if (metadata != null) {
            this.metadata = metadata.toJson();
        }
    }

    public ItemStack toItemStack() {
        try {
            BsonDocument metaDoc = null;
            if (this.metadata != null && !this.metadata.isEmpty()) {
                metaDoc = BsonDocument.parse(this.metadata);
            }

            return new ItemStack(this.itemId, this.quantity, this.durability, this.maxDurability, metaDoc);
        } catch (Exception ex) {
            return null;
        }
    }

    public int getSlot() {
        return slot;
    }
}
