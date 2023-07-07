package io.github.sawors.hicsuntdracones;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    
    static Plugin instance = null;

    @Override
    public void onEnable() {
        instance = this;
        
        
        // COMMANDS
        Objects.requireNonNull(getServer().getPluginCommand("map")).setExecutor(new Main());
        logAdmin("Hello !");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    public static @NotNull Plugin getPlugin() {
        if(instance == null) throw new IllegalStateException("The plugin might not have initialized correctly!");
        return instance;
    }
    
    public static void logAdmin(Object object){
        Logger logger = Bukkit.getLogger();
        
        String pluginname = getPlugin().getName();
        
        String output = "["+pluginname+" Log"+"-"+ Time.valueOf(LocalTime.now()) + "] "+ Arrays.toString(Thread.currentThread().getStackTrace()) +" : "+object;
        Bukkit.getLogger().log(Level.INFO, output);
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.isOp()){
                p.sendMessage(Component.text(output));
            }
        }
    }
}
