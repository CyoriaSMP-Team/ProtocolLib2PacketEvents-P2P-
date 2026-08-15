package com.comphenix.protocol.timing;

public class StatisticsStream {
    private int count; private double mean; private double m2; private double min=Double.POSITIVE_INFINITY; private double max=Double.NEGATIVE_INFINITY;
    public StatisticsStream() { }
    public StatisticsStream(StatisticsStream other) { synchronized(other){count=other.count;mean=other.mean;m2=other.m2;min=other.min;max=other.max;} }
    public synchronized void observe(double value){count++;double delta=value-mean;mean+=delta/count;m2+=delta*(value-mean);min=Math.min(min,value);max=Math.max(max,value);}
    public synchronized double getMean(){checkCount();return mean;}
    public synchronized double getVariance(){checkCount();return count < 2 ? 0.0 : m2/(count-1);}
    public double getStandardDeviation(){return Math.sqrt(getVariance());}
    public synchronized double getMinimum(){checkCount();return min;}
    public synchronized double getMaximum(){checkCount();return max;}
    public synchronized StatisticsStream add(StatisticsStream other){
        if(other==null||other.count==0)return new StatisticsStream(this);
        if(count==0)return new StatisticsStream(other);
        StatisticsStream result=new StatisticsStream();
        double delta=other.mean-mean; double total=(double)count+other.count;
        result.count=count+other.count;
        result.mean=mean+delta*(other.count/total);
        result.m2=m2+other.m2+delta*delta*count*other.count/total;
        result.min=Math.min(min,other.min); result.max=Math.max(max,other.max);
        return result;
    }
    public synchronized int getCount(){return count;}
    private void checkCount(){if(count==0)throw new IllegalStateException("No observations in stream.");}
    public synchronized String toString(){return count==0?"StatisticsStream [Nothing recorded]":String.format("StatisticsStream [Average: %.3f, SD: %.3f, Min: %.3f, Max: %.3f, Count: %s]",getMean(),getStandardDeviation(),getMinimum(),getMaximum(),count);}
}
