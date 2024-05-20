package com.example.tricount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class TricountApplication {

	public static void main(String[] args) {
		SpringApplication.run(TricountApplication.class, args);
	}

}
