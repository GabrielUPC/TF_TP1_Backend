package pe.edu.upc.tf_tp1_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:tf_tp1_test;MODE=PostgreSQL",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"jwt.secret=test-only-secret-test-only-secret-test-only-secret-test-only-secret",
		"modelo.ipress.url=http://127.0.0.1:8000"
})
class TfTp1BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
