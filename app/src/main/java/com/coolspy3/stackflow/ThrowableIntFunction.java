package com.coolspy3.stackflow;

@FunctionalInterface
public interface ThrowableIntFunction<U>
{
    public U apply(int i) throws Throwable;
}
