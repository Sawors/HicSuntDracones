package io.github.sawors.hicsuntdracones;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class SLogger {
    
    private final int logColorLeft;
    private final int logColorRight;
    private final Plugin plugin;
    
    public SLogger(Plugin plugin){
        this(plugin,0xff9d00,0xe631e1);
    }
    
    public SLogger(Plugin plugin, int leftColor, int rightColor) {
        this.plugin = plugin;
        this.logColorLeft = leftColor;
        this.logColorRight = rightColor;
    }
    
    /**
     *
     * @param leftColor The new left color. Set to null to keep the old color.
     * @param rightColor The new right color. Set to null to keep the old color.
     * @return A new {@link SLogger} with the new colors.
     */
    public SLogger withColors(@Nullable Integer leftColor, @Nullable Integer rightColor){
        return new SLogger(this.plugin,leftColor != null ? leftColor : this.logColorLeft, rightColor != null ? rightColor : this.logColorRight);
    }
    
    /**
     * Warning : Please note that <b>this method is not designed for fast logging</b> as it has to fetch the stacktrace, which can impact performances. <b>To do quick successive logging with good performances please use logAdmin(object, true)</b> as it will disable all the slow features
     * @param object The object to print. This method will print the result of object.toString().
     */
    public void logAdmin(Object object){
        logAdmin(object,true);
    }
    
    /**
     *
     * @param object The object to print. This method will print the result of object.toString().
     * @param simplified Whether to use the simplified printing mode or not. <b>The simplified mode is much faster than the default one (up to 6 times faster)</b>
     */
    public void logAdmin(Object object, boolean simplified){
        logAdmin(object,true,false,false);
    }
    
    /**
     *
     * @param object The object to print. This method will print the result of object.toString().
     * @param sendPlayer Should online operators receive the message.
     * @param includeLine Should the console log contain the class and the line number where this method has been used. <i>(high performance impact)</i>
     * @param color Should the player output (if sendPlayer = true) be colored with a fancy gradient. <i>(low performance impact)</i>
     */
    public void logAdmin(Object object, boolean sendPlayer, boolean includeLine, boolean color) {
        
        String objectString = object != null ?object.toString() : "⚠ null ⚠";
        if(!Objects.isNull(object)){
            if(object instanceof Map<?,?> map){
                StringBuilder builder = new StringBuilder();
                
            } else if(object instanceof Set<?> set){
                StringBuilder builder = new StringBuilder(set.getClass().getSimpleName()+": [");
                for(Object o : set){
                    builder.append("\n -").append(o.toString());
                }
            }
        }
        
        // TODO : proper integration of this
        String pluginname = plugin.getName();
        
        String anchor;
        
        if(includeLine){
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stack[2];
            if(caller.getMethodName().equals("logAdmin")) caller = stack[3];
            anchor = "["+pluginname+" @"+caller.getClassName().substring(caller.getClassName().lastIndexOf(".")+1)+"."+caller.getMethodName()+"("+caller.getFileName()+":"+caller.getLineNumber()+")]";
        } else {
            anchor = "["+pluginname+"]";
        }
        
        Bukkit.getLogger().log(Level.INFO, anchor+" : "+objectString);
        
        if(sendPlayer){
            String playerAnchor = "["+pluginname+" - "+ Time.valueOf(LocalTime.now())+"]";
            Component coloredOutput = color ? Utils.gradientText(playerAnchor+" :",
                    logColorLeft,
                    logColorRight
            ).append(Component.text(" "+objectString).color(NamedTextColor.GRAY)) : Component.text(playerAnchor+" : "+objectString).color(NamedTextColor.YELLOW);
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.isOp()){
                    p.sendMessage(coloredOutput);
                }
            }
        }
    }
    
    private String getFormattedString(Object object){
        StringBuilder builder = new StringBuilder();
        
        return builder.toString();
    }
}
