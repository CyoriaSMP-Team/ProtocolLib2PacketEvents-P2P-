package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NettyChannelInjectorTest {
    @Test
    void preservesLengthPrefixedInboundAndWireOutboundFrames() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyChannelInjector injector = new NettyChannelInjector(null, null, channel, null, null, null);
        injector.inject();

        assertNotNull(channel.pipeline().get("p2p-inbound"));
        assertNotNull(channel.pipeline().get("p2p-outbound"));

        byte[] inbound = writeAndReadInbound(channel, new byte[]{2, 1, 9});
        assertArrayEquals(new byte[]{2, 1, 9}, inbound);
        channel.writeOutbound(new WirePacket(1, new byte[]{9}));
        ByteBuf outbound = channel.readOutbound();
        assertNotNull(outbound);
        byte[] bytes = new byte[outbound.readableBytes()];
        outbound.readBytes(bytes);
        outbound.release();
        assertArrayEquals(new byte[]{1, 9}, bytes);
        injector.close();
        channel.finishAndReleaseAll();
    }

    private static byte[] writeAndReadInbound(EmbeddedChannel channel, byte[] bytes) {
        channel.writeInbound(Unpooled.wrappedBuffer(bytes));
        ByteBuf inbound = channel.readInbound();
        assertNotNull(inbound);
        byte[] result = new byte[inbound.readableBytes()];
        inbound.readBytes(result);
        inbound.release();
        return result;
    }
}
