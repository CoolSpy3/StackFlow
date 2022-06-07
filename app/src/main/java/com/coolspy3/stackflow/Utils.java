package com.coolspy3.stackflow;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils
{

    @SafeVarargs
    public static <T, U extends Collection<T>> Set<T> concat(U... u)
    {
        return Stream.of(u).flatMap(U::stream).collect(Collectors.toSet());
    }

    public static Object[] concatArrs(Object[]... arrs)
    {
        return Stream.of(arrs).flatMap(Stream::of).toArray();
    }

    public static BigDecimal isDecimal(String s)
    {
        try
        {
            return new BigDecimal(s);
        }
        catch (NumberFormatException e)
        {
            if (s.substring(s.length() - 1).toLowerCase().equals("d"))
            {
                s = s.substring(0, s.length() - 1);
                try
                {
                    return new BigDecimal(s);
                }
                catch (NumberFormatException exc)
                {}
            }
            return null;
        }
    }

    public static BigInteger isInteger(String s)
    {
        try
        {
            return new BigInteger(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static Object convertJava(Object o)
    {
        if (o.getClass().isArray())
        {
            Object[] jarr = (Object[]) o;
            return transformArray(jarr, Utils::convertJava, Object[]::new);
        }
        if (Type.typeOf(o) == Type.JAVA_OBJECT) return new JavaObject(o);
        else if (o instanceof Character) return "" + o;
        else
            return o;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object[] convertArgs(Class<?>[] types, Object[] args)
    {
        Object[] params = new Object[types.length];
        for (int i = 0; i < types.length; i++)
        {
            Object param = args[i];
            if (param == null) params[i] = null;
            else if (param instanceof JavaObject) params[i] = ((JavaObject) param).o;
            else if (types[i].isArray())
            {
                Object[] arr = (Object[]) param;
                Class<?>[] newTypes = new Class[arr.length];
                Arrays.fill(newTypes, types[i].arrayType());
                Object[] newVals = convertArgs(newTypes, arr);
                params[i] = Arrays.copyOf(newVals, newVals.length, (Class) types[i]);
            }
            else if (types[i] == String.class) params[i] = param.toString();
            else if (types[i] == Character.class || types[i] == Character.TYPE)
            {
                if (param instanceof String) params[i] = ((String) param).charAt(0);
                else
                    params[i] = (char) ((BigInteger) param).intValue();
            }
            else if (types[i] == Integer.class || types[i] == Integer.TYPE)
                params[i] = ((BigInteger) param).intValue();
            else if (types[i] == Byte.class || types[i] == Byte.TYPE)
                params[i] = (byte) ((BigInteger) param).intValue();
            else if (types[i] == Short.class || types[i] == Short.TYPE)
                params[i] = (short) ((BigInteger) param).intValue();
            else if (types[i] == Long.class || types[i] == Long.TYPE)
                params[i] = ((BigInteger) param).longValue();
            else if (types[i] == Float.class || types[i] == Float.TYPE)
            {
                if (param instanceof BigInteger)
                    params[i] = (float) ((BigInteger) param).longValue();
                else
                    params[i] = ((BigDecimal) param).floatValue();
            }
            else if (types[i] == Double.class || types[i] == Double.TYPE)
            {
                if (param instanceof BigInteger)
                    params[i] = (double) ((BigInteger) param).longValue();
                else
                    params[i] = ((BigDecimal) param).doubleValue();
            }
            else
                params[i] = param;
        }

        return params;
    }

    public static <T, U> U[] transformArray(T[] arr, Function<T, U> converter,
            IntFunction<U[]> constructor)
    {
        return fillArray(constructor.apply(arr.length), i -> converter.apply(arr[i]));
    }

    public static <T, U> U[] transformThrowableArray(T[] arr, ThrowableFunction<T, U> converter,
            IntFunction<U[]> constructor) throws Throwable
    {
        return fillThrowableArray(constructor.apply(arr.length), i -> converter.apply(arr[i]));
    }

    public static <T> T[] fillArray(T[] arr, IntFunction<T> supplier)
    {
        for (int i = arr.length - 1; i >= 0; i--)
            arr[i] = supplier.apply(i);
        return arr;
    }

    public static <T> T[] fillThrowableArray(T[] arr, ThrowableIntFunction<T> supplier)
            throws Throwable
    {
        for (int i = arr.length - 1; i >= 0; i--)
            arr[i] = supplier.apply(i);
        return arr;
    }

    public static <T> T[] stackFillArray(T[] arr, IntFunction<T> supplier)
    {
        for (int i = arr.length - 1; i >= 0; i--)
            arr[i] = supplier.apply(i);
        return arr;
    }

    public static Object interpretNull(Type type)
    {
        switch (type)
        {
            case ARRAY:
                return new Object[0];
            case BOOLEAN:
                return false;
            case DECIMAL:
                return BigDecimal.ZERO;
            case FUNCTION:
                return new com.coolspy3.stackflow.Function(new ArrayList<>(),
                        new Context(Context.ROOT_CTX));
            case NUMBER:
                return BigInteger.ZERO;
            case STRING:
                return "";
            case TYPE:
                return Type.NULL;
            case VARIABLE:
                return new Variable(null);
            case CLASS:
            case JAVA_CLASS:
            case JAVA_CONSTRUCTOR:
            case JAVA_METHOD:
            case JAVA_OBJECT:
            case NULL:
            case OBJECT:
            default:
                return null;
        }
    }

    private Utils()
    {}

}
