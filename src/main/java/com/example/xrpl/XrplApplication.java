package com.example.xrpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.modulith.Modulithic;

@Modulithic
@SpringBootApplication(scanBasePackages = "com.example.xrpl")
public class XrplApplication {

	public static void main(String[] args) {
		SpringApplication.run(XrplApplication.class, args);
	}

}
