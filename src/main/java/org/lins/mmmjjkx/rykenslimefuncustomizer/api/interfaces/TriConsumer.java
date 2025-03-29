package org.lins.mmmjjkx.rykenslimefuncustomizer.api.interfaces;

@FunctionalInterface
public interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
}
