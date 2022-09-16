package com.ryanjhuston.Hooks;

import com.ryanjhuston.SkcraftBasics;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;

public class LuckPermsHook {

    private SkcraftBasics plugin;
    private LuckPerms luckPerms;

    public LuckPermsHook(SkcraftBasics plugin) {
        this.plugin = plugin;

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if(provider != null) {
            luckPerms = provider.getProvider();
        }
    }

    public void switchGroup(Player player) {
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
            InheritanceNode inheritanceNode = InheritanceNode.builder(plugin.getConfig().getString("Mod-Group")).build();
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            if (!inheritedGroups.stream().anyMatch(g -> g.getName().equals(plugin.getConfig().getString("Mod-Group"))) && plugin.getSkcraftPlayer(player).getInModMode()) {
                user.data().add(inheritanceNode);
            } else if (inheritedGroups.stream().anyMatch(g -> g.getName().equals(plugin.getConfig().getString("Mod-Group"))) && !plugin.getSkcraftPlayer(player).getInModMode()) {
                user.data().remove(inheritanceNode);
            }
        });
    }
}
