package com.comphenix.protocol;

import org.bukkit.conversations.*;

class MultipleLinesPrompt extends StringPrompt {
    public interface MultipleConversationCanceller extends ConversationCanceller { boolean cancelBasedOnInput(ConversationContext context,String currentLine); boolean cancelBasedOnInput(ConversationContext context,String currentLine,StringBuilder lines,int lineCount); }
    private final MultipleConversationCanceller endMarker; private final String initialPrompt;
    public MultipleLinesPrompt(String endMarker,String initialPrompt){this(new MultipleWrapper(new ExactCanceller(endMarker)),initialPrompt);} public MultipleLinesPrompt(ConversationCanceller canceller,String initialPrompt){this(new MultipleWrapper(canceller),initialPrompt);} public MultipleLinesPrompt(MultipleConversationCanceller canceller,String initialPrompt){this.endMarker=canceller;this.initialPrompt=initialPrompt;}
    private static final String KEY="multiple_lines_prompt"; private static final String KEY_LAST=KEY+".last_line"; private static final String KEY_LINES=KEY+".linecount";
    public String removeAccumulatedInput(ConversationContext context){Object value=context.getSessionData(KEY);if(value instanceof StringBuilder){context.setSessionData(KEY,null);context.setSessionData(KEY_LINES,null);return value.toString();}return null;}
    public Prompt acceptInput(ConversationContext context,String in){StringBuilder lines=(StringBuilder)context.getSessionData(KEY);Integer count=(Integer)context.getSessionData(KEY_LINES);if(lines==null)context.setSessionData(KEY,lines=new StringBuilder());if(count==null)count=0;context.setSessionData(KEY_LAST,in);context.setSessionData(KEY_LINES,++count);lines.append(in).append('\n');return endMarker.cancelBasedOnInput(context,in,lines,count)?Prompt.END_OF_CONVERSATION:this;}
    public String getPromptText(ConversationContext context){Object last=context.getSessionData(KEY_LAST);return last instanceof String?(String)last:initialPrompt;}
    private static final class ExactCanceller implements ConversationCanceller{private final String marker;ExactCanceller(String marker){this.marker=marker;}public boolean cancelBasedOnInput(ConversationContext c,String in){return marker!=null&&marker.equals(in);}public void setConversation(Conversation c){}public ConversationCanceller clone(){return new ExactCanceller(marker);}}
    private static final class MultipleWrapper implements MultipleConversationCanceller{private final ConversationCanceller delegate;public MultipleWrapper(ConversationCanceller d){delegate=d;}public boolean cancelBasedOnInput(ConversationContext c,String in){return delegate.cancelBasedOnInput(c,in);}public boolean cancelBasedOnInput(ConversationContext c,String in,StringBuilder lines,int count){return cancelBasedOnInput(c,in);}public void setConversation(Conversation c){delegate.setConversation(c);}public MultipleWrapper clone(){return new MultipleWrapper(delegate.clone());}}
}
