package com.comphenix.protocol;

import com.google.common.collect.Range;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

final class RangeParser {
    private RangeParser(){}
    public static List<Range<Integer>> getRanges(String text,Range<Integer> legal){return getRanges(new ArrayDeque<>(Collections.singleton(text)),legal);}
    public static List<Range<Integer>> getRanges(Deque<String> input,Range<Integer> legal){List<Range<Integer>> out=new ArrayList<>();while(!input.isEmpty()){String token=input.poll();if(token==null||token.isBlank())continue;for(String piece:token.split(",")){String[] parts=piece.trim().split("-");try{Range<Integer> range=parts.length==1?Range.singleton(Integer.parseInt(parts[0])):Range.closed(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));if(!legal.encloses(range))throw new IllegalArgumentException(range+" is outside "+legal);out.add(range);}catch(NumberFormatException ignored){break;}}}return out;}
}
