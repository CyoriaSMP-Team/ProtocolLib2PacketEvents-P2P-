package com.comphenix.protocol.reflect.cloning;

import org.bukkit.inventory.ItemStack;

public class BukkitCloner implements Cloner {
    @Override public boolean canClone(Object source){return source instanceof ItemStack;}
    @Override public Object clone(Object source){return ((ItemStack)source).clone();}
}
