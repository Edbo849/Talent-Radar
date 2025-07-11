package com.talentradar.model;

public enum Position {
    GOALKEEPER("Goalkeeper"),
    RIGHT_BACK("Right Back"),
    LEFT_BACK("Left Back"),
    CENTER_BACK("Center Back"),
    RIGHT_WING_BACK("Right Wing Back"),
    LEFT_WING_BACK("Left Wing Back"),
    DEFENSIVE_MIDFIELDER("Defensive Midfielder"),
    CENTRAL_MIDFIELDER("Central Midfielder"),
    ATTACKING_MIDFIELDER("Attacking Midfielder"),
    RIGHT_WINGER("Right Winger"),
    LEFT_WINGER("Left Winger"),
    STRIKER("Striker"),
    CENTER_FORWARD("Center Forward");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
