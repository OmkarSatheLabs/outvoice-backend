package com.omkarsathe.outvoice.country;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
public class GeoIpService {

    private DatabaseReader databaseReader;

    @PostConstruct
    public void init() throws IOException {
        databaseReader = new DatabaseReader.Builder(
                new ClassPathResource("GeoLite2-Country.mmdb").getInputStream()
        ).build();
    }

    public String getCountryCode(String ipAddress) {
        if ("127.0.0.1".equals(ipAddress)
                || "::1".equals(ipAddress)) {
            return "IN"; // dev default
        }

        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CountryResponse response = databaseReader.country(ip);
            return response.country().isoCode(); // e.g. IN, US
        } catch (Exception ex) {
            return null;
        }
    }
}
