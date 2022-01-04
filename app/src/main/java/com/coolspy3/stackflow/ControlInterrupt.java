package com.coolspy3.stackflow;

public class ControlInterrupt extends Throwable
{

    public static final ControlInterrupt BREAK = new ControlInterrupt();
    public static final ControlInterrupt CONTINUE = new ControlInterrupt();
    public static final ControlInterrupt RETURN = new ControlInterrupt();

    public ControlInterrupt()
    {}

    public ControlInterrupt(String message)
    {
        super(message);
    }

    public ControlInterrupt(Throwable cause)
    {
        super(cause);
    }

    public ControlInterrupt(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ControlInterrupt(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
