package com.abc.pricing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Configuration for R2DBC database initialization.
 */
@Configuration
public class DatabaseConfig {

	@Bean
//	@Profile("local")
	public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
		 ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
	        initializer.setConnectionFactory(connectionFactory);
	        
	        // Ensure this is the R2DBC version of ResourceDatabasePopulator
	        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
	        
	        initializer.setDatabasePopulator(populator);
	        return initializer;
	}
}
