package com.omkarsathe.outvoice.mail;

import java.util.List;
import java.util.Map;

public record MailRequest (
        String to,
        MailType type,
        Map<String, Object> variables,
        List<String> attachmentPaths
) {
    public MailRequest(String to, MailType type, Map<String, Object> variables) {
        this(to, type, variables, List.of());
    }
}
