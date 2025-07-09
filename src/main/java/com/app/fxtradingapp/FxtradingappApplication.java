package com.app.fxtradingapp;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class FxtradingappApplication {

	public static void main(String[] args) {
//		String base64Key = Encoders.BASE64.encode(
//				Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
//		);
//		System.out.println("Your Base64 key: " + base64Key);
		SpringApplication.run(FxtradingappApplication.class, args);
	}

}
