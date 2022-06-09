package com.coolspy3.stackflow;

public class StackException extends Throwable
{

    public final CallStackElement[] stack;

    public StackException(Throwable cause, CallStackElement[] stack)
    {
        super(cause);
        this.stack = stack;
    }

}
