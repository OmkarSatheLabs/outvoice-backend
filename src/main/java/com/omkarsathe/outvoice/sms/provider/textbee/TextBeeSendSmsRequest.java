package com.omkarsathe.outvoice.sms.provider.textbee;

import java.util.List;

public record TextBeeSendSmsRequest(
        List<String> recipients,
        String message
) {
}
