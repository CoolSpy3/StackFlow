package com.coolspy3.stackflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.coolspy3.stackflow.builtinscripts.BuiltinLoader;

import org.apache.commons.text.StringEscapeUtils;

public class Interpreter
{

    public static final ArrayList<Operator> OPERATORS = new ArrayList<>(Arrays.asList(
            Operator.unary("int", a -> a.length, b -> b ? 1 : 0, BigDecimal::intValue, null,
                    java.util.function.Function.identity()::apply, String::length),
            Operator.binary("add", Utils::concatArrs, (a, b) -> a || b, BigDecimal::add,
                    Function::concat, BigInteger::add, String::join),
            Operator.unary("inc", arr -> Arrays.copyOf(arr, arr.length + 1), b -> !b,
                    i -> i.add(BigDecimal.ONE), null, i -> i.add(BigInteger.ONE), null),
            Operator.binary("sub", null, (a, b) -> a ^ b, BigDecimal::subtract, null,
                    BigInteger::subtract, null),
            Operator.unary("dec", arr -> Arrays.copyOf(arr, arr.length - 1), b -> !b,
                    i -> i.subtract(BigDecimal.ONE), null, i -> i.subtract(BigInteger.ONE), null),
            Operator.binary("mult", null, null, BigDecimal::multiply, null, BigInteger::multiply,
                    null),
            Operator.binary("div", null, null, BigDecimal::divide, null, BigInteger::divide, null),
            Operator.binary("mod", null, null, null, null, BigInteger::mod, null),
            Operator.unary("not", null, b -> !b, null, null, BigInteger::not, null),
            Operator.binary("bitShift", null, null, null, null, (a, b) -> a.shiftLeft(b.intValue()),
                    null),
            Operator.binary("gt", (a, b) -> a.length > b.length, (a, b) -> a ? !b : false,
                    (a, b) -> a.compareTo(b) > 0, null, (a, b) -> a.compareTo(b) > 0,
                    (a, b) -> a.compareTo(b) > 0),
            Operator.binary("lt", (a, b) -> a.length < b.length, (a, b) -> a ? false : b,
                    (a, b) -> a.compareTo(b) < 0, null, (a, b) -> a.compareTo(b) < 0,
                    (a, b) -> a.compareTo(b) < 0),
            Operator.binary("gteq", (a, b) -> a.length >= b.length, (a, b) -> a ? true : !b,
                    (a, b) -> a.compareTo(b) >= 0, null, (a, b) -> a.compareTo(b) >= 0,
                    (a, b) -> a.compareTo(b) >= 0),
            Operator.binary("lteq", (a, b) -> a.length <= b.length, (a, b) -> a ? b : true,
                    (a, b) -> a.compareTo(b) <= 0, null, (a, b) -> a.compareTo(b) <= 0,
                    (a, b) -> a.compareTo(b) < 0),
            Operator.binary("eq", (a, b) -> Arrays.equals(a, b), (a, b) -> a == b,
                    (a, b) -> a.compareTo(b) == 0, null, (a, b) -> a.equals(b),
                    (a, b) -> a.equals(b)),
            Operator.binary("neq", (a, b) -> !Arrays.equals(a, b), (a, b) -> a != b,
                    (a, b) -> a.compareTo(b) != 0, null, (a, b) -> !a.equals(b),
                    (a, b) -> !a.equals(b)),
            Operator.binary("and", null, (a, b) -> a && b, null, null, (a, b) -> a.and(b), null),
            Operator.binary("or", null, (a, b) -> a || b, null, null, (a, b) -> a.or(b), null),
            Operator.binary("nand", null, (a, b) -> !(a && b), null, null, (a, b) -> a.and(b).not(),
                    null),
            Operator.binary("nor", null, (a, b) -> !(a || b), null, null, (a, b) -> a.or(b).not(),
                    null),
            Operator.binary("xor", null, (a, b) -> a ^ b, null, null, (a, b) -> a.xor(b), null),
            Operator.binary("xnor", null, (a, b) -> !(a ^ b), null, null, (a, b) -> a.xor(b).not(),
                    null),
            Operator.unary("neg", null, b -> !b, BigDecimal::negate, null, BigInteger::negate,
                    null),
            Operator.unary("abs", null, null, BigDecimal::abs, null, BigInteger::abs, null),
            Operator.binary("pow", null, null,
                    (a, b) -> new BigDecimal(Math.pow(a.doubleValue(), b.doubleValue())), null,
                    (a, b) -> a.pow(b.intValue()), null)));

    public LinkedList<Object> stack = new LinkedList<>();
    public final LinkedList<LinkedList<Object>> stackStack = new LinkedList<>();
    public Context ctx = new Context(Context.ROOT_CTX);

    public void exec(File file) throws Throwable
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            exec(reader.lines().collect(Collectors.toList()));
        }
    }

    public void exec(InputStream is) throws Throwable
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            exec(reader.lines().collect(Collectors.toList()));
        }
    }

    public void exec(List<String> lines) throws Throwable
    {
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).strip();

            if (line.startsWith("//") || line.isBlank()) continue;

            // System.out.println(String.format(
            // "\nEvaluating Line: %s of %s: %s\nCurrent Stack: %s\nCurrent Context: %s\n",
            // i + 1, lines.size(), line, stack, ctx));

            if (line.equals("null"))
            {
                stack.push(null);
                continue;
            }

            if (line.equals("true"))
            {
                stack.push(true);
                continue;
            }

            if (line.equals("false"))
            {
                stack.push(false);
                continue;
            }

            if (line.startsWith("\""))
            {
                if (!line.endsWith("\"")) throw new ParseException("Unterminated String: " + line);

                stack.push(StringEscapeUtils.unescapeJava(line.substring(1, line.length() - 1)));
                continue;
            }

            if (line.startsWith("\'"))
            {
                if (!line.endsWith("\'")) throw new ParseException("Unterminated String: " + line);

                stack.push(line.substring(1, line.length() - 1));
                continue;
            }

            {
                BigInteger v = Utils.isInteger(line);
                if (v != null)
                {
                    stack.push(v);
                    continue;
                }
            }

            {
                BigDecimal v = Utils.isDecimal(line);
                if (v != null)
                {
                    stack.push(v);
                    continue;
                }
            }

            switch (line)
            {
                case "beginFunc" ->
                {
                    i++;
                    ArrayList<String> func = new ArrayList<>();
                    int f = 0;
                    for (;; i++)
                    {
                        if (i == lines.size()) throw new ParseException("Unterminated Function");
                        String instruction = lines.get(i).strip();
                        if (instruction.equals("beginFunc")) f++;
                        else if (instruction.equals("endFunc"))
                        {
                            if (f == 0) break;
                            else
                                f--;
                        }
                        func.add(instruction);
                    }

                    stack.push(new Function(func, ctx));
                }
                case "noop" -> stack.push(Function.NOOP);
                case "call" ->
                {
                    try
                    {
                        ((Function) stack.poll()).exec(this);
                    }
                    catch (ControlInterrupt e)
                    {
                        if (e != ControlInterrupt.RETURN) throw e;
                    }
                }
                case "jcall" ->
                {
                    Method method = (Method) stack.poll();
                    Object obj = null;
                    if (!Modifier.isStatic(method.getModifiers()))
                        obj = stack.peek() instanceof JavaObject ? ((JavaObject) stack.poll()).o
                                : stack.poll();
                    Class<?>[] types = method.getParameterTypes();
                    Object[] params =
                            Utils.stackFillArray(new Object[types.length], j -> stack.poll());
                    try
                    {
                        Object retVal = method.invoke(obj, Utils.convertArgs(types, params));
                        if (method.getReturnType() != Void.TYPE)
                            stack.push(Utils.convertJava(retVal));
                    }
                    catch (InvocationTargetException e)
                    {
                        throw e.getCause();
                    }
                }
                case "jNew" ->
                {
                    Constructor<?> constructor = (Constructor<?>) stack.poll();
                    Class<?>[] types = constructor.getParameterTypes();
                    Object[] params =
                            Utils.stackFillArray(new Object[types.length], j -> stack.poll());
                    try
                    {
                        stack.push(Utils.convertJava(
                                constructor.newInstance(Utils.convertArgs(types, params))));
                    }
                    catch (InvocationTargetException e)
                    {
                        throw e.getCause();
                    }
                }
                case "findClass" -> stack.push(Class.forName(stack.poll().toString()));
                case "findField" -> stack.push(Utils
                        .convertJava(((Class<?>) stack.poll()).getField(stack.poll().toString())
                                .get(stack.peek() instanceof JavaObject
                                        ? ((JavaObject) stack.poll()).o
                                        : stack.poll())));
                case "findStaticField" -> stack.push(Utils.convertJava(
                        ((Class<?>) stack.poll()).getField(stack.poll().toString()).get(null)));
                case "findMethod" ->
                {
                    int numTypes = ((BigInteger) stack.poll()).intValue();
                    Class<?>[] types = new Class[numTypes];
                    for (int j = 0; j < numTypes; j++)
                        types[j] = (Class<?>) stack.poll();
                    stack.push(((Class<?>) stack.poll()).getMethod(stack.poll().toString(), types));
                }
                case "findConstructor" ->
                {
                    int numTypes = ((BigInteger) stack.poll()).intValue();
                    Class<?>[] types = new Class[numTypes];
                    for (int j = 0; j < numTypes; j++)
                        types[j] = (Class<?>) stack.poll();
                    stack.push(((Class<?>) stack.poll()).getConstructor(types));
                }
                case "define" -> ctx.put(stack.poll().toString(), new Variable(stack.poll()));
                case "set" -> ((Variable) stack.poll()).obj = stack.poll();
                case "val" -> stack.push(((Variable) stack.poll()).obj);
                case "var" -> stack.push(ctx.get(stack.poll().toString()));
                case "eval" -> stack.push(ctx.get(stack.poll().toString()).obj);
                case "delete" -> ctx.remove(stack.poll().toString());
                case "pushCtx" -> ctx = ctx.push();
                case "pushRootCtx" -> ctx = ctx.pushNewRoot();
                case "popCtx" -> ctx = ctx.pop();
                case "pushStack" ->
                {
                    stackStack.push(stack);
                    stack = new LinkedList<>();
                }
                case "popStack" -> stack = stackStack.pop();
                case "clearStack" -> stack.clear();
                case "swp" ->
                {
                    Object o1 = stack.poll();
                    Object o2 = stack.poll();
                    stack.push(o1);
                    stack.push(o2);
                }
                case "pull" ->
                {
                    int idx = ((BigInteger) stack.poll()).intValue();
                    stack.push(stack.remove(idx));
                }
                case "typeOf" -> stack.push(Type.typeOf(stack.poll()));
                case "string" -> stack.push(stack.poll().toString());
                case "type" -> stack.push(Type.fromName(stack.poll().toString()));
                case "array" ->
                {
                    int len = ((BigInteger) stack.poll()).intValue();
                    Object[] arr = new Object[len];
                    for (int j = len - 1; j >= 0; j--)
                        arr[j] = stack.poll();
                    stack.push(arr);
                }
                case "len" -> stack.push(BigInteger.valueOf(((Object[]) stack.poll()).length));
                case "get" ->
                {
                    int idx = ((BigInteger) stack.poll()).intValue();
                    Object[] arr = (Object[]) stack.poll();
                    stack.push(arr[idx]);
                }
                case "for" ->
                {
                    Function incFunc = (Function) stack.poll();
                    Function checkFunc = (Function) stack.poll();
                    Function initFunc = (Function) stack.poll();
                    Function func = (Function) stack.poll();

                    ThrowableSupplier<Boolean> checker = () -> {
                        exec(checkFunc.code);
                        return ((Boolean) stack.poll()) == true;
                    };

                    Context retCtx = ctx;
                    ctx = func.ctx.push();

                    try
                    {
                        for (exec(initFunc.code); checker.get(); exec(incFunc.code))
                            try
                            {
                                exec(func.code);
                            }
                            catch (ControlInterrupt e)
                            {
                                if (e == ControlInterrupt.BREAK) break;
                                else if (e == ControlInterrupt.CONTINUE) continue;
                                else
                                    throw e;
                            }
                    }
                    finally
                    {
                        ctx = retCtx;
                    }
                }
                case "while" ->
                {
                    Function checkFunc = (Function) stack.poll();
                    Function func = (Function) stack.poll();

                    ThrowableSupplier<Boolean> checker = () -> {
                        checkFunc.exec(this);
                        return ((Boolean) stack.poll()) == true;
                    };

                    while (checker.get())
                        try
                        {
                            func.exec(this);
                        }
                        catch (ControlInterrupt e)
                        {
                            if (e == ControlInterrupt.BREAK) break;
                            else if (e == ControlInterrupt.CONTINUE) continue;
                            else
                                throw e;
                        }
                }
                case "doWhile" ->
                {
                    Function checkFunc = (Function) stack.poll();
                    Function func = (Function) stack.poll();

                    ThrowableSupplier<Boolean> checker = () -> {
                        checkFunc.exec(this);
                        return ((Boolean) stack.poll()) == true;
                    };

                    do
                        try
                        {
                            func.exec(this);
                        }
                        catch (ControlInterrupt e)
                        {
                            if (e == ControlInterrupt.BREAK) break;
                            else if (e == ControlInterrupt.CONTINUE) continue;
                            else
                                throw e;
                        }
                    while (checker.get());
                }
                case "break" -> throw ControlInterrupt.BREAK;
                case "continue" -> throw ControlInterrupt.CONTINUE;
                case "return" -> throw ControlInterrupt.RETURN;
                case "if" ->
                {
                    if ((Boolean) stack.poll()) ((Function) stack.poll()).exec(this);
                }
                case "ifElse" ->
                {
                    if ((Boolean) stack.poll())
                    {
                        stack.poll();
                        ((Function) stack.poll()).exec(this);
                    }
                    else
                    {
                        ((Function) stack.poll()).exec(this);
                        stack.poll();
                    }
                }
                case "tryCatch" ->
                {
                    Function catchFunc = (Function) stack.poll();
                    Function tryFunc = (Function) stack.poll();
                    try
                    {
                        tryFunc.exec(this);
                    }
                    catch (Exception e)
                    {
                        stack.push(e);
                        catchFunc.exec(this);
                    }
                }
                case "tryFinally" ->
                {
                    Function finallyFunc = (Function) stack.poll();
                    Function tryFunc = (Function) stack.poll();
                    try
                    {
                        tryFunc.exec(this);
                    }
                    finally
                    {
                        finallyFunc.exec(this);
                    }
                }
                case "tryCatchFinally" ->
                {
                    Function finallyFunc = (Function) stack.poll();
                    Function catchFunc = (Function) stack.poll();
                    Function tryFunc = (Function) stack.poll();
                    try
                    {
                        tryFunc.exec(this);
                    }
                    catch (Exception e)
                    {
                        stack.push(e);
                        catchFunc.exec(this);
                    }
                    finally
                    {
                        finallyFunc.exec(this);
                    }
                }
                case "throw" -> throw (Throwable) ((JavaObject) stack.poll()).o;
                case "assert" ->
                {
                    if (!(Boolean) stack.poll())
                        throw new AssertionError("Assertion: " + line + " failed!");
                }
                case "load" -> loadScript(stack.poll().toString());
                case "include" ->
                {
                    ctx = ctx.push();
                    try
                    {
                        loadScript(stack.poll().toString());
                    }
                    finally
                    {
                        stack.push(new StackObject(null, ctx));
                        ctx = ctx.pop();
                    }
                }
                case "import" ->
                {
                    ctx = ctx.push();
                    try
                    {
                        stackStack.push(stack);
                        stack = new LinkedList<>();
                        try
                        {
                            loadScript(stack.poll().toString());
                        }
                        finally
                        {
                            stack = stackStack.pop();
                        }
                    }
                    finally
                    {
                        stack.push(new StackObject(null, ctx));
                        ctx = ctx.pop();
                    }
                }
                case "class" ->
                {
                    Object[] superClasses = (Object[]) stack.poll();
                    if (superClasses == null) superClasses = new Object[0];
                    Function initFunc = (Function) stack.poll();
                    Function staticInitFunc = (Function) stack.poll();
                    stack.push(StackClass.create(staticInitFunc, initFunc,
                            Arrays.copyOf(superClasses, superClasses.length, StackClass[].class),
                            this));
                }
                case "getMember" ->
                {
                    String memberName = stack.poll().toString();
                    Object obj = stack.poll();
                    if (obj instanceof StackClass)
                        stack.push(((StackClass) obj).ctx.getPublic(memberName));
                    stack.push(((StackObject) obj).ctx.getPublic(memberName));
                }
                case "evalMember" ->
                {
                    String memberName = stack.poll().toString();
                    Object obj = stack.poll();
                    if (obj instanceof StackClass)
                        stack.push(((StackClass) obj).ctx.getPublic(memberName).obj);
                    else
                        stack.push(((StackObject) obj).ctx.getPublic(memberName).obj);
                }
                case "setMember" ->
                {
                    String memberName = stack.poll().toString();
                    Object obj = stack.poll();
                    Object val = stack.poll();
                    if (obj instanceof StackClass)
                        stack.push(((StackClass) obj).ctx.putPublic(memberName, val));
                    stack.push(((StackObject) obj).ctx.putPublic(memberName, val));
                }
                case "public" ->
                {
                    Variable var = (Variable) stack.poll();
                    var.modif = ModifierUtils.makePublic(var.modif);
                }
                case "protected" ->
                {
                    Variable var = (Variable) stack.poll();
                    var.modif = ModifierUtils.makeProtected(var.modif);
                }
                case "private" ->
                {
                    Variable var = (Variable) stack.poll();
                    var.modif = ModifierUtils.makePrivate(var.modif);
                }
                case "copy" ->
                {
                    Object obj = stack.peek();
                    if (obj instanceof Object[])
                        obj = Arrays.copyOf((Object[]) obj, ((Object[]) obj).length);
                    stack.push(obj);
                }
                case "pop" -> stack.pop();
                case "expand" -> Arrays.stream((Object[]) stack.poll()).forEach(stack::push);
                default ->
                {
                    Optional<Operator> op =
                            OPERATORS.stream().filter(o -> o.name.equals(line)).findAny();

                    if (op.isEmpty()) throw new ParseException("Unknown Command: " + line);

                    op.get().apply(stack, this);
                }
            }
        }
    }

    public void loadScript(String script) throws Throwable
    {
        script += ".sf";
        if (BuiltinLoader.hasScript(script)) BuiltinLoader.loadScript(script, this);
        else
            exec(new File(script));
    }

}
