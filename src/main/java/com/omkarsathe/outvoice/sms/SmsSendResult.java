package com.omkarsathe.outvoice.sms;

public record SmsSendResult(
        boolean success,
        String providerMessageId,
        String providerBatchId
) {
}
