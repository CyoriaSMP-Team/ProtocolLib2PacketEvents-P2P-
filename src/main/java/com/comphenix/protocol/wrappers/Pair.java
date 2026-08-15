/* ProtocolLib2PacketEvents - clean-room pair wrapper. */
package com.comphenix.protocol.wrappers;

import java.util.Objects;

public class Pair<A, B> {
    private A first;
    private B second;
    public Pair(A first, B second) { this.first = first; this.second = second; }
    public A getFirst() { return first; }
    public B getSecond() { return second; }
    public void setFirst(A first) { this.first = first; }
    public void setSecond(B second) { this.second = second; }
    @Override public boolean equals(Object other) {
        return other instanceof Pair<?, ?> pair && Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }
    @Override public int hashCode() { return Objects.hash(first, second); }
}
