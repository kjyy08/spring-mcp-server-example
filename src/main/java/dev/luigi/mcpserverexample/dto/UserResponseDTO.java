package dev.luigi.mcpserverexample.dto;

import dev.luigi.mcpserverexample.enums.Platform;
import dev.luigi.mcpserverexample.enums.Role;
import dev.luigi.mcpserverexample.entity.User;

import java.time.ZonedDateTime;

public record UserResponseDTO(String id, String username, Role role, Platform platform, ZonedDateTime createdAt) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getRole(), user.getPlatform(), user.getCreatedAt());
    }
}
