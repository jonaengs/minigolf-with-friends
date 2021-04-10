package com.mygdx.minigolf.model;

public class Effect {

    private final Power power;
    private final Constraint constraint;

    public Effect(Power power, Constraint constraint){
        this.power = power;
        this.constraint = constraint;
    }

    public Power getPower(){
        return this.power;
    }

    public void setConstraintAmount(int i){
        this.constraint.setAmount(i);
    }

    public void decrementConstraintAmount(){
        this.constraint.decrementAmount();
    }

    public int getConstraintAmount(){
        return this.constraint.getAmount();
    }

    public void setConstraintStart(int start){
        this.constraint.setStart(start);
    }

    public int getConstraintStart(){
        return this.constraint.getStart();
    }


}
