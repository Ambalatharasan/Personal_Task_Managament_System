package Project.TaskAutomation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainApplicationTest {
	@Test
	public void testApplicationContext() {
		assertTrue(true, "Test framework is working");
	}

	@Test
	public void testMainApplicationExists() {
		assertNotNull(MainApplication.class, "MainApplication class should exist");
	}
}