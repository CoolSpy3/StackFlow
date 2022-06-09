package com.coolspy3.stackflow;

public class CallStackElement
{

    public final String file;
    public final int line;

    public CallStackElement(String file, int line)
    {
        this.file = file;
        this.line = line;
    }

    @Override
    public String toString()
    {
        return String.format("%s:%d", file, line + 1);
    }

}
