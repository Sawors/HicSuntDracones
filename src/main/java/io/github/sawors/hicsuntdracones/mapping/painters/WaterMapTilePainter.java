package io.github.sawors.hicsuntdracones.mapping.painters;

import com.destroystokyo.paper.MaterialSetTag;
import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class WaterMapTilePainter extends TilePainter{
    
    public WaterMapTilePainter(World world) {
        super(world);
    }
    
    @Override
    public @NotNull Color renderTile(WorldTile tile) {
        Material material = tile.material();
        Color color = new Color(0x000000);
        if(MaterialSetTag.LEAVES.isTagged(material)) {
            color = new Color(0x175A26);
        } else {
            switch (material){
                case WATER -> color = new Color(0x165197);
                case STONE -> color = new Color(0x7D7D7D);
                case SNOW, SNOW_BLOCK, POWDER_SNOW -> color = new Color(0xffffff);
                case SAND -> color = new Color(0xF8E98E);
                case GRAVEL -> color = new Color(0xA2A2A2);
                case LAVA -> color = new Color(0xEF7005);
                default -> color = new Color(0x388310);
            }
        }
        
        int minY = 63;
        int maxY = 256;
        int tileY = tile.maxY();
        
        double heightFactor = (((double) (Math.min(Math.max((tileY),minY),maxY)-minY)/(maxY-minY))*0.75)+0.25;
        
        int r = (int) (color.getRed()*heightFactor);
        int g = (int) (color.getGreen()*heightFactor);
        int b = (int) (color.getBlue()*heightFactor);
        
        return new Color(r,g,b);
    }
}
