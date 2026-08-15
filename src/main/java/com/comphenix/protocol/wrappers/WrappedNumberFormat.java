package com.comphenix.protocol.wrappers;

public class WrappedNumberFormat extends AbstractWrapper {
    protected WrappedNumberFormat(){super(WrappedNumberFormat.class);this.handle=this;}
    protected WrappedNumberFormat(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);}
    public static boolean isSupported(){return true;} public static WrappedNumberFormat fromHandle(Object handle){return handle instanceof WrappedNumberFormat v?v:new WrappedNumberFormat(handle);} public static Blank blank(){return new Blank();} public static Fixed fixed(WrappedChatComponent content){return new Fixed(content);} public static Styled styled(WrappedComponentStyle style){return new Styled(style);}
    public static class Blank extends WrappedNumberFormat { public Blank(){super();} }
    public static class Fixed extends WrappedNumberFormat { private final WrappedChatComponent content; public Fixed(WrappedChatComponent content){super();this.content=content;} public WrappedChatComponent getContent(){return content;} }
    public static class Styled extends WrappedNumberFormat { private final WrappedComponentStyle style; public Styled(WrappedComponentStyle style){super();this.style=style;} public WrappedComponentStyle getStyle(){return style;} }
}
