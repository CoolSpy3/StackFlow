package com.coolspy3.stackflow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

public enum Type
{

    BOOLEAN, STRING, NUMBER, DECIMAL, ARRAY, TYPE, JAVA_OBJECT, JAVA_CLASS, JAVA_METHOD, JAVA_CONSTRUCTOR, OBJECT, CLASS, FUNCTION, VARIABLE, NULL;

    public static Type typeOf(Object o)
    {
        if (o == null) return NULL;
        if (o instanceof Boolean) return BOOLEAN;
        if (o instanceof String) return STRING;
        if (o instanceof BigInteger) return NUMBER;
        if (o instanceof BigDecimal) return DECIMAL;
        if (o instanceof Object[]) return ARRAY;
        if (o instanceof Type) return TYPE;
        if (o instanceof JavaObject) return JAVA_OBJECT;
        if (o instanceof Class) return JAVA_CLASS;
        if (o instanceof Method) return JAVA_METHOD;
        if (o instanceof Constructor) return JAVA_CONSTRUCTOR;
        if (o instanceof StackObject) return OBJECT;
        if (o instanceof StackClass) return CLASS;
        if (o instanceof Function) return FUNCTION;
        if (o instanceof Variable) return VARIABLE;
        return null;
    }

    public static Type fromName(String name)
    {
        switch (name)
        {
            case "boolean":
                return BOOLEAN;
            case "string":
                return STRING;
            case "number":
                return NUMBER;
            case "decimal":
                return DECIMAL;
            case "array":
                return ARRAY;
            case "type":
                return TYPE;
            case "jobject":
                return JAVA_OBJECT;
            case "jclass":
                return JAVA_CLASS;
            case "jmethod":
                return JAVA_METHOD;
            case "jconstructor":
                return JAVA_CONSTRUCTOR;
            case "object":
                return OBJECT;
            case "class":
                return CLASS;
            case "function":
                return FUNCTION;
            case "var":
                return VARIABLE;
            case "null":
                return NULL;
            default:
                return null;
        }
    }

}
