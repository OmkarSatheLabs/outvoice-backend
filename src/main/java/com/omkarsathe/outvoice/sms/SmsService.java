package com.omkarsathe.outvoice.sms;

import com.omkarsathe.outvoice.sms.outbox.SmsOutbox;
import com.omkarsathe.outvoice.sms.outbox.SmsOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsOutboxRepository smsOutboxRepository;

    public void queue(SmsMessage message) {

        SmsOutbox outbox = SmsOutbox.pending(
                message.recipient(),
                message.message()
        );

        smsOutboxRepository.save(outbox);
    }
}
