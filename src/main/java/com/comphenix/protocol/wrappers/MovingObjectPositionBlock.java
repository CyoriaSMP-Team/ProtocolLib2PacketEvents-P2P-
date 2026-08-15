package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import org.bukkit.util.Vector;

public class MovingObjectPositionBlock implements Cloneable {
    private BlockPosition blockPosition=BlockPosition.ORIGIN; private Vector position=new Vector(); private EnumWrappers.Direction direction=EnumWrappers.Direction.UP; private boolean insideBlock;
    public MovingObjectPositionBlock() { }
    public MovingObjectPositionBlock(BlockPosition blockPosition,Vector position,EnumWrappers.Direction direction,boolean insideBlock){this.blockPosition=blockPosition;this.position=position;this.direction=direction;this.insideBlock=insideBlock;}
    public static Class<?> getNmsClass(){return MovingObjectPositionBlock.class;}
    public BlockPosition getBlockPosition(){return blockPosition;} public void setBlockPosition(BlockPosition v){blockPosition=v;}
    public Vector getPosVector(){return position;} public void setPosVector(Vector v){position=v;}
    public EnumWrappers.Direction getDirection(){return direction;} public void setDirection(EnumWrappers.Direction v){direction=v;}
    public boolean isInsideBlock(){return insideBlock;} public void setInsideBlock(boolean v){insideBlock=v;}
    public static EquivalentConverter<MovingObjectPositionBlock> getConverter(){return new EquivalentConverter<>(){public MovingObjectPositionBlock getSpecific(Object g){return g instanceof MovingObjectPositionBlock v?v:null;} public Object getGeneric(MovingObjectPositionBlock s){return s;} public Class<MovingObjectPositionBlock> getSpecificType(){return MovingObjectPositionBlock.class;} public Class<?> getGenericType(){return MovingObjectPositionBlock.class;}};}
    @Override public MovingObjectPositionBlock clone(){return new MovingObjectPositionBlock(blockPosition,position==null?null:position.clone(),direction,insideBlock);}
}
