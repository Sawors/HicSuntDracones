package io.github.sawors.hicsuntdracones;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Objects;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    
    static Plugin instance = null;
    final private static int logColorLeft = 0xff9d00;
    final private static int logColorRight = 0xe631e1;
    
    static MapRegionManager regionManager = null;
    

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        regionManager = new MapRegionManager();
        // COMMANDS
        Objects.requireNonNull(getServer().getPluginCommand("map")).setExecutor(new MapCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    public static @NotNull Plugin getPlugin() {
        if(instance == null) throw new IllegalStateException("The plugin might not have initialized correctly!");
        return instance;
    }
    
    
    
    /**
     * Warning : Please note that this method is not designed for fast logging as it has to fetch the stacktrace, which can impact performances. To do quick successive logging with good performances please use logAdmin(object, true) as it will disable all the slow features
     * @param object The object to print. This method will print the result of object.toString().
     */
    public static void logAdmin(Object object){
        logAdmin(object,false);
    }
    
    /**
     *
     * @param object The object to print. This method will print the result of object.toString().
     * @param simplified Whether to use the simplified printing mode or not. The simplified mode is much faster than the default one (up to 6 times faster)
     */
    public static void logAdmin(Object object, boolean simplified){
        
        String pluginname = getPlugin().getName();
        
        String anchor;
        if(simplified){
            anchor = "["+pluginname+"]";
        } else {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stack[2];
            if(caller.getMethodName().equals("logAdmin")) caller = stack[3];
            anchor = "["+pluginname+" @"+caller.getClassName().substring(caller.getClassName().lastIndexOf(".")+1)+"."+caller.getMethodName()+"("+caller.getFileName()+":"+caller.getLineNumber()+")]";
        }
        
        Bukkit.getLogger().log(Level.INFO, anchor+" : "+object);
        
        if(!simplified){
            String playerAnchor = "["+pluginname+" - "+Time.valueOf(LocalTime.now())+"]";
            Component coloredOutput = Utils.gradientText(playerAnchor+" :",
                            logColorLeft,
                            logColorRight
                    ).append(Component.text(" "+object).color(NamedTextColor.GRAY));
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.isOp()){
                    p.sendMessage(coloredOutput);
                }
            }
        }
    }
}
