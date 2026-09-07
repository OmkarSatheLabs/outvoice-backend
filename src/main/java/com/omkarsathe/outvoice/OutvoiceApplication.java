package com.omkarsathe.outvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OutvoiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OutvoiceApplication.class, args);
	}

}
