package dev.luigi.mcpserverexample.dto;

import dev.luigi.mcpserverexample.enums.Platform;
import dev.luigi.mcpserverexample.enums.Role;

public record UserRequestDTO(String username, Role role, Platform platform) {
}
