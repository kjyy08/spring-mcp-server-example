package dev.luigi.mcpserverexample.provider;

import dev.luigi.mcpserverexample.service.RecipeService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolCallbackProviderConfig {
    @Bean
    public ToolCallbackProvider recipeTools(RecipeService recipeService) {
        return MethodToolCallbackProvider.builder().toolObjects(recipeService).build();
    }
}
