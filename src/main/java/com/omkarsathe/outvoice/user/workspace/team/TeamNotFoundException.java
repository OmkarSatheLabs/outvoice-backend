package com.omkarsathe.outvoice.user.workspace.team;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(String id) {
        super("Role with id " + id + " not found");
    }
}
