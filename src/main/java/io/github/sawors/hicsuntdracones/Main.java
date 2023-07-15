package io.github.sawors.hicsuntdracones;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Main extends JavaPlugin {
    
    static Plugin instance = null;
    static SLogger logger = null;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        logger = new SLogger(getPlugin());
        
        for(World w : Bukkit.getWorlds()){
            new WorldMapManager(w);
        }
        // COMMANDS
        Objects.requireNonNull(getServer().getPluginCommand("map")).setExecutor(new MapCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    public static SLogger logger() {
        return logger;
    }
    
    public static @NotNull Plugin getPlugin() {
        if(instance == null) throw new IllegalStateException("The plugin might not have initialized correctly!");
        return instance;
    }
}
