package com.coolspy3.stackflow;

@FunctionalInterface
public interface ThrowableFunction<T, U>
{
    public U apply(T t) throws Throwable;
}
