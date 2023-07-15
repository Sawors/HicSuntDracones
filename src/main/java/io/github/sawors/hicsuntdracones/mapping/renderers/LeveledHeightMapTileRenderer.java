package io.github.sawors.hicsuntdracones.mapping.renderers;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LeveledHeightMapTileRenderer extends HeightMapTileRenderer{
    
    int step;
    int offset = 0;
    
    public LeveledHeightMapTileRenderer(World world) {
        super(world);
        this.step = 4;
    }
    
    public LeveledHeightMapTileRenderer(World world, int step) {
        super(world);
        this.step = step;
    }
    
    @Override
    public @NotNull Color renderTile(WorldTile tile) {
        float rangedValue = Math.min(Math.max(tile.maxY(),minHeight),maxHeight);
        float roundedValue = (rangedValue-(rangedValue%step));
        return Color.getHSBColor(
                // ( O_O')
                (1-((roundedValue-minHeight+offset)/(maxHeight-minHeight)))*(240/360f),
                1,
                1
        );
    }
    
    @Override
    public void setMinHeight(float minHeight) {
        super.setMinHeight(minHeight);
        offset = (int) (this.minHeight-(this.minHeight-(this.minHeight%step)));
    }
}
