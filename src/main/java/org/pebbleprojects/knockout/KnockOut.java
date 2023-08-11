package org.pebbleprojects.knockout;

import org.bukkit.plugin.java.JavaPlugin;
import org.pebbleprojects.knockout.handlers.Handler;

public final class KnockOut extends JavaPlugin {

    public static KnockOut INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        new Thread(Handler::new).start();
    }

    @Override
    public void onDisable() {
        Handler.INSTANCE.shutdown();

        getServer().getScheduler().cancelTasks(this);
    }
}
