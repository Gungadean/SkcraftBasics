package com.ryanjhuston.Hooks;

import com.ryanjhuston.SkcraftBasics;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook {

    private SkcraftBasics plugin;
    private LuckPerms luckPerms;

    public LuckPermsHook(SkcraftBasics plugin) {
        this.plugin = plugin;

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        luckPerms = provider.getProvider();
    }

    public void switchGroup(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user.getPrimaryGroup().equals(plugin.getConfig().getString("Default-Group"))) {
            user.data().add(InheritanceNode.builder(plugin.getConfig().getString("Mod-Group")).build());
        } else if (user.getPrimaryGroup().equals(plugin.getConfig().getString("Mod-Group"))) {
            user.data().remove(InheritanceNode.builder(plugin.getConfig().getString("Default-Group")).build());
        }
    }
}
