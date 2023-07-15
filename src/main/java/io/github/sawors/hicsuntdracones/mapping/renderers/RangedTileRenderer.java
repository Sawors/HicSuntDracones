package io.github.sawors.hicsuntdracones.mapping.renderers;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;

public abstract class RangedTileRenderer extends TileRenderer{
    
    protected WorldTile[] rangeSource;
    
    public RangedTileRenderer(World world) {
        super(world);
    }
    
    public void setRangeSource(WorldTile[] rangeSource){
        this.rangeSource = rangeSource;
    }
    
    public WorldTile[] getRangeSource() {
        return rangeSource;
    }
}
