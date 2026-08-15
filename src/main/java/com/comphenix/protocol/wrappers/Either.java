/* ProtocolLib2PacketEvents - clean-room either wrapper. */
package com.comphenix.protocol.wrappers;

import java.util.Optional;
import java.util.function.Function;

public abstract class Either<L, R> {
    public static final class Left<L, R> extends Either<L, R> {
        private final L value;
        private Left(L value) { this.value = value; }
        @Override public <T> T map(Function<L, T> left, Function<R, T> right) { return left.apply(value); }
        @Override public Optional<L> left() { return Optional.ofNullable(value); }
        @Override public Optional<R> right() { return Optional.empty(); }
    }
    public static final class Right<L, R> extends Either<L, R> {
        private final R value;
        private Right(R value) { this.value = value; }
        @Override public <T> T map(Function<L, T> left, Function<R, T> right) { return right.apply(value); }
        @Override public Optional<L> left() { return Optional.empty(); }
        @Override public Optional<R> right() { return Optional.ofNullable(value); }
    }
    public abstract <T> T map(Function<L, T> left, Function<R, T> right);
    public abstract Optional<L> left();
    public abstract Optional<R> right();
    public static <L, R> Either<L, R> left(L value) { return new Left<>(value); }
    public static <L, R> Either<L, R> right(R value) { return new Right<>(value); }
}
