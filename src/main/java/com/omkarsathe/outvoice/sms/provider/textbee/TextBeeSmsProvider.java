package com.omkarsathe.outvoice.sms.provider.textbee;

import com.omkarsathe.outvoice.sms.SmsMessage;
import com.omkarsathe.outvoice.sms.SmsProvider;
import com.omkarsathe.outvoice.sms.SmsSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TextBeeSmsProvider implements SmsProvider {

    private final TextBeeClient textBeeClient;

    @Override
    public SmsSendResult send(SmsMessage message) {

        TextBeeSendSmsRequest request =
                new TextBeeSendSmsRequest(
                        List.of(message.recipient()),
                        message.message()
                );

        TextBeeSendSmsResponse response =
                textBeeClient.sendSms(request);

        return new SmsSendResult(
                response.success(),
                null,
                response.smsBatchId()
        );
    }
}
