package com.coolspy3.stackflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Function
{

    public static final Function NOOP =
            new Function(new ArrayList<>(), Context.ROOT_CTX, new CallStackElement("<noop>", 0));

    public final List<String> code;
    public final Context ctx;
    public final CallStackElement location;

    public Function(List<String> code, Context ctx, CallStackElement location)
    {
        this.code = Collections.unmodifiableList(code);
        this.ctx = ctx;
        this.location = location;
    }

    public void exec(Interpreter interpreter) throws Throwable
    {
        Context retCtx = interpreter.ctx;
        interpreter.ctx = ctx.push();
        try
        {
            interpreter.exec(code, location.file, location.line);
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
        return new Function(instructions, ctx, new CallStackElement("<concat_function>", 0));
    }

}
