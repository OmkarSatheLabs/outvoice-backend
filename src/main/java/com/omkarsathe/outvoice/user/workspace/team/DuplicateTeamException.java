package com.omkarsathe.outvoice.user.workspace.team;

public class DuplicateTeamException extends RuntimeException {
    public DuplicateTeamException(String message) {
        super("Team " + message + " already exists");
    }
}
