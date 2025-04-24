package dev.luigi.mcpserverexample.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luigi.mcpserverexample.repository.RecipeRepository;
import org.springframework.context.annotation.Configuration;

/**
 * 레시피 리소스, 프롬프트 MCP 등록 예시
 */
@Configuration
public class RecipeResourcePromptConfig {

    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecipeResourcePromptConfig(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }
//
//    // 1. 레시피 전체 목록 제공 리소스 등록
//    @Bean
//    public List<McpServerFeatures.SyncResourceSpecification> recipeResources() {
//        var recipeListResource = new McpSchema.Resource(
//                "all-recipes",                            // 리소스 이름(ID)
//                "전체 레시피 목록",                          // 설명
//                "서버에 저장된 모든 레시피 정보를 제공합니다."    // 상세설명(선택)
//        );
//
//
//        var allRecipeSpec = new McpServerFeatures.SyncResourceSpecification(recipeListResource, (exchange, request) -> {
//            try {
//                List<Recipe> recipes = recipeRepository.findAll();
//                String json = objectMapper.writeValueAsString(
//                        recipes.stream().map(r -> Map.of(
//                                "id", r.getId(),
//                                "name", r.getName(),
//                                "description", r.getDescription()
//                        )).collect(Collectors.toList())
//                );
//                return new McpSchema.ReadResourceResult(
//                        List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", json))
//                );
//            } catch (Exception e) {
//                throw new RuntimeException("레시피 목록 변환 실패", e);
//            }
//        });
//
//        return List.of(allRecipeSpec);
//    }
//
//    // 2. 레시피 프롬프트 템플릿 등록 (예: 사용자에게 친절하게 안내하는 메시지)
//    @Bean
//    public List<McpServerFeatures.SyncPromptSpecification> recipePrompts() {
//        var prompt = new McpSchema.Prompt(
//                "recipe_greeting", "레시피 안내 인사말",
//                List.of(new McpSchema.PromptArgument("name", "유저 이름", true))
//        );
//
//        var promptSpec = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, req) -> {
//            String name = (String) req.arguments().getOrDefault("name", "친구");
//            String content = String.format("%s님, 오늘은 어떤 레시피가 궁금하신가요? 이름이나 키워드를 입력해 주세요!", name);
//            var userMsg = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(content));
//            return new McpSchema.GetPromptResult("맞춤 인사 메시지", List.of(userMsg));
//        });
//
//        return List.of(promptSpec);
//    }
}