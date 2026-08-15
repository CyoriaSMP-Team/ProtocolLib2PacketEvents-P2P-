package com.comphenix.protocol.wrappers;

import java.io.StringReader;

public class ComponentParser {
    public static Object deserialize(Object parser,Class<?> componentClass,StringReader reader){if(reader==null)return null;StringBuilder text=new StringBuilder();try{int c;while((c=reader.read())!=-1)text.append((char)c);}catch(java.io.IOException error){throw new IllegalArgumentException("Unable to read component",error);}return WrappedChatComponent.fromJson(text.toString());}
}
