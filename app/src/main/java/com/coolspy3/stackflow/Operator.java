package com.coolspy3.stackflow;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class Operator
{

    public final String name;
    public final boolean isUnary;
    public final BinaryOperator<Object[]> arrOp;
    public final BinaryOperator<Boolean> boolOp;
    public final BinaryOperator<BigDecimal> decOp;
    public final BinaryOperator<Runnable> funcOp;
    public final BinaryOperator<BigInteger> intOp;
    public final BinaryOperator<String> stringOp;

    public Operator(String name, boolean isUnary, BinaryOperator<Object[]> arrOp,
            BinaryOperator<Boolean> boolOp, BinaryOperator<BigDecimal> decOp,
            BinaryOperator<Runnable> funcOp, BinaryOperator<BigInteger> intOp,
            BinaryOperator<String> stringOp)
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

    public static Operator unary(String name, UnaryOperator<Object[]> arrOp,
            UnaryOperator<Boolean> boolOp, UnaryOperator<BigDecimal> decOp,
            UnaryOperator<Runnable> funcOp, UnaryOperator<BigInteger> intOp,
            UnaryOperator<String> stringOp)
    {
        return new Operator(name, true, (v, n) -> arrOp.apply(v), (v, n) -> boolOp.apply(v),
                (v, n) -> decOp.apply(v), (v, n) -> funcOp.apply(v), (v, n) -> intOp.apply(v),
                (v, n) -> stringOp.apply(v));
    }

    public static Operator binary(String name, BinaryOperator<Object[]> arrOp,
            BinaryOperator<Boolean> boolOp, BinaryOperator<BigDecimal> decOp,
            BinaryOperator<Runnable> funcOp, BinaryOperator<BigInteger> intOp,
            BinaryOperator<String> stringOp)
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
            case NUMBER -> apply(intOp, stack);
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
    private <T> void apply(BinaryOperator<T> op, LinkedList<Object> stack)
    {
        if (op == null)
            throw new ParseException("Cannot apply operator: " + name + " for the given types.");
        if (isUnary) op.apply((T) stack.poll(), null);
        else
        {
            T el2 = (T) stack.poll();
            T el1 = (T) stack.poll();
            stack.push(
                    op.apply(el1, el2 == null ? (T) Utils.interpretNull(Type.typeOf(el1)) : el2));
        }
    }

}
