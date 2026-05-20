package com.omkarsathe.outvoice.organization;

/**
 * Fine-grained permissions assignable to MEMBER users.
 * OWNER and SUPER_USER receive all permissions automatically — this enum only applies to MEMBERs.
 */
public enum Permission {
    INVOICE_READ,
    INVOICE_WRITE,
    INVOICE_DELETE,
    INVOICE_EXPORT,
    CLIENT_READ,
    CLIENT_WRITE,
    CLIENT_DELETE,
    REPORT_READ,
    REPORT_EXPORT,
    MANAGE_MEMBERS
}
