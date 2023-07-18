package io.github.sawors.hicsuntdracones.mapping.painters;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;

public class HeightMapTilePainter extends RangedTilePainter {
    
    float maxHeight;
    float minHeight;
    
    @Override
    public @NotNull Color renderTile(WorldTile tile) {
        return Color.getHSBColor(
                // ( O_O')
                // 240
                0,
                0,
                //(1-((Math.min(Math.max(tile.maxY(),minHeight),maxHeight)-minHeight)/(maxHeight-minHeight)))*(240/360f)
                (Math.min(Math.max(tile.maxY(),minHeight),maxHeight)-minHeight)/(maxHeight-minHeight)
        );
    }
    
    public HeightMapTilePainter(World world) {
        super(world);
        setMaxHeight(world.getMaxHeight());
        setMinHeight(world.getMinHeight());
    }
    
    @Override
    public void setRangeSource(WorldTile[] rangeSource) {
        super.setRangeSource(rangeSource);
        setMaxHeight(Arrays.stream(rangeSource).mapToInt(WorldTile::maxY).max().orElse(0));
        setMinHeight(Arrays.stream(rangeSource).mapToInt(WorldTile::maxY).min().orElse(0));
    }
    
    public float getMaxHeight() {
        return maxHeight;
    }
    
    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }
    
    public float getMinHeight() {
        return minHeight;
    }
    
    public void setMinHeight(float minHeight) {
        this.minHeight = paintUnderSeaLevel ? minHeight : Math.max(minHeight, this.world.getSeaLevel());
    }
}
