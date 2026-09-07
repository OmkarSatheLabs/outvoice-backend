package com.omkarsathe.outvoice.sms.provider.textbee;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sms.textbee")
@Getter
@Setter
public class TextBeeProperties {

    private String baseUrl;
    private String apiKey;
    private String deviceId;
}
