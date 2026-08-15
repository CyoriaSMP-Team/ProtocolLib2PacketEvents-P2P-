package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class WrappedLevelChunkData {
    private WrappedLevelChunkData() { }
    public static class BlockEntityInfo extends AbstractWrapper {
        private int sectionX,sectionZ,y; private MinecraftKey typeKey; private NbtCompound additionalData;
        public BlockEntityInfo(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);}
        private BlockEntityInfo(int x,int z,int y,MinecraftKey key,NbtCompound data){super(BlockEntityInfo.class);sectionX=x;sectionZ=z;this.y=y;typeKey=key;additionalData=data;handle=this;}
        public int getSectionX(){return sectionX;} public void setSectionX(int v){sectionX=v;} public int getSectionZ(){return sectionZ;} public void setSectionZ(int v){sectionZ=v;} public int getY(){return y;} public void setY(int v){y=v;} public MinecraftKey getTypeKey(){return typeKey;} public void setTypeKey(MinecraftKey v){typeKey=v;} public NbtCompound getAdditionalData(){return additionalData;} public void setAdditionalData(NbtCompound v){additionalData=v;}
        public static BlockEntityInfo fromValues(int x,int z,int y,MinecraftKey key){return new BlockEntityInfo(x,z,y,key,null);} public static BlockEntityInfo fromValues(int x,int z,int y,MinecraftKey key,NbtCompound data){return new BlockEntityInfo(x,z,y,key,data);}
    }
    public static final class ChunkData extends AbstractWrapper {
        private NbtCompound heightmapsTag; private Map<EnumWrappers.HeightmapType,long[]> heightmaps=Collections.emptyMap(); private byte[] buffer=new byte[0]; private List<BlockEntityInfo> entities=new ArrayList<>();
        public ChunkData(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);} private ChunkData(NbtCompound tag,byte[] buffer,List<BlockEntityInfo> entities){super(ChunkData.class);heightmapsTag=tag;this.buffer=buffer==null?new byte[0]:buffer.clone();if(entities!=null)this.entities.addAll(entities);handle=this;}
        public NbtCompound getHeightmapsTag(){return heightmapsTag;} public void setHeightmapsTag(NbtCompound v){heightmapsTag=v;} public Map<EnumWrappers.HeightmapType,long[]> getHeightmaps(){return heightmaps;} public void setHeightmaps(Map<EnumWrappers.HeightmapType,long[]> v){heightmaps=v==null?Collections.emptyMap():v;} public byte[] getBuffer(){return buffer.clone();} public void setBuffer(byte[] v){buffer=v==null?new byte[0]:v.clone();} public List<BlockEntityInfo> getBlockEntityInfo(){return Collections.unmodifiableList(entities);} public void setBlockEntityInfo(List<BlockEntityInfo> v){entities=new ArrayList<>();if(v!=null)entities.addAll(v);}
        public static ChunkData fromValues(NbtCompound tag,byte[] buffer,List<BlockEntityInfo> entities){return new ChunkData(tag,buffer,entities);} public static ChunkData fromValues(Map<EnumWrappers.HeightmapType,long[]> heights,byte[] buffer,List<BlockEntityInfo> entities){ChunkData result=new ChunkData(null,buffer,entities);result.heightmaps=heights==null?Collections.emptyMap():heights;return result;}
    }
    public static class LightData extends AbstractWrapper {
        private BitSet sky=new BitSet(),block=new BitSet(),emptySky=new BitSet(),emptyBlock=new BitSet(); private List<byte[]> skyUpdates=new ArrayList<>(),blockUpdates=new ArrayList<>(); private boolean trustEdges;
        public LightData(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);} private LightData(BitSet a,BitSet b,BitSet c,BitSet d,List<byte[]> e,List<byte[]> f,boolean trust){super(LightData.class);sky=(BitSet)a.clone();block=(BitSet)b.clone();emptySky=(BitSet)c.clone();emptyBlock=(BitSet)d.clone();if(e!=null)skyUpdates.addAll(e);if(f!=null)blockUpdates.addAll(f);trustEdges=trust;handle=this;}
        public BitSet getSkyYMask(){return (BitSet)sky.clone();} public void setSkyYMask(BitSet v){sky=v==null?new BitSet():(BitSet)v.clone();} public BitSet getBlockYMask(){return (BitSet)block.clone();} public void setBlockYMask(BitSet v){block=v==null?new BitSet():(BitSet)v.clone();} public BitSet getEmptySkyYMask(){return (BitSet)emptySky.clone();} public void setEmptySkyYMask(BitSet v){emptySky=v==null?new BitSet():(BitSet)v.clone();} public BitSet getEmptyBlockYMask(){return (BitSet)emptyBlock.clone();} public void setEmptyBlockYMask(BitSet v){emptyBlock=v==null?new BitSet():(BitSet)v.clone();} public List<byte[]> getSkyUpdates(){return Collections.unmodifiableList(skyUpdates);} public List<byte[]> getBlockUpdates(){return Collections.unmodifiableList(blockUpdates);} public boolean isTrustEdges(){return trustEdges;} public void setTrustEdges(boolean v){trustEdges=v;}
        public static LightData fromValues(BitSet a,BitSet b,BitSet c,BitSet d,List<byte[]> e,List<byte[]> f,boolean trust){return new LightData(a,b,c,d,e,f,trust);} public static LightData fromValues(BitSet a,BitSet b,BitSet c,BitSet d,List<byte[]> e,List<byte[]> f){return fromValues(a,b,c,d,e,f,false);}
    }
}
