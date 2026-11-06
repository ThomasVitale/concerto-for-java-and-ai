package com.thomasvitale.mousike.scenes;

public enum Medium {

    FILM("Film"),
    GAME("Game"),
    THEATRE("Theatre"),
    TV("TV Series");

    private final String label;

    Medium(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

}
