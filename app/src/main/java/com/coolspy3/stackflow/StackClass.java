package com.coolspy3.stackflow;

public class StackClass
{

    public final Context ctx;
    public final Function initFunc;
    public final StackClass[] superClasses;

    public StackClass(Context ctx, Function initFunc, StackClass[] superClasses)
    {
        this.ctx = ctx;
        this.initFunc = initFunc;
        this.superClasses = superClasses;
    }

    public static StackClass create(Function staticInitFunc, Function initFunc,
            StackClass[] superClasses, Interpreter interpreter) throws Throwable
    {
        Context[] sprs = Utils.transformArray(superClasses, StackClass::ctx, Context[]::new);
        Context ctx = staticInitFunc.ctx.resolveAsTail(sprs);
        Context retCtx = interpreter.ctx;
        interpreter.ctx = ctx;
        try
        {
            interpreter.exec(staticInitFunc.code);
        }
        finally
        {
            interpreter.ctx = retCtx;
        }

        return new StackClass(ctx.relativize().resolveAsHead(sprs), initFunc, superClasses);
    }

    public StackObject init(Interpreter interpreter) throws Throwable
    {
        Context[] sprs = Utils.transformThrowableArray(superClasses,
                clazz -> clazz.init(interpreter).ctx, Context[]::new);
        Context ctx = initFunc.ctx.resolveAsTail(sprs);
        Context retCtx = interpreter.ctx;
        interpreter.ctx = ctx;
        try
        {
            interpreter.exec(initFunc.code);
        }
        finally
        {
            interpreter.ctx = retCtx;
        }

        return new StackObject(this, ctx);
    }

    public Context ctx()
    {
        return ctx;
    }

    public Function initFunc()
    {
        return initFunc;
    }

    public StackClass[] superClasses()
    {
        return superClasses;
    }

}
