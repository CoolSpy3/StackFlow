package com.coolspy3.stackflow.builtinscripts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.coolspy3.stackflow.Interpreter;

public final class BuiltinLoader
{

    private static final Class<BuiltinLoader> loader = BuiltinLoader.class;

    public static boolean hasScript(String script)
    {
        try (InputStream is = loader.getResourceAsStream(script))
        {
            return is != null;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public static void loadScript(String script, Interpreter interpreter) throws Throwable
    {
        try (InputStream is = loader.getResourceAsStream(script))
        {
            if (is == null)
                throw new FileNotFoundException("Builtin Script: " + script + " cannot be loaded");

            interpreter.exec(is, String.format("<%s>", script));
        }
    }

    private BuiltinLoader()
    {}

}
