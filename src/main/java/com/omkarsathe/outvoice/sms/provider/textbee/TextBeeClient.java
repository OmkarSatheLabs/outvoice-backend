package com.omkarsathe.outvoice.sms.provider.textbee;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TextBeeClient {

    private final TextBeeProperties properties;

    private final RestClient restClient =
            RestClient.builder()
                    .baseUrl("https://api.textbee.dev")
                    .build();

    public TextBeeSendSmsResponse sendSms(
            TextBeeSendSmsRequest request
    ) {

        return restClient.post()
                .uri("/api/v1/gateway/send-sms")
                .header("x-api-key", properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TextBeeSendSmsResponse.class);
    }
}
