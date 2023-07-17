package io.github.sawors.hicsuntdracones.mapping.painters;

import io.github.sawors.hicsuntdracones.mapping.WorldTile;
import org.bukkit.World;

public abstract class RangedTilePainter extends TilePainter {
    
    protected WorldTile[] rangeSource;
    
    public RangedTilePainter(World world) {
        super(world);
    }
    
    public void setRangeSource(WorldTile[] rangeSource){
        this.rangeSource = rangeSource;
    }
    
    public WorldTile[] getRangeSource() {
        return rangeSource;
    }
}
