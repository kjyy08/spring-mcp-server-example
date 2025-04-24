package dev.luigi.mcpserverexample.service;

import dev.luigi.mcpserverexample.entity.Recipe;
import dev.luigi.mcpserverexample.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Tool(name = "find_one_recipe", description = "레시피 이름을 입력받아서 레시피 정보를 사용자에게 제공하려면 이 도구를 사용하세요.")
    public Recipe findByName(String name){
        return recipeRepository.findByName(name);
    }

    @Tool(name = "find_all_recipes", description = "전체 레시피 목록을 조회하는 행동이 필요하다면 이 도구를 사용하세요.")
    public List<Recipe> findAll(){
        return recipeRepository.findAll();
    }

    @Tool(name = "save_recipe", description = "유저가 레시피를 추가하기를 원한다면 이 도구를 사용하세요.")
    public void save(@ToolParam(description = "레시피 이름") String name,
                     @ToolParam(description = "레시피 정보") String description){
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setDescription(description);
        recipeRepository.save(recipe);
    }
}
