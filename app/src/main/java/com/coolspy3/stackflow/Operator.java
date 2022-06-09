package com.coolspy3.stackflow;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.function.BiFunction;

public class Operator
{

    public final String name;
    public final boolean isUnary;
    public final BinaryOpFunc<Object[]> arrOp;
    public final BinaryOpFunc<Boolean> boolOp;
    public final BinaryOpFunc<BigDecimal> decOp;
    public final BinaryOpFunc<Function> funcOp;
    public final BinaryOpFunc<BigInteger> intOp;
    public final BinaryOpFunc<String> stringOp;

    public Operator(String name, boolean isUnary, BinaryOpFunc<Object[]> arrOp,
            BinaryOpFunc<Boolean> boolOp, BinaryOpFunc<BigDecimal> decOp,
            BinaryOpFunc<Function> funcOp, BinaryOpFunc<BigInteger> intOp,
            BinaryOpFunc<String> stringOp)
    {
        this.name = name;
        this.isUnary = isUnary;
        this.arrOp = arrOp;
        this.boolOp = boolOp;
        this.decOp = decOp;
        this.funcOp = funcOp;
        this.intOp = intOp;
        this.stringOp = stringOp;
    }

    public static Operator unary(String name, UnaryOpFunc<Object[]> arrOp,
            UnaryOpFunc<Boolean> boolOp, UnaryOpFunc<BigDecimal> decOp,
            UnaryOpFunc<Function> funcOp, UnaryOpFunc<BigInteger> intOp,
            UnaryOpFunc<String> stringOp)
    {
        return new Operator(name, true, arrOp == null ? null : (v, n) -> arrOp.apply(v),
                boolOp == null ? null : (v, n) -> boolOp.apply(v),
                decOp == null ? null : (v, n) -> decOp.apply(v),
                funcOp == null ? null : (v, n) -> funcOp.apply(v),
                intOp == null ? null : (v, n) -> intOp.apply(v),
                stringOp == null ? null : (v, n) -> stringOp.apply(v));
    }

    public static Operator binary(String name, BinaryOpFunc<Object[]> arrOp,
            BinaryOpFunc<Boolean> boolOp, BinaryOpFunc<BigDecimal> decOp,
            BinaryOpFunc<Function> funcOp, BinaryOpFunc<BigInteger> intOp,
            BinaryOpFunc<String> stringOp)
    {
        return new Operator(name, false, arrOp, boolOp, decOp, funcOp, intOp, stringOp);
    }

    public void apply(LinkedList<Object> stack, Interpreter interpreter) throws Throwable
    {
        int firstIdx = isUnary ? 0 : 1;
        switch (Type.typeOf(stack.get(firstIdx)))
        {
            case ARRAY -> apply(arrOp, stack);
            case BOOLEAN -> apply(boolOp, stack);
            case DECIMAL -> apply(decOp, stack);
            case FUNCTION -> apply(funcOp, stack);
            case NULL ->
            {
                if (isUnary)
                    throw new ParseException("Cannot apply operation: " + name + " on <null>");
                Object o2 = stack.peek();
                if (o2 == null) throw new ParseException(
                        "Cannot apply operation: " + name + " on <null>, <null>");
                if (o2 instanceof Type) stack.set(firstIdx, Type.NULL);
                else if (o2 instanceof Variable) throw new ParseException(
                        "Cannot apply operation: " + name + " on <null>, Variable");
                else
                    stack.set(firstIdx, Utils.interpretNull(Type.typeOf(o2)));
                apply(stack, interpreter);
            }
            case NUMBER ->
            {
                if (!isUnary && stack.peek() instanceof BigDecimal)
                {
                    stack.set(1, new BigDecimal((BigInteger) stack.get(1)));
                    apply(decOp, stack);
                }
                else
                    apply(intOp, stack);
            }
            case STRING ->
            {
                if (isUnary) apply(stringOp, stack);
                else
                {
                    String el2 = (String) stack.poll();
                    String el1 = stack.peek() == null ? "" : stack.peek().toString();
                    stack.poll();
                    stack.push(stringOp.apply(el1, el2));
                }
            }
            case TYPE ->
            {
                stack.set(firstIdx, stack.get(firstIdx).toString());
                apply(stack, interpreter);
            }
            case VARIABLE ->
            {
                Variable var = (Variable) stack.get(firstIdx);
                stack.set(firstIdx, var.obj);
                apply(stack, interpreter);
                var.obj = stack.poll();
            }
            case CLASS -> ((Function) ((StackClass) stack.get(firstIdx)).ctx.getPublic(name).obj)
                    .exec(interpreter);
            case OBJECT -> ((Function) ((StackObject) stack.get(firstIdx)).ctx.getPublic(name).obj)
                    .exec(interpreter);
            default -> throw new ParseException("Unknown Type!");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void apply(BinaryOpFunc<T> op, LinkedList<Object> stack)
    {
        if (op == null)
            throw new ParseException("Cannot apply operator: " + name + " for the given types.");
        if (isUnary) stack.push(op.apply((T) stack.poll(), null));
        else
        {
            Object rel2 = stack.poll();
            if (rel2 instanceof Variable) rel2 = ((Variable) rel2).obj;
            else if (rel2 instanceof Type) rel2 = rel2.toString();
            T el1 = (T) stack.poll();
            if (el1 instanceof BigDecimal && rel2 instanceof BigInteger)
                rel2 = new BigDecimal((BigInteger) rel2);
            T el2 = (T) rel2;
            stack.push(
                    op.apply(el1, el2 == null ? (T) Utils.interpretNull(Type.typeOf(el1)) : el2));
        }
    }

    @FunctionalInterface
    public static interface UnaryOpFunc<T> extends java.util.function.Function<T, Object>
    {}

    @FunctionalInterface
    public static interface BinaryOpFunc<T> extends BiFunction<T, T, Object>
    {}

}
