package com.coolspy3.stackflow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Context implements Map<String, Variable>
{

    public static final Context ROOT_CTX = new Context();

    private final Map<String, Variable> ctx;
    private final Context[] sprs;

    // #region containsKey
    @Override
    public boolean containsKey(Object key)
    {
        return ctx.containsKey(key)
                || getFirstMatching(sprs, spr -> spr.containsAccessibleKey(key)) != null;
    }

    public boolean containsAccessibleKey(Object key)
    {
        return (ctx.containsKey(key) && ModifierUtils.allowProtectedAccess(get(key).modif))
                || getFirstMatching(sprs, spr -> spr.containsAccessibleKey(key)) != null;
    }

    public boolean containsPublicKey(Object key)
    {
        return (ctx.containsKey(key) && ModifierUtils.isPublic(get(key).modif))
                || getFirstMatching(sprs, spr -> spr.containsPublicKey(key)) != null;
    }

    public boolean containsWritableKey(Object key)
    {
        return ctx.containsKey(key) ? !ModifierUtils.isFinal(ctx.get(key).modif)
                : getFirstMatching(sprs, spr -> spr.containsAccessibleWritableKey(key)) != null;
    }

    public boolean containsAccessibleWritableKey(Object key)
    {
        return ctx.containsKey(key) ? containsWritableKey(key) && containsAccessibleKey(key)
                : getFirstMatching(sprs, spr -> spr.containsAccessibleWritableKey(key)) != null;
    }

    public boolean containsPublicWritableKey(Object key)
    {
        return ctx.containsKey(key) ? containsWritableKey(key) && containsPublicKey(key)
                : getFirstMatching(sprs, spr -> spr.containsPublicWritableKey(key)) != null;
    }
    // #endregion

    // #region get
    @Override
    public Variable get(Object key)
    {
        if (!containsKey(key)) return null;
        if (ctx.containsKey(key)) return ctx.get(key);
        return getFirstMatching(sprs, spr -> spr.containsAccessibleKey(key)).get(key);
    }

    public Variable getProtected(Object key)
    {
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            return null;
        return get(key);
    }

    public Variable getPublic(Object key)
    {
        if (!containsPublicKey(key)) return null;
        if (ctx.containsKey(key)) return ctx.get(key);
        return getFirstMatching(sprs, spr -> spr.containsPublicKey(key)).get(key);
    }
    // #endregion

    // #region put
    @Override
    public Variable put(String key, Variable value)
    {
        Context spr = getFirstMatching(sprs, sCtx -> sCtx.containsAccessibleKey(key));
        return !ctx.containsKey(key) && spr != null ? spr.putProtected(key, value)
                : ctx.put(key, value);
    }

    public Variable putProtected(String key, Variable value)
    {
        if (!containsAccessibleWritableKey(key)) throw new IllegalArgumentException(
                "Attempted to write to non-existent variable: " + key);
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return put(key, value);
    }

    public Variable putPublic(String key, Variable value)
    {
        if (!containsPublicWritableKey(key)) throw new IllegalArgumentException(
                "Attempted to write to non-existent variable: " + key);
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        Context spr = getFirstMatching(sprs, sCtx -> sCtx.containsPublicKey(key));
        return !ctx.containsKey(key) && spr != null ? spr.putPublic(key, value)
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
        Context spr = getFirstMatching(sprs, sCtx -> sCtx.containsAccessibleWritableKey(key));
        return !ctx.containsKey(key) && spr != null ? spr.putProtected(key, value)
                : updateValue(key, value);
    }

    public Variable putProtected(String key, Object value)
    {
        if (!containsAccessibleWritableKey(key)) throw new IllegalArgumentException(
                "Attempted to write to non-existent variable: " + key);
        if (ctx.containsKey(key) && !ModifierUtils.allowProtectedAccess(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return put(key, value);
    }

    public Variable putPublic(String key, Object value)
    {
        if (!containsPublicWritableKey(key)) throw new IllegalArgumentException(
                "Attempted to write to non-existent variable: " + key);
        Context spr = getFirstMatching(sprs, sCtx -> sCtx.containsPublicWritableKey(key));
        if (ctx.containsKey(key) && !ModifierUtils.isPublic(get(key).modif))
            throw new IllegalAccessError("Attempted to write to inaccessible variable: " + key);
        return !ctx.containsKey(key) && spr != null ? spr.putPublic(key, value)
                : updateValue(key, value);
    }
    // #endregion

    // #region setModif
    private Variable updateModif(String key, int modif)
    {
        if (!ctx.containsKey(key)) throw new IllegalArgumentException(
                "Attempted to set modifier of unknown variable: " + key);
        Variable var = ctx.get(key);
        var.modif = modif;
        return var;
    }

    public Variable setModif(String key, int modif)
    {
        Context spr = getFirstMatching(sprs, sCtx -> sCtx.containsAccessibleKey(key));
        return !ctx.containsKey(key) && spr != null ? spr.setModifProtected(key, modif)
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
        Context spr = getFirstMatching(sprs, sCtx -> sCtx.containsPublicKey(key));
        return !ctx.containsKey(key) && spr != null ? spr.setModifPublic(key, modif)
                : updateModif(key, modif);
    }
    // #endregion

    // #region remove
    @Override
    public Variable remove(Object key)
    {
        if (!containsKey(key))
            throw new IllegalArgumentException("Attempted to remove unknown variable: " + key);
        return ctx.containsKey(key) ? ctx.remove(key)
                : getFirstMatching(sprs, spr -> spr.containsAccessibleKey(key)).remove(key);
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
        return ctx.containsKey(key) ? ctx.remove(key)
                : getFirstMatching(sprs, spr -> spr.containsPublicKey(key)).removePublic(key);
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
        int size = ctx.size();
        for (Context spr : sprs)
            size += spr.size();
        return size;
    }

    @Override
    public boolean isEmpty()
    {
        return isEmpty() && getFirstMatching(sprs, Context::isEmpty) == null;
    }

    @Override
    public boolean containsValue(Object value)
    {
        return ctx.containsValue(value)
                || getFirstMatching(sprs, ctx -> ctx.containsValue(value)) != null;
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
        return Utils.concat(ctx.keySet(),
                Utils.concat(Utils.transformArray(sprs, Context::keySet, Set[]::new)));
    }

    @Override
    public Collection<Variable> values()
    {
        return Utils.concat(ctx.values(),
                Utils.concat(Utils.transformArray(sprs, Context::values, Set[]::new)));
    }

    @Override
    public Set<Entry<String, Variable>> entrySet()
    {
        return Utils.concat(ctx.entrySet(),
                Utils.concat(Utils.transformArray(sprs, Context::entrySet, Set[]::new)));
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
        return sprs[0];
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

    public Context resolveAsTail(Context... ctxs)
    {
        Context[] sprs = new Context[ctxs.length + 1];
        if (ctxs.length != 0) System.arraycopy(ctxs, 0, sprs, 0, ctxs.length);
        sprs[sprs.length - 1] = this;
        return new Context(sprs);
    }

    public Context resolveAsHead(Context... ctxs)
    {
        Context[] sprs = new Context[ctxs.length + 1];
        sprs[0] = this;
        if (ctxs.length != 0) System.arraycopy(ctxs, 1, sprs, 0, ctxs.length);
        return new Context(sprs);
    }
    // #endregion

    // #region General Methods
    @Override
    public String toString()
    {
        return "{ctx = " + ctx.toString() + ", sprs = " + Arrays.toString(sprs) + "}";
    }

    public Context(Context... sprs)
    {
        this.sprs = Utils.transformArray(sprs, Objects::requireNonNull, Context[]::new);
        this.ctx = new HashMap<>();
    }

    private Context()
    {
        this.sprs = new Context[0];
        this.ctx = Collections.unmodifiableMap(new HashMap<>());
    }
    // #endregion

    private static final Context getFirstMatching(Context[] sprs,
            Function<Context, Boolean> matcher)
    {
        for (Context spr : sprs)
            if (matcher.apply(spr)) return spr;
        return null;
    }

}
