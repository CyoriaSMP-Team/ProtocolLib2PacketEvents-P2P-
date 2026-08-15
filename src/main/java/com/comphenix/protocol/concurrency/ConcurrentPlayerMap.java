package com.comphenix.protocol.concurrency;

import com.google.common.base.Function;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.entity.Player;

/** Concurrent map keyed by a stable player identity, without retaining a server player strongly as the key. */
public class ConcurrentPlayerMap<T> extends AbstractMap<Player,T> implements ConcurrentMap<Player,T> {
    public enum PlayerKey implements Function<Player,Object>{ADDRESS{public Object apply(Player p){return p==null?null:p.getAddress();}},NAME{public Object apply(Player p){return p==null?null:p.getName();}}}
    private final Function<Player,Object> keyFunction;
    private final ConcurrentMap<Object,T> values=new ConcurrentHashMap<>();
    private final ConcurrentMap<Object,Player> players=new ConcurrentHashMap<>();
    public static <T> ConcurrentPlayerMap<T> usingAddress(){return new ConcurrentPlayerMap<>(PlayerKey.ADDRESS);}
    public static <T> ConcurrentPlayerMap<T> usingName(){return new ConcurrentPlayerMap<>(PlayerKey.NAME);}
    private ConcurrentPlayerMap(PlayerKey key){this.keyFunction=key;}
    public ConcurrentPlayerMap(Function<Player,Object> key){if(key==null)throw new NullPointerException();this.keyFunction=key;}
    private Object key(Object object){return object instanceof Player?keyFunction.apply((Player)object):null;}
    private Object require(Player p){Object key=key(p);if(key==null)throw new NullPointerException("player key cannot be null");players.put(key,p);return key;}
    @Override public T put(Player p,T v){return values.put(require(p),v);}
    @Override public T putIfAbsent(Player p,T v){return values.putIfAbsent(require(p),v);}
    @Override public T replace(Player p,T v){return values.replace(require(p),v);}
    @Override public boolean replace(Player p,T old,T v){return values.replace(require(p),old,v);}
    @Override public T get(Object p){Object key=key(p);return key==null?null:values.get(key);}
    @Override public boolean containsKey(Object p){Object key=key(p);return key!=null&&values.containsKey(key);}
    @Override public T remove(Object p){Object key=key(p);if(key==null)return null;players.remove(key);return values.remove(key);}
    @Override public boolean remove(Object p,Object v){Object key=key(p);if(key==null)return false;boolean removed=values.remove(key,v);if(removed)players.remove(key);return removed;}
    @Override public int size(){return values.size();}
    @Override public boolean isEmpty(){return values.isEmpty();}
    @Override public void clear(){values.clear();players.clear();}
    @Override public Set<Entry<Player,T>> entrySet(){return new AbstractSet<>(){public int size(){return values.size();}public Iterator<Entry<Player,T>> iterator(){Iterator<Entry<Object,T>> it=values.entrySet().iterator();return new Iterator<>(){public boolean hasNext(){return it.hasNext();}public Entry<Player,T> next(){Entry<Object,T> e=it.next();return new SimpleEntry<>(players.get(e.getKey()),e.getValue());}};}};}
}
