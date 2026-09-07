package com.omkarsathe.outvoice.user.workspace.role;

public class DuplicateRoleException extends RuntimeException {
    public DuplicateRoleException(String message) {
        super("Role " + message + " already exists");
    }
}
