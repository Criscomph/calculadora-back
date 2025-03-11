package com.application.main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.emprestimo.calculator")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
