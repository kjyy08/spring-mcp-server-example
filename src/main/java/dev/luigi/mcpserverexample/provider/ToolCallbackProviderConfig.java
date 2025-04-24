package dev.luigi.mcpserverexample.provider;

import dev.luigi.mcpserverexample.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ToolCallbackProviderConfig {
    private final UserService userService;

    @Bean
    public ToolCallbackProvider recipeTools() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(userService)
                .build();
    }
}
