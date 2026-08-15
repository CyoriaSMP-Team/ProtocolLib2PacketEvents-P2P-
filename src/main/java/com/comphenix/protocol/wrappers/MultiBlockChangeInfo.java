package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import org.bukkit.Location;
import org.bukkit.World;

public class MultiBlockChangeInfo {
    private int x,y,z; private WrappedBlockData data; private ChunkCoordIntPair chunk;
    public MultiBlockChangeInfo(short coordinate,WrappedBlockData data,ChunkCoordIntPair chunk){this.x=(coordinate>>12)&15;this.z=(coordinate>>8)&15;this.y=coordinate&255;this.data=data;this.chunk=chunk;}
    public MultiBlockChangeInfo(Location location,WrappedBlockData data){setLocation(location);this.data=data;}
    public Location getLocation(World world){return new Location(world,getAbsoluteX(),y,getAbsoluteZ());}
    public void setLocation(Location location){if(location==null)throw new IllegalArgumentException("location cannot be null");setLocation(location.getBlockX(),location.getBlockY(),location.getBlockZ());}
    public void setLocation(int x,int y,int z){this.x=x&15;this.y=y;this.z=z&15;this.chunk=new ChunkCoordIntPair(x>>4,z>>4);}
    public int getX(){return x;} public int getAbsoluteX(){return chunk==null?x:(chunk.getChunkX()<<4)+x;} public void setX(int v){x=v&15;}
    public int getY(){return y;} public void setY(int v){y=v;}
    public int getZ(){return z;} public int getAbsoluteZ(){return chunk==null?z:(chunk.getChunkZ()<<4)+z;} public void setZ(int v){z=v&15;}
    public WrappedBlockData getData(){return data;} public void setData(WrappedBlockData v){data=v;} public ChunkCoordIntPair getChunk(){return chunk;}
    public static EquivalentConverter<MultiBlockChangeInfo> getConverter(ChunkCoordIntPair chunk){return new EquivalentConverter<>(){public MultiBlockChangeInfo getSpecific(Object g){return g instanceof MultiBlockChangeInfo v?v:null;} public Object getGeneric(MultiBlockChangeInfo s){return s;} public Class<MultiBlockChangeInfo> getSpecificType(){return MultiBlockChangeInfo.class;} public Class<?> getGenericType(){return MultiBlockChangeInfo.class;}};}
}
