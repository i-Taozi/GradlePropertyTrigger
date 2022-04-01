package buildcraft.core.tablet.manager;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import buildcraft.core.tablet.TabletServer;

public enum TabletManagerServer {
    INSTANCE;

    private HashMap<EntityPlayer, TabletThread> threads = new HashMap<>();

    public TabletServer get(EntityPlayer player) {
        if (!threads.containsKey(player)) {
            TabletServer tablet = new TabletServer(player);
            TabletThread thread = new TabletThread(tablet);
            threads.put(player, thread);
            new Thread(thread, "BuildCraft Tablet Manager").start();
        }
        return (TabletServer) threads.get(player).getTablet();
    }

    public void onServerStopping() {
        for (TabletThread thread : threads.values()) {
            thread.stop();
        }
        threads.clear();
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        for (TabletThread thread : threads.values()) {
            thread.tick(0.05F);
        }
    }

    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TabletThread thread = threads.get(event.player);
        if (thread != null) {
            thread.stop();
            threads.remove(event.player);
        }
    }
}
