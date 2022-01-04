package com.coolspy3.stackflow;

@FunctionalInterface
public interface ThrowableSupplier<T>
{
    public T get() throws Throwable;
}
