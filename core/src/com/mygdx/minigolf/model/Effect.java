package com.mygdx.minigolf.model;

public abstract class Effect {

    protected final Constraint constraint;

    protected Effect(Constraint constraint){
        this.constraint = constraint;
    }

    public Constraint getConstraint(){
        return this.constraint;
    }

    public static class ExplodingEffect extends Effect{
        public ExplodingEffect() {
            super(new UseConstraint(1));
        }
    }

    public static class NoCollisionEffect extends Effect{
        public NoCollisionEffect(){
            super(new StrokeConstraint(3));
        }
    }
}
