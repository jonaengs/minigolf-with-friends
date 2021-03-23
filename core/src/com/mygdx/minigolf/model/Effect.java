package com.mygdx.minigolf.model;

enum Constraint {
    STROKES(0),
    USES(0);

    private int amount;

    Constraint(int amount){
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
}

public enum Effect {
    EXPLODING(Constraint.USES),
    NO_COLLISION(Constraint.STROKES);

    private final Constraint constraint;

    Effect(Constraint constraint){
        this.constraint = constraint;

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


}
