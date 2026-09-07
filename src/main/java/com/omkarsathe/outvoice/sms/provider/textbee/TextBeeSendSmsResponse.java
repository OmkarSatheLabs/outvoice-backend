package com.omkarsathe.outvoice.sms.provider.textbee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TextBeeSendSmsResponse(
        TextBeeData data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextBeeData(
            Boolean success,
            String message,
            String smsBatchId,
            Integer recipientCount,
            Integer successCount,
            Integer failureCount
    ) {
    }

    public boolean success() {
        return data != null &&
                Boolean.TRUE.equals(data.success());
    }

    public String smsBatchId() {
        return data != null ? data.smsBatchId() : null;
    }
}
