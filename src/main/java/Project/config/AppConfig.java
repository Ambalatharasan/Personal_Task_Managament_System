package Project.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages = "Project")
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class AppConfig {

	@Value("${mail.host:smtp.gmail.com}")
	private String mailHost;

	@Value("${mail.port:587}")
	private int mailPort;

	@Value("${mail.username:}")
	private String mailUsername;

	@Value("${mail.password:}")
	private String mailPassword;

	@Value("${spring.datasource.url:jdbc:h2:mem:taskdb2;DB_CLOSE_DELAY=-1}")
	private String dbUrl;

	@Value("${spring.datasource.username:sa}")
	private String dbUsername;

	@Value("${spring.datasource.password:}")
	private String dbPassword;

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(dbUrl);
		config.setUsername(dbUsername);
		config.setPassword(dbPassword);
		config.setDriverClassName("org.h2.Driver");
		config.setMaximumPoolSize(10);
		config.setMinimumIdle(2);
		config.setConnectionTimeout(30000);
		config.setIdleTimeout(600000);
		config.setMaxLifetime(1800000);

		return new HikariDataSource(config);
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource);

		try {
			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
			ClassPathResource resource = new ClassPathResource("schema.sql");

			if (resource.exists()) {
				populator.addScript(resource);
				populator.setContinueOnError(true);
				initializer.setDatabasePopulator(populator);
				System.out.println("->schema.sql loaded successfully");
			} else {
				System.out.println("!!schema.sql not found at classpath:schema.sql");
			}
		} catch (Exception e) {
			System.err.println("!Warning: Could not load schema.sql - " + e.getMessage());
		}

		return initializer;
	}

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailHost);
		mailSender.setPort(mailPort);
		mailSender.setUsername(mailUsername);
		mailSender.setPassword(mailPassword);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.debug", "false");

		return mailSender;
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}