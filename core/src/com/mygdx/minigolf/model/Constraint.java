package com.mygdx.minigolf.model;


public class Constraint{
    private int amount;
    private final ConstraintType type;

    //only relevant if constrainttype is strokes
    private int start;

    public Constraint(ConstraintType type, int amount){
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
