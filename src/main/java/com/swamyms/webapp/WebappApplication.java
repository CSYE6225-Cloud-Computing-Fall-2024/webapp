package com.swamyms.webapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappApplication {

	public static void main(String[] args) {

		// Load environment variables from .env file
		Dotenv dotenv = Dotenv.load();

		// Set system properties to be used in application.properties
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		SpringApplication.run(WebappApplication.class, args);
	}

}
