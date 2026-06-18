package com.covacova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CovacovaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CovacovaApplication.class, args);
	}

}
