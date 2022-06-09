package com.coolspy3.stackflow;

import java.io.File;
import java.util.Arrays;

public class App
{
    public static void main(String[] args) throws Throwable
    {
        File file = new File(args[0]);
        if (file.exists())
        {
            Interpreter interpreter = new Interpreter();
            if (Arrays.asList(args).contains("debug")) interpreter.debug = true;
            try
            {
                interpreter.exec(file);
            }
            catch (StackException exc)
            {
                exc.getCause().printStackTrace(System.err);
                Arrays.asList(exc.stack).forEach(el -> {
                    System.err.println("at " + el.toString());
                });
            }
            return;
        }
        System.err.println("Cannot find program: " + args[0]);
    }

    public static void test(int[][][] arg0, String arg1, String[] arg2, boolean arg3)
    {}
}
