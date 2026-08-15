package com.comphenix.protocol.injector.netty.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacketListenerInvokerTest {
    @Test
    void prefersCurrentPacketListenerAndFallsBackToConnection() {
        TestPacket packet = new TestPacket();
        FakeConnection connection = new FakeConnection();
        PacketListenerInvoker invoker = new PacketListenerInvoker(connection);

        invoker.send(packet);
        invoker.disconnect("bye");
        assertEquals("listener-send", connection.listener.lastAction);
        assertEquals("listener-disconnect:bye", connection.listener.lastDisconnect);

        connection.listener = null;
        invoker.send(packet);
        invoker.disconnect("fallback");
        assertEquals("connection-send", connection.lastAction);
        assertEquals("connection-disconnect:fallback", connection.lastDisconnect);
    }

    private static final class TestPacket { }

    private static final class FakeListener {
        private String lastAction;
        private String lastDisconnect;

        private void send(TestPacket packet) {
            lastAction = "listener-send";
        }

        private void disconnect(String reason) {
            lastDisconnect = "listener-disconnect:" + reason;
        }
    }

    private static final class FakeConnection {
        private FakeListener listener = new FakeListener();
        private String lastAction;
        private String lastDisconnect;

        private FakeListener getPacketListener() {
            return listener;
        }

        private void send(TestPacket packet) {
            lastAction = "connection-send";
        }

        private void disconnect(String reason) {
            lastDisconnect = "connection-disconnect:" + reason;
        }
    }
}
