package dev.tanguy.game.jeopardy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JeopardyApplication {

	static void main(String[] args) {
		SpringApplication.run(JeopardyApplication.class, args);
	}

}
