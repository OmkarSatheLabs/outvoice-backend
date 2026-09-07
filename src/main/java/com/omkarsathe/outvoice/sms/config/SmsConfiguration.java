package com.omkarsathe.outvoice.sms.config;

import com.omkarsathe.outvoice.sms.provider.textbee.TextBeeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TextBeeProperties.class)
public class SmsConfiguration {
}
