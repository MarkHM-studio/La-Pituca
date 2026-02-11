package com.restobar.lapituca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class LapitucaApplication {

	public static void main(String[] args) {
		LocalDateTime hola = LocalDateTime.now();

		System.out.println(hola);

		SpringApplication.run(LapitucaApplication.class, args);
	}
}
