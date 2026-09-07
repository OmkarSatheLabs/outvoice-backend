package com.omkarsathe.outvoice.user.workspace.role;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String id) {
        super("Role with id " + id + " not found");
    }
}
