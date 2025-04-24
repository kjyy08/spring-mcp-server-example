package dev.luigi.mcpserverexample.repository;

import dev.luigi.mcpserverexample.enums.Platform;
import dev.luigi.mcpserverexample.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    List<User> findByPlatform(Platform platform);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);

}
