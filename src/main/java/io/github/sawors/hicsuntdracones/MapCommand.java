package io.github.sawors.hicsuntdracones;

import io.github.sawors.hicsuntdracones.mapping.WorldRegion;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static io.github.sawors.hicsuntdracones.Main.logAdmin;

public class MapCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        
        if(commandSender instanceof Player p){
            
            if(strings.length >= 1){
                switch(strings[0]){
                    case "chunks" -> {
                        logAdmin(p.getWorld().getLoadedChunks().length);
                    }
                    case "region" -> {
                        logAdmin(WorldRegion.getRegionForCoordinates(p.getLocation().x(),p.getLocation().z()));
                    }
                    case "test" -> {
                        logAdmin(Runtime.getRuntime().availableProcessors());
                    }
                    case "spec" -> {
                        p.setFlying(true);
                        p.teleport(new Location(p.getWorld(),32,207,32,-90,90));
                        p.setFlying(true);
                    }
                    case "save" -> {
                        World w = p.getWorld();
                        File regionDirectory = new File(w.getWorldFolder().getPath()+File.separator+"region");
                        
                        
                        if(regionDirectory.isDirectory()) {
                            File[] regions = regionDirectory.listFiles(c -> c.getName().endsWith(".mca"));
                            if (regions == null || regions.length == 0) {
                                logAdmin("There is no region generated in world " + w.getName());
                                return true;
                            }
                            
                            List<String> regs = Arrays.stream(regions).map(f -> f.getName().replace("r.", "").replace(".mca", "")).toList();
                            
                            // worker threads
                            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
                            
                            for(String part : regs) {
                                
                                //ArrayBlockingQueue<ChunkSnapshot> regionData = new SynchronousQueue<>();
                                
                                
                                int breakIndex = part.indexOf(".");
                                
                                int regionX = Integer.parseInt(part.substring(0, breakIndex));
                                int regionZ = Integer.parseInt(part.substring(breakIndex + 1));
                                logAdmin("region " + regionX + ", " + regionZ);
                                
                                long startTime = System.currentTimeMillis();
                                
                                Queue<ChunkSnapshot> fetchedChunks = new LinkedList<>();
                                int chunkAmount = 32*32;
                                List<ChunkSnapshot> chunkData = Collections.synchronizedList(new ArrayList<>(chunkAmount));
                                
                                for(int chunkRelativeX = 0; chunkRelativeX < 32; chunkRelativeX++){
                                    for(int chunkRelativeY = 0; chunkRelativeY < 32; chunkRelativeY++){
                                        
                                        // for every chunk of the region
                                        
                                        int chunkX = chunkRelativeX+(regionX*32);
                                        int chunkY = chunkRelativeY+(regionZ*32);
                                        
                                        w.getChunkAtAsync(chunkX, chunkY, false, c -> {
                                            chunkData.add(
                                                    c == null ?
                                                            null :
                                                            c.getChunkSnapshot(true,true,false)
                                            );
                                            
                                            if(chunkData.size() == chunkAmount){
                                                // all the chunks have now been added to the list !
                                                WorldRegion region = new WorldRegion(regionX,regionZ,chunkData.toArray(new ChunkSnapshot[32*32]),p.getWorld());
                                                MapRegionManager.getInstance(w).saveData(region);
                                                
                                                logAdmin("Region mapping finished and saved in "+(System.currentTimeMillis()-startTime)+"ms!");
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                    
                    case "render" -> {
                        File regionDirectory = new File(Main.getPlugin().getDataFolder()+File.separator+"regions"+File.separator+p.getWorld().getName());
                        List<String> regions = Arrays.stream(Objects.requireNonNull(regionDirectory.listFiles(f -> f.getName().endsWith(".yml.png")))).map(f -> f.getName().replace("r.","").replace(".yml.png","")).toList();
                        logAdmin(regions);
                        int maxX = regions.stream().max(Comparator.comparingInt(name -> Integer.parseInt(name.substring(0,name.indexOf("."))))).map(f -> Integer.parseInt(f.substring(0,f.indexOf(".")))).orElse(0);
                        int minX = regions.stream().min(Comparator.comparingInt(name -> Integer.parseInt(name.substring(0,name.indexOf("."))))).map(f -> Integer.parseInt(f.substring(0,f.indexOf(".")))).orElse(0);
                        int maxZ = regions.stream().max(Comparator.comparingInt(name -> Integer.parseInt(name.substring(name.indexOf(".")+1)))).map(f -> Integer.parseInt(f.substring(f.indexOf(".")+1))).orElse(0);
                        int minZ = regions.stream().min(Comparator.comparingInt(name -> Integer.parseInt(name.substring(name.indexOf(".")+1)))).map(f -> Integer.parseInt(f.substring(f.indexOf(".")+1))).orElse(0);
                        
                        logAdmin(maxX);
                        logAdmin(minX);
                        logAdmin(maxZ);
                        logAdmin(minZ);
                        
                        BufferedImage fullRender = new BufferedImage((maxX-minX)*64,(maxZ-minZ)*64,BufferedImage.TYPE_INT_RGB);
                        Graphics2D graph = fullRender.createGraphics();
                        for(String regionName : regions){
                            File regionRender = new File(regionDirectory.getPath()+File.separator+"r."+regionName+".yml.png");
                            int x = Integer.parseInt(regionName.substring(0,regionName.indexOf(".")))*64;
                            int z = Integer.parseInt(regionName.substring(regionName.indexOf(".")+1))*64;
                            try{
                                BufferedImage regionImage = ImageIO.read(regionRender);
                                graph.drawImage(regionImage,x-(minX*64),z-(minZ*64),64,64,null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        
                        try{
                            ImageIO.write(fullRender,"png",new File(regionDirectory.getPath()+File.separator+"_full-render.png"));
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        
                        graph.dispose();
                        
                    }
                }
                return true;
            }
            
            
            
            World w = p.getWorld();
            //WorldMapper mapper = new WorldMapper(w);
            
            File regionDirectory = new File(w.getWorldFolder().getPath()+File.separator+"region");
            if(regionDirectory.isDirectory()) {
                File[] regions = regionDirectory.listFiles(c -> c.getName().endsWith(".mca"));
                if(regions == null || regions.length == 0) {
                    logAdmin("There is no region generated in world "+w.getName());
                    return true;
                }
                
                List<String> regs =  Arrays.stream(regions).map(f -> f.getName().replace("r.","").replace(".mca","")).toList();
                
                Set<Chunk> generated = new HashSet<>(regions.length*32*32);
                logAdmin(regions.length*32*32);
                
               
                
                int seaLevel = w.getSeaLevel();
                
                // for every region
                for(String part : regs){
                    
                    //ArrayBlockingQueue<ChunkSnapshot> regionData = new SynchronousQueue<>();
                    
                    
                    
                    int breakIndex = part.indexOf(".");
                    
                    int regionX = Integer.parseInt(part.substring(0,breakIndex));
                    int regionY = Integer.parseInt(part.substring(breakIndex+1));
                    logAdmin("region "+regionX+", "+regionY);
                    int chunkAmount = 0;
                    long startTime = System.currentTimeMillis();
                    
                    for(int chunkRelativeX = 0; chunkRelativeX < 32; chunkRelativeX++){
                        for(int chunkRelativeY = 0; chunkRelativeY < 32; chunkRelativeY++){
                            
                            // for every chunk of the region
                            
                            int chunkX = chunkRelativeX+(regionX*32);
                            int chunkY = chunkRelativeY+(regionY*32);
                            
                            int finalChunkAmount = chunkAmount;
                            int finalChunkRelativeX = chunkRelativeX;
                            int finalChunkRelativeY = chunkRelativeY;
                            w.getChunkAtAsync(chunkX, chunkY, false, c -> {
                                if(c != null){
                                    final Biome biome = c.getChunkSnapshot(true,true,false).getBiome(8,seaLevel,8);
                                    
                                    /*if(biome.equals(Biome.RIVER)){
                                        b.setType(Material.BLUE_WOOL);
                                    } else if(biome.equals(Biome.FOREST)){
                                        b.setType(Material.GREEN_WOOL);
                                    }  else if(biome.equals(Biome.PLAINS)){
                                        b.setType(Material.LIME_WOOL);
                                    } else {
                                        b.setType(Material.LIGHT_GRAY_WOOL);
                                    }*/
                                    if(!c.isEntitiesLoaded()){
                                        c.unload(false);
                                    }
                                    
                                }
                                
                                if(finalChunkRelativeX == 31 && finalChunkRelativeY == 31){
                                    int delta = (int) (System.currentTimeMillis()-startTime);
                                    Bukkit.getLogger().log(Level.INFO,"genChunk"+": "+delta+"ms");
                                }
                            });
                            
                            
                            
                        }
                    }
                }
                
                
                //generated.removeIf(Objects::isNull);
                logAdmin(generated.size(),true);
                
                if(true){
                    return true;
                }
                
                
                
                int maxX = generated.stream().max(Comparator.comparingInt(Chunk::getX)).orElse(w.getChunkAt(0,0)).getX();
                int minX = generated.stream().min(Comparator.comparingInt(Chunk::getX)).orElse(w.getChunkAt(0,0)).getX();
                int width = maxX-minX;
                int maxZ = generated.stream().max(Comparator.comparingInt(Chunk::getZ)).orElse(w.getChunkAt(0,0)).getZ();
                int minZ = generated.stream().min(Comparator.comparingInt(Chunk::getZ)).orElse(w.getChunkAt(0,0)).getZ();
                int height = maxZ-minZ;
                logAdmin(width);
                logAdmin(height);
                
                // ie sampling 1 block for every n blocks
                int precision = 4;
                if(strings.length >= 1){
                    try{
                        precision = Integer.parseInt(strings[0]);
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
                precision = Math.min(Math.max(precision,1),16);
                int step = 16/precision;
                
                Integer[][] biomeMap = new Integer[(width+1)*step][(height+1)*step];
                
                final Map<Material,Integer> colorCache = new HashMap<>();
                
                final int finalPrecision = precision;
                
                logAdmin(generated.size());
                logAdmin(generated.size()*16*16);
                
                if(true){
                    return true;
                }
                
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for(Chunk c : generated){
                            for(int x = 0; x<16; x+= finalPrecision){
                                for(int z = 0; z<16; z+= finalPrecision){
                                    int absX = (c.getX()*16)+x;
                                    int absZ = (c.getZ()*16)+z;
                                    int relX = (absX+Math.abs(minX*16))/ finalPrecision;
                                    int relZ = (absZ+Math.abs(minZ*16))/ finalPrecision;
                                    final Block[] site = {null};
                                    final Material[] matGet = {null};
                                    new BukkitRunnable(){
                                        @Override
                                        public void run() {
                                            site[0] = c.getWorld().getHighestBlockAt(absX,absZ);
                                            matGet[0] = site[0].getType();
                                        }
                                    }.runTask(Main.getPlugin());
                                    
                                    if(site[0] == null || matGet[0] == null){
                                        logAdmin("Could not get block");
                                        return;
                                    }
                                    
                                    int color = 0x000000;
                                    
                                    final Material mat = matGet[0];
                                    
                                    String textureName = mat.toString().toLowerCase(Locale.ROOT);
                                    if(textureName.contains("lava")){
                                        textureName+="_still";
                                    } else if(mat.equals(Material.SNOW_BLOCK)){
                                        textureName="snow";
                                    } else if(textureName.contains("grass")){
                                        color = 0x99a14a;
                                        colorCache.put(mat,color);
                                    } else if(textureName.contains("water")){
                                        color = 0x34518b;
                                        colorCache.put(mat,color);
                                    } else if(textureName.contains("leaves")){
                                        color = 0x175a26;
                                        colorCache.put(mat,color);
                                    }
                                    File texture = new File("textures"+File.separator+"block"+File.separator+textureName+".png");
                                    if(!colorCache.containsKey(mat) && texture.exists()){
                                        logAdmin(texture.getPath());
                                        try{
                                            BufferedImage base = ImageIO.read(texture);
                                            int tWidth = base.getWidth();
                                            int tHeight = base.getHeight();
                                            
                                            
                                            
                                            
                                            int sampleRate = 4;
                                            int sampleX = Math.floorDiv(tWidth, sampleRate);
                                            int sampleY = Math.floorDiv(tHeight, sampleRate);
                                            float totalSamples = sampleX*sampleY;
                                            
                                            float r = 0;
                                            float g = 0;
                                            float b = 0;
                                            
                                            for(int ix = 0; ix<sampleX; ix++){
                                                for(int iy = 0; iy<sampleY; iy++){
                                                    Color sampleColor = new Color(base.getRGB(ix*sampleRate,iy*sampleRate));
                                                    r += sampleColor.getRed()/totalSamples;
                                                    g += sampleColor.getGreen()/totalSamples;
                                                    b += sampleColor.getBlue()/totalSamples;
                                                    
                                                }
                                            }
                                            
                                            color = new Color((int)r,(int)g,(int)b).getRGB();
                                            
                                            colorCache.put(mat,color);
                                        } catch (IOException e){
                                            e.printStackTrace();
                                        }
                                    } else {
                                        color = colorCache.getOrDefault(mat,0x404040);
                                    }
                        
                        /*if(m.contains("river")){
                            color = 0x0094ff;
                        } else if(m.contains("plains")){
                            color = 0x175a26;
                        } else {
                            color = 0x7d7d7d;
                        }*/
                                    
                                    biomeMap[relX][relZ] = color;
                                }
                            }
                        }
                        
                        BufferedImage mapImage = new BufferedImage(width*step,height*step,BufferedImage.TYPE_INT_RGB);
                        for(int x = 0; x < width*step; x++){
                            for(int y = 0; y < height*step; y++){
                                Integer color = biomeMap[x][y];
                                mapImage.setRGB(x,y,color != null ? color : 0x000000);
                            }
                        }
                        File out = new File("biomeMap_"+ finalPrecision +".png");
                        try {
                            out.createNewFile();
                            ImageIO.write(mapImage,"png",out);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        
                        logAdmin("rendering done !");
                    }
                }.runTaskAsynchronously(Main.getPlugin());
                return true;
            } else {
                logAdmin("There is no region generated in world "+w.getName());
            }
        }
        return false;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
