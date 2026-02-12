package github.maxsuel.agregadordeinvestimentos.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Investment Aggregator API",
                version = "1.0",
                description = "System for managing stock portfolios with real-time integration via Brapi."
        )
)
public class OpenApiConfig {
}
