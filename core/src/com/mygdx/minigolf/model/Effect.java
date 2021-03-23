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
    // The exploding effect can be attained by a player and used to send another player back to spawn on collision
    EXPLODING(Constraint.USES),
    // The no_collision effect makes the player unable to colloide with anything expect world bounderies
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
