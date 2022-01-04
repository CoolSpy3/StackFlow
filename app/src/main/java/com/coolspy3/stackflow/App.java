package com.coolspy3.stackflow;

import java.io.File;

public class App
{
    public static void main(String[] args) throws Throwable
    {
        File file = new File(args[0]);
        if (file.exists())
        {
            new Interpreter().exec(file);
            return;
        }
        System.err.println("Cannot find program: " + args[0]);
    }
}
