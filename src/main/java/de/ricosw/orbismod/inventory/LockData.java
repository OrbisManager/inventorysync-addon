package de.ricosw.orbismod.inventory;

public class LockData {

    public LockData() {

    }

    public LockData(String serverId, long lockedAt) {
        this.serverId = serverId;
        this.lockedAt = lockedAt;
    }

    public long lockedAt;
    public String serverId;

}
