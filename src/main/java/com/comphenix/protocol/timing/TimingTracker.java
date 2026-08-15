package com.comphenix.protocol.timing;

import com.comphenix.protocol.PacketType;

public interface TimingTracker { TimingTracker EMPTY=new TimingTracker(){public void track(PacketType type,Runnable action){if(action!=null)action.run();}}; void track(PacketType type,Runnable action); }
