package com.coolspy3.stackflow;

import java.util.Collections;
import java.util.List;

public class Function
{

    public final List<String> code;
    public final Context ctx;

    public Function(List<String> code, Context ctx)
    {
        this.code = Collections.unmodifiableList(code);
        this.ctx = ctx;
    }

    public void exec(Interpreter interpreter) throws Throwable
    {
        Context retCtx = interpreter.ctx;
        interpreter.ctx = ctx;
        try
        {
            interpreter.exec(code);
        }
        finally
        {
            interpreter.ctx = retCtx;
        }
    }

}
