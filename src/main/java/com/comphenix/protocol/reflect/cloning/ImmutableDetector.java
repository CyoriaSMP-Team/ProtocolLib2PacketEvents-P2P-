package com.comphenix.protocol.reflect.cloning;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

public class ImmutableDetector implements Cloner {
    @Override public boolean canClone(Object source){return source!=null&&isImmutable(source.getClass());}
    public static boolean isImmutable(Class<?> type){return type!=null&&!type.isArray()&&(type.isPrimitive()||type.isEnum()||type==String.class||Number.class.isAssignableFrom(type)||type==Boolean.class||type==Character.class||type==UUID.class||type==Locale.class||type==BigInteger.class||type==BigDecimal.class||type==URI.class||type==URL.class);}
    @Override public Object clone(Object source){if(!canClone(source))throw new IllegalArgumentException("Not immutable");return source;}
}
