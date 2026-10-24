package dev.tanguy.game.jeopardy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Starts the whole app (needs the local Postgres). Clerk's keys are only fetched on the first request. */
@SpringBootTest(properties = "app.security.clerk.issuer=https://test-instance.clerk.accounts.dev")
class JeopardyApplicationTests {

	@Test
	void contextLoads() {
	}

}
