package com.mygdx.minigolf.model;

class Constraint {

    private int amount;
    private final ConstraintType type;

    //only relevant if constrainttype is strokes
    private int start;

    Constraint(ConstraintType type, int amount){
        this.type = type;
        this.amount = amount;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public void decrementAmount(){
        this.amount -= 1;
    }

    public int getAmount(){
        return amount;
    }

    public void setStart(int start){
        this.start = start;
    }

    public int getStart(){
        return this.start;
    }
}

public class Effect {

    private final Power power;
    private final Constraint constraint;

    Effect(Power power, Constraint constraint){
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
