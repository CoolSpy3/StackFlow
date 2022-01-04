package com.coolspy3.stackflow;

public final class ModifierUtils
{

    public static final int FINAL = 0x01;
    public static final int PROTECTED = 0x02;
    public static final int PRIVATE = 0x04;

    public static int makeFinal(int modif)
    {
        return modif | FINAL;
    }

    public static int makePublic(int modif)
    {
        return modif & ~(PROTECTED | PRIVATE);
    }

    public static int makeProtected(int modif)
    {
        return modif & ~PRIVATE | PROTECTED;
    }

    public static int makePrivate(int modif)
    {
        return modif & ~PROTECTED | PRIVATE;
    }

    public static boolean isFinal(int modif)
    {
        return (modif & FINAL) == FINAL;
    }

    public static boolean isProtected(int modif)
    {
        return (modif & PROTECTED) == PROTECTED;
    }

    public static boolean isPrivate(int modif)
    {
        return (modif & PRIVATE) == PRIVATE;
    }

    public static boolean isPublic(int modif)
    {
        return !isProtected(modif) && !isPrivate(modif);
    }

    public static boolean allowProtectedAccess(int modif)
    {
        return isProtected(modif) || isPublic(modif);
    }

    private ModifierUtils()
    {}

}
