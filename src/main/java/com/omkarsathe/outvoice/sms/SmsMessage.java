package com.omkarsathe.outvoice.sms;

public record SmsMessage(
        String recipient,
        String message
) {
}
