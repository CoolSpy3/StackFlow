package com.coolspy3.stackflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Function
{

    public static final Function NOOP = new Function(new ArrayList<>(), Context.ROOT_CTX);

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

    public static Function concat(Function... funcs)
    {
        Context ctx = new Context(Context.ROOT_CTX);
        ArrayList<String> instructions = new ArrayList<>();
        for (int i = 0; i < funcs.length; i++)
        {
            String name = Integer.toString(i);
            ctx.add(name, new Variable(funcs[i]));
            instructions.add(name);
            instructions.add("eval");
            instructions.add("call");
        }
        return new Function(instructions, ctx);
    }

}
