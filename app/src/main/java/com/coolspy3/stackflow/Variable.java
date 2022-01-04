package com.coolspy3.stackflow;

public final class Variable
{

    public Object obj;
    public int modif;

    public Variable(Object obj)
    {
        this(obj, ModifierUtils.PROTECTED);
    }

    public Variable(Object obj, int modif)
    {
        this.obj = obj;
        this.modif = modif;
    }

    @Override
    public String toString()
    {
        return "Var(" + obj.toString() + ", " + modif + ")";
    }

    public Object getObj()
    {
        return obj;
    }

    public void setObj(Object obj)
    {
        this.obj = obj;
    }

    public int getModif()
    {
        return modif;
    }

    public void setModif(int modif)
    {
        this.modif = modif;
    }

}
