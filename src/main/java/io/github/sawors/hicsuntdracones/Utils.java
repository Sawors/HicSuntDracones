package io.github.sawors.hicsuntdracones;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.awt.*;

public class Utils {
    
    public static Component gradientText(String text, int fromColor, int toColor){
        String[] letters = text.split("");
        Color source = new Color(fromColor);
        Color target = new Color(toColor);
        Component output = Component.empty();
        int redStep = (target.getRed()-source.getRed())/letters.length;
        int greenStep = (target.getGreen()-source.getGreen())/letters.length;
        int blueStep = (target.getBlue()-source.getBlue())/letters.length;
        for(int i = 0; i<letters.length; i++){
            output = output.append(Component.text(letters[i]).color(TextColor.color(source.getRed()+(redStep*i),source.getGreen()+(greenStep*i),source.getBlue()+(blueStep*i))));
        }
        return output;
    }
    
    public static Component gradientText(String text, int... colors){
        if(colors.length == 1){
            return Component.text(text).color(TextColor.color(colors[0]));
        } else if(colors.length == 0){
            return Component.text(text).color(NamedTextColor.WHITE);
        }
        Component output = Component.empty();
        int step = (int) Math.ceil((double) text.length() /(colors.length-1));
        int i = 0;
        int colorIndex = 0;
        do{
            String sub = text.substring(i,Math.min(text.length(),i+step));
            output = output.append(gradientText(sub,colors[colorIndex],colors[colorIndex+1]));
            
            i+=step;
            colorIndex++;
        } while (i <= text.length() && colorIndex < colors.length-1);
        
        
        return output;
    }
}
