package com.coolspy3.stackflow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Context implements Map<String, Variable>
{

    private final HashMap<String, Variable> ctx = new HashMap<>();
    private final Context[] sprs;

    // #region containsKey
    @Override
    public boolean containsKey(Object key)
    {
        return ctx.containsKey(key) || spr.containsAccessibleKey(key);
    }

    public boolean containsAccessibleKey(Object key)
    {
        return (ctx.containsKey(key) && ModifierUtils.allowProtectedAccess(get(key).modif))
                || spr.containsAccessibleKey(key);
    }

    public boolean containsPublicKey(Object key)
    {
        return (ctx.containsKey(key) && ModifierUtils.isPublic(get(key).modif))
                || spr.containsPublicKey(key);
    }

    public boolean containsWritableKey(Object key)
    {
        return ctx.containsKey(key) ? !ModifierUtils.isFinal(ctx.get(key).modif)
                : spr.containsAccessibleWritableKey(key);
    }

    public boolean containsAccessibleWritableKey(Object key)
    {
        return ctx.containsKey(key) ? containsWritableKey(key) && containsAccessibleKey(key)
                : spr.containsAccessibleWritableKey(key);
    }

    public boolean containsPublicWritableKey(Object key)
    {
        return ctx.containsKey(key) ? containsWritableKey(key) && containsPublicKey(key)
                : spr.containsPublicWritableKey(key);
    }
    // #endregion

    // #region get
    @Override
    public Variable get(Object key)
    {
        return ctx.containsKey(key) ? ctx.get(key) : spr.getProtected(key);
    }

    public Variable getProtected(Object key)
    {
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            return null;
        return get(key);
    }

    public Variable getPublic(Object key)
    {
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(get(key).modif)) return null;
        return ctx.containsKey(key) ? ctx.get(key) : spr.getPublic(key);
    }
    // #endregion

    // #region put
    @Override
    public Variable put(String key, Variable value)
    {
        return !ctx.containsKey(key) && spr.containsAccessibleKey(key)
                ? spr.putProtected(key, value)
                : ctx.put(key, value);
    }

    public Variable putProtected(String key, Variable value)
    {
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return put(key, value);
    }

    public Variable putPublic(String key, Variable value)
    {
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return !ctx.containsKey(key) && spr.containsPublicKey(key) ? spr.putPublic(key, value)
                : ctx.put(key, value);
    }

    private Variable updateValue(String key, Object value)
    {
        if (!ctx.containsKey(key))
            return ctx.put(key, new Variable(value, ModifierUtils.PROTECTED));
        else
        {
            Variable var = ctx.get(key);
            if (ModifierUtils.isFinal(var.modif))
                throw new IllegalAccessError("Attempted to write to final variable: " + key);
            var.obj = value;
            return var;
        }
    }

    public Variable put(String key, Object value)
    {
        return !ctx.containsKey(key) && spr.containsAccessibleWritableKey(key)
                ? spr.putProtected(key, value)
                : updateValue(key, value);
    }

    public Variable putProtected(String key, Object value)
    {
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return put(key, value);
    }

    public Variable putPublic(String key, Object value)
    {
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return !ctx.containsKey(key) && spr.containsPublicWritableKey(key)
                ? spr.putPublic(key, value)
                : updateValue(key, value);
    }
    // #endregion

    // #region setModif
    private Variable updateModif(String key, int modif)
    {
        if (!containsKey(key)) throw new IllegalArgumentException(
                "Attempted to set modifier of unknown variable: " + key);
        Variable var = ctx.get(key);
        var.modif = modif;
        return var;
    }

    public Variable setModif(String key, int modif)
    {
        return !ctx.containsKey(key) && spr.containsAccessibleKey(key)
                ? spr.setModifProtected(key, modif)
                : updateModif(key, modif);
    }

    public Variable setModifProtected(String key, int modif)
    {
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return setModif(key, modif);
    }

    public Variable setModifPublic(String key, int modif)
    {
        if (!containsPublicKey(key)) throw new IllegalArgumentException(
                "Attempted to set modifier of unknown variable: " + key);
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return !ctx.containsKey(key) && spr.containsPublicKey(key)
                ? spr.setModifProtected(key, modif)
                : updateModif(key, modif);
    }
    // #endregion

    // #region remove
    @Override
    public Variable remove(Object key)
    {
        if (!containsKey(key))
            throw new IllegalArgumentException("Attempted to remove unknown variable: " + key);
        return ctx.containsKey(key) ? ctx.remove(key) : spr.removeProtected(key);
    }

    public Variable removeProtected(Object key)
    {
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(ctx.get(key).modif))
            throw new IllegalAccessError("Attempted to remove inaccessible variable: " + key);
        return remove(key);
    }

    public Variable removePublic(Object key)
    {
        if (!containsPublicKey(key))
            throw new IllegalArgumentException("Attempted to remove unknown variable: " + key);
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(ctx.get(key).modif))
            throw new IllegalAccessError("Attempted to remove inaccessible variable: " + key);
        return ctx.containsKey(key) ? ctx.remove(key) : spr.removePublic(key);
    }
    // #endregion

    // #region Simple Methods
    public void add(String key, Variable value)
    {
        ctx.put(key, value);
    }

    public void add(String key, Object value)
    {
        updateValue(key, value);
    }

    @Override
    public int size()
    {
        return ctx.size() + spr.size();
    }

    @Override
    public boolean isEmpty()
    {
        return ctx.isEmpty() && spr.isEmpty();
    }

    @Override
    public boolean containsValue(Object value)
    {
        return ctx.containsValue(value) || spr.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Variable> m)
    {
        m.entrySet().forEach(e -> put(e.getKey(), e.getValue()));
    }

    public void addAll(Map<? extends String, ? extends Variable> m)
    {
        m.entrySet().forEach(e -> add(e.getKey(), e.getValue()));
    }

    @Override
    public void clear()
    {
        ctx.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return Utils.concat(spr.keySet(), ctx.keySet());
    }

    @Override
    public Collection<Variable> values()
    {
        return Utils.concat(spr.values(), ctx.values());
    }

    @Override
    public Set<Entry<String, Variable>> entrySet()
    {
        return Utils.concat(spr.entrySet(), ctx.entrySet());
    }
    // #endregion

    // #region Context Methods
    public Context push()
    {
        return new Context(this);
    }

    public Context pushNewRoot()
    {
        Context spr = this;
        return new Context(ROOT_CTX)
        {
            @Override
            public Context pop()
            {
                return spr;
            }
        };
    }

    public Context pop()
    {
        return spr;
    }

    public Context relativize()
    {
        Context newCtx = new Context(ROOT_CTX);
        newCtx.addAll(ctx);
        return newCtx;
    }

    public Context resolve(Context ctx)
    {
        Context newCtx = new Context(this);
        newCtx.addAll(ctx);
        return newCtx;
    }

    public Context relativeResolve(Context... ctxs)
    {
        Context newCtx = new Context(this);
        for (Context ctx : ctxs)
            newCtx.addAll(ctx);
        return newCtx;
    }
    // #endregion

    // #region General Methods
    @Override
    public String toString()
    {
        return "{ctx = " + ctx.toString() + ", spr = " + (spr == null ? "<null>" : spr.toString())
                + "}";
    }

    public Context(Context spr)
    {
        this.sprs = new Context[] {Objects.requireNonNull(spr)};
    }

    private Context()
    {
        this.sprs = null;
    }
    // #endregion

    // #region Root Context
    public static final Context ROOT_CTX = new Context()
    {

        @Override
        public boolean containsKey(Object key)
        {
            return false;
        }

        @Override
        public boolean containsAccessibleKey(Object key)
        {
            return false;
        }

        @Override
        public boolean containsPublicKey(Object key)
        {
            return false;
        }

        public boolean containsWritableKey(Object key)
        {
            return false;
        }

        public boolean containsAccessibleWritableKey(Object key)
        {
            return false;
        }

        public boolean containsPublicWritableKey(Object key)
        {
            return false;
        }

        @Override
        public Variable get(Object key)
        {
            return null;
        }

        @Override
        public Variable getProtected(Object key)
        {
            return null;
        }

        @Override
        public Variable getPublic(Object key)
        {
            return null;
        }

        @Override
        public Variable put(String key, Variable value)
        {
            return null;
        }

        @Override
        public Variable putProtected(String key, Variable value)
        {
            return null;
        }

        @Override
        public Variable putPublic(String key, Variable value)
        {
            return null;
        }

        @Override
        public Variable put(String key, Object value)
        {
            return null;
        }

        @Override
        public Variable putProtected(String key, Object value)
        {
            return null;
        }

        @Override
        public Variable putPublic(String key, Object value)
        {
            return null;
        }

        @Override
        public Variable setModif(String key, int modif)
        {
            throw new IllegalArgumentException(
                    "Attempted to set modifier of unknown variable: " + key);
        }

        @Override
        public Variable setModifProtected(String key, int modif)
        {
            throw new IllegalArgumentException(
                    "Attempted to set modifier of unknown variable: " + key);
        }

        @Override
        public Variable setModifPublic(String key, int modif)
        {
            throw new IllegalArgumentException(
                    "Attempted to set modifier of unknown variable: " + key);
        }

        @Override
        public Variable remove(Object key)
        {
            throw new IllegalArgumentException("Attempted to remove unknown variable: " + key);
        }

        @Override
        public Variable removeProtected(Object key)
        {
            throw new IllegalArgumentException("Attempted to remove unknown variable: " + key);
        }

        @Override
        public Variable removePublic(Object key)
        {
            throw new IllegalArgumentException("Attempted to remove unknown variable: " + key);
        }

        @Override
        public void add(String key, Object value)
        {}

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public boolean isEmpty()
        {
            return true;
        }

        @Override
        public boolean containsValue(Object value)
        {
            return false;
        }

        @Override
        public void putAll(Map<? extends String, ? extends Variable> m)
        {}

        @Override
        public void addAll(Map<? extends String, ? extends Variable> m)
        {}

        @Override
        public void clear()
        {}

        @Override
        public Set<String> keySet()
        {
            return Set.of();
        }

        @Override
        public Collection<Variable> values()
        {
            return Set.of();
        }

        @Override
        public Set<Entry<String, Variable>> entrySet()
        {
            return Set.of();
        }
    };
    // #endregion

}
