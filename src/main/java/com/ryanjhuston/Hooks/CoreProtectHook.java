package com.ryanjhuston.Hooks;

import com.ryanjhuston.SkcraftBasics;
import com.ryanjhuston.Types.SkcraftPlayer;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.event.CoreProtectPreLogEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class CoreProtectHook implements Listener {

    private SkcraftBasics plugin;

    public CoreProtectHook(SkcraftBasics plugin) {
        this.plugin = plugin;

        Plugin coreProtect = plugin.getServer().getPluginManager().getPlugin("CoreProtect");

        if(plugin != null && coreProtect instanceof CoreProtect) {
            CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();
            if(coreProtectAPI.isEnabled()) {
                if(coreProtectAPI.APIVersion() > 6) {
                    plugin.pm.registerEvents(this, plugin);
                }
            }
        }
    }

    @EventHandler
    public void onActionLog(CoreProtectPreLogEvent event) {
        Player player = Bukkit.getPlayer(event.getUser());

        if(player == null) {
            return;
        }

        SkcraftPlayer skcraftPlayer = plugin.getSkcraftPlayer(player);

        if(skcraftPlayer.getInModMode()) {
            event.setUser(event.getUser() + "-mod");
        }
    }
}