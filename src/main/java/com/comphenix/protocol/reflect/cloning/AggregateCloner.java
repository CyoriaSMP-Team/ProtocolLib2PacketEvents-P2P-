package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class AggregateCloner implements Cloner {
    public static class BuilderParameters { private InstanceProvider instanceProvider; private Cloner aggregateCloner; public InstanceProvider getInstanceProvider(){return instanceProvider;} public Cloner getAggregateCloner(){return aggregateCloner;} }
    public static class Builder { private final List<Function<BuilderParameters,Cloner>> factories=new ArrayList<>(); private final BuilderParameters parameters=new BuilderParameters(); public Builder(){} public Builder instanceProvider(InstanceProvider p){parameters.instanceProvider=p;return this;} public Builder andThen(Class<? extends Cloner> type){return andThen(p->{try{return type.getDeclaredConstructor().newInstance();}catch(Exception ex){return null;}});} public Builder andThen(Function<BuilderParameters,Cloner> factory){factories.add(factory);return this;} public Builder andThen(com.google.common.base.Function<BuilderParameters,Cloner> factory){if(factory!=null)factories.add(factory::apply);return this;} public AggregateCloner build(){AggregateCloner result=new AggregateCloner();parameters.aggregateCloner=result;if(parameters.instanceProvider==null)parameters.instanceProvider=DefaultInstances.DEFAULT;for(Function<BuilderParameters,Cloner> factory:factories){Cloner value=factory.apply(parameters);if(value!=null)result.cloners.add(value);}return result;} }
    public static final AggregateCloner DEFAULT = newBuilder().andThen(ImmutableDetector.class).andThen(CollectionCloner.class).andThen(FieldCloner.class).build();
    private final List<Cloner> cloners=new ArrayList<>();
    public static Builder newBuilder(){return new Builder();}
    private AggregateCloner(){ }
    public List<Cloner> getCloners(){return Collections.unmodifiableList(cloners);}
    @Override public boolean canClone(Object source){for(Cloner cloner:cloners)if(cloner.canClone(source))return true;return false;}
    @Override public Object clone(Object source){for(Cloner cloner:cloners)if(cloner.canClone(source))return cloner.clone(source);throw new IllegalArgumentException("Cannot clone "+source);}
}
