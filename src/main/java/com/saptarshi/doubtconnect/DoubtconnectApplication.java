package com.saptarshi.doubtconnect;

import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling

public class DoubtconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoubtconnectApplication.class, args);
	}

}