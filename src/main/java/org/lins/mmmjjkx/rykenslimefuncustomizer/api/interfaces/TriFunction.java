package org.lins.mmmjjkx.rykenslimefuncustomizer.api.interfaces;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}
