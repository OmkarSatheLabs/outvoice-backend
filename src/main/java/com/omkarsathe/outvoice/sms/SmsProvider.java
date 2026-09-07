package com.omkarsathe.outvoice.sms;

public interface SmsProvider {

    SmsSendResult send(SmsMessage message);
}
