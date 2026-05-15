package de.ricosw.orbismod;

import com.google.gson.Gson;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.ricosw.orbismanager.api.OrbisManagerAPI;
import de.ricosw.orbismanager.api.model.IPlayerDataEntry;
import de.ricosw.orbismod.inventory.LockData;
import de.ricosw.orbismod.inventory.PlayerInventoryData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class OrbisInventorySync extends JavaPlugin {

    private Gson gson = new Gson();

    private List<UUID> blacklistedPlayer = new ArrayList<>();

    public OrbisInventorySync(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        if (OrbisManagerAPI.get() == null) {
            getLogger().at(Level.FINE).log("Failed to load OrbisManagerAPI");
            return;
        }

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        HytaleServer.get().getEventBus().register(
                EventPriority.FIRST,
                PlayerDisconnectEvent.class,
                this::onPlayerDisconnect
        );

        OrbisManagerAPI.get().addTag("inventory_sync");
    }

    private void onPlayerReady(PlayerReadyEvent event) {
        PlayerRef playerRef = resolvePlayerRef(event.getPlayerRef());
        if (playerRef == null) {
            return;
        }

        assert playerRef.getWorldUuid() != null;
        Objects.requireNonNull(Universe.get().getWorld(playerRef.getWorldUuid())).execute(() -> {
            Store<EntityStore> store = Objects.requireNonNull(playerRef.getReference()).getStore();

            Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
            if (player == null) return;
            CombinedItemContainer playerInventory = InventoryComponent.getCombined(Objects.requireNonNull(playerRef.getReference()).getStore(), playerRef.getReference(), InventoryComponent.EVERYTHING);
            //clearInventory(playerInventory);
            loadInventory(playerRef, player);
        });
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Objects.requireNonNull(event.getPlayerRef().getReference());

        Store<EntityStore> store = event.getPlayerRef().getReference().getStore();

        assert event.getPlayerRef().getWorldUuid() != null;
        Objects.requireNonNull(Universe.get().getWorld(event.getPlayerRef().getWorldUuid())).execute(() -> {
            Player player = store.getComponent(event.getPlayerRef().getReference(), Player.getComponentType());
            if (player == null) return;

            saveInventory(event.getPlayerRef().getUuid(), player);
        });
    }

    private void saveInventory(UUID playerUUID, Player player) {
        if (blacklistedPlayer.contains(playerUUID)) return;

        CombinedItemContainer playerInventory = InventoryComponent.getCombined(Objects.requireNonNull(player.getReference()).getStore(), player.getReference(), InventoryComponent.EVERYTHING);

        PlayerInventoryData data = PlayerInventoryData.fromInventory(playerInventory);
        String json = gson.toJson(data);
        OrbisManagerAPI.get().setPlayerData(playerUUID.toString(), "inventorysync.data", "json", json);
        unlockInventory(playerUUID);
    }

    private void loadInventory(PlayerRef playerRef, Player player) {
        IPlayerDataEntry lockPlayerData = OrbisManagerAPI.get().getPlayerData(playerRef.getUuid().toString(), "inventorysync.lock");
        CombinedItemContainer playerInventory = InventoryComponent.getCombined(Objects.requireNonNull(playerRef.getReference()).getStore(), playerRef.getReference(), InventoryComponent.EVERYTHING);
        if (lockPlayerData != null) {
            LockData lockData = gson.fromJson(lockPlayerData.asJson(), LockData.class);
            if (lockData != null) {
                if (!lockData.serverId.equals(OrbisManagerAPI.get().getServerId())) {
                    blacklistedPlayer.add(playerRef.getUuid());
                    playerRef.sendMessage(Message.raw("Inventory is locked by other server"));
                    clearInventory(playerInventory);
                    return;
                }
            }
        }
        blacklistedPlayer.remove(playerRef.getUuid());

        IPlayerDataEntry playerDataEntry = OrbisManagerAPI.get().getPlayerData(playerRef.getUuid().toString(), "inventorysync.data");
        if (playerDataEntry == null) {
            return;
        }
        PlayerInventoryData data = gson.fromJson(playerDataEntry.asJson(), PlayerInventoryData.class);
        if (data == null) {
            return;
        }

        clearInventory(playerInventory);

        playerRef.sendMessage(Message.raw("Loading inventory..."));

        data.toInventory(playerInventory);

        lockInventory(playerRef.getUuid());
    }

    private void clearInventory(CombinedItemContainer combinedEverything) {
        if (combinedEverything != null) {
            combinedEverything.clear();
        }
    }

    private static PlayerRef resolvePlayerRef(Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return ref.getStore().getComponent(ref, PlayerRef.getComponentType());
    }

    private void lockInventory(UUID playerUUID) {
        LockData lock = new LockData(OrbisManagerAPI.get().getServerId(), System.currentTimeMillis());
        OrbisManagerAPI.get().setPlayerData(playerUUID.toString(), "inventorysync.lock", "json", gson.toJson(lock));
    }

    private void unlockInventory(UUID playerUUID) {
        OrbisManagerAPI.get().deletePlayerData(playerUUID.toString(), "inventorysync.lock");
    }
}
