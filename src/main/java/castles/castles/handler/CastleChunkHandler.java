package castles.castles.handler;

import castles.castles.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class CastleChunkHandler implements Listener {
    @EventHandler
    public void ChunkUnloadEvent(ChunkUnloadEvent event) {
        if (event.getChunk().getPersistentDataContainer().has(Utils.castlesKey)) {
            event.getChunk().load();
        }
    }
}
