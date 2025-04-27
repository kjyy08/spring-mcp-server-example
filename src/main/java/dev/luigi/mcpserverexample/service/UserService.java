package dev.luigi.mcpserverexample.service;

import dev.luigi.mcpserverexample.dto.UserRequestDTO;
import dev.luigi.mcpserverexample.dto.UserResponseDTO;
import dev.luigi.mcpserverexample.entity.User;
import dev.luigi.mcpserverexample.enums.Platform;
import dev.luigi.mcpserverexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Tool(name = "getAllUsers", description = "모든 사용자 정보를 조회합니다. 사용자 목록이 필요할 때 이 도구를 사용하세요. 예: '모든 사용자 목록 보여줘', '시스템에 등록된 사용자가 몇 명인지 알려줘', '전체 사용자 정보를 조회해줘'. 페이지네이션이 필요한 경우 getPaginatedUsers 도구를 대신 사용하세요.")
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    @Tool(name = "getPaginatedUsers", description = "페이지네이션과 정렬 옵션을 적용하여 사용자 목록을 조회합니다. 많은 수의 사용자가 있을 때 일부만 가져오거나 특정 순서로 정렬하고 싶을 때 사용하세요. 예: '사용자 목록을 페이지별로 보여줘', '사용자를 이름 순으로 정렬해서 보여줘', '첫 10명의 사용자만 조회해줘', '사용자 목록 두 번째 페이지 보여줘'")
    public List<UserResponseDTO> findAllUsers(
            @ToolParam(description = "조회할 페이지 번호(0부터 시작). 예: 첫 페이지는 0, 두 번째 페이지는 1")
            @RequestParam(defaultValue = "0")
            int pageNo,
            @ToolParam(description = "한 페이지에 표시할 사용자 수. 일반적으로 10, 20, 50 등의 값을 사용")
            @RequestParam(defaultValue = "10")
            int pageSize,
            @ToolParam(description = "정렬할 속성 이름. User 엔티티의 필드명을 사용해야 함. 가능한 값: 'username', 'role', 'platform', 'createdAt' 등")
            @RequestParam(defaultValue = "createdAt")
            String properties,
            @ToolParam(description = "정렬 방향. 'ASC'(오름차순) 또는 'DESC'(내림차순) 중 하나를 입력. 예: Sort.Direction.ASC 또는 Sort.Direction.DESC")
            @RequestParam(defaultValue = "DESC")
            Sort.Direction direction) {
        Sort sortType = Sort.by(direction, properties);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortType);

        return userRepository.findAll(pageable).stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    @Tool(name = "getUserById", description = "사용자 ID로 특정 사용자 정보를 조회합니다. 고유 식별자(ID)를 알고 있을 때 특정 사용자의 상세 정보가 필요한 경우 사용하세요. 예: '고유 ID가 abc123인 사용자 정보 보여줘', '특정 ID로 사용자 찾아줘', '이 ID를 가진 사용자의 상세 정보 알려줘'")
    public UserResponseDTO findUserById(
            @ToolParam(description = "조회할 사용자의 고유 ID. 시스템이 자동으로 생성한 UUID 형식의 문자열입니다. 예: '550e8400-e29b-41d4-a716-446655440000'")
            String id) {
        return UserResponseDTO.from(userRepository.findById(id)
                .orElseThrow()
        );
    }

    @Tool(name = "getUserByUsername", description = "사용자 이름으로 특정 사용자 정보를 조회합니다. 사용자 이름을 알고 있을 때 해당 사용자의 모든 정보를 가져올 때 사용하세요. 사용자 ID보다 이름으로 찾는 것이 더 직관적인 경우에 유용합니다. 예: '홍길동 사용자 정보 보여줘', '사용자 이름으로 계정 정보 검색해줘', '특정 사용자의 상세 정보 알려줘'")
    public UserResponseDTO findUserByUsername(
            @ToolParam(description = "조회할 사용자의 이름. 사용자가 회원가입 시 입력한 고유한 아이디입니다. 대소문자를 구분합니다. 예: 'hong123', 'admin_user'")
            String username) {
        return UserResponseDTO.from(userRepository.findByUsername(username)
                .orElseThrow()
        );
    }

    @Tool(name = "getUsersByPlatform", description = "특정 플랫폼에 속한 모든 사용자를 조회합니다. 소셜 로그인 플랫폼별로 사용자를 분류하여 볼 때 사용하세요. 예: '카카오로 가입한 사용자 목록 보여줘', '네이버 계정으로 로그인한 사용자들이 몇 명인지 알려줘', '구글 플랫폼 사용자만 조회해줘'")
    public List<UserResponseDTO> findUsersByPlatform(
            @ToolParam(description = "사용자가 속한 플랫폼 유형. 다음 값 중 하나여야 합니다: 'KAKAO'(카카오), 'NAVER'(네이버), 'GOOGLE'(구글). 예: Platform.KAKAO, Platform.NAVER, Platform.GOOGLE")
            Platform platform) {
        return userRepository.findByPlatform(platform).stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    @Tool(name = "removeUserById", description = "사용자 ID를 기준으로 사용자를 삭제합니다. 특정 ID의 사용자를 시스템에서 완전히 제거해야 할 때 사용하세요. 이 작업은 되돌릴 수 없으니 신중하게 사용해야 합니다. 예: '아이디가 abc123인 사용자 삭제해줘', '특정 ID를 가진 계정을 제거해줘', '시스템에서 이 사용자를 완전히 제거해줘'")
    @Transactional
    public void deleteUserById(
            @ToolParam(description = "삭제할 사용자의 고유 ID. 시스템이 자동으로 생성한 UUID 형식의 문자열입니다. 예: '550e8400-e29b-41d4-a716-446655440000'")
            String id) {
        userRepository.deleteById(id);
    }

    @Tool(name = "removeUserByUsername", description = "사용자 이름을 기준으로 사용자를 삭제합니다. 사용자 ID를 모르지만 이름은 알고 있을 때 계정을 제거하려는 경우 사용하세요. 이 작업은 되돌릴 수 없으니 신중하게 사용해야 합니다. 예: '홍길동 사용자 계정 삭제해줘', '특정 사용자 이름으로 등록된 계정 제거해줘', '이 사용자 이름을 가진 계정을 시스템에서 지워줘'")
    @Transactional
    public void deleteUserByUsername(
            @ToolParam(description = "삭제할 사용자의 이름. 사용자가 회원가입 시 입력한 고유한 아이디입니다. 대소문자를 구분합니다. 예: 'hong123', 'admin_user'")
            String username) {
        userRepository.deleteByUsername(username);
    }

    @Tool(name = "createUser", description = "새로운 사용자를 시스템에 등록하는 도구로 새 계정을 생성하거나 신규 사용자를 추가할 때 사용하세요. 등록 전에 checkUserExists 도구로 사용자 이름 중복 여부를 확인하는 것이 좋습니다. 예: '새 사용자 계정 만들어줘', '관리자 권한을 가진 새 계정 생성해줘', '카카오 플랫폼으로 새 사용자 등록해줘'")
    @Transactional
    public UserResponseDTO saveUser(
            @ToolParam(description = "생성할 사용자의 정보. UserRequestDTO 객체는 다음 필드로 구성됩니다: username(사용자 이름, 필수), role(역할, ROLE_USER 또는 ROLE_ADMIN), platform(플랫폼, KAKAO/NAVER/GOOGLE 중 하나). 예: {\"username\": \"hong123\", \"role\": \"ROLE_USER\", \"platform\": \"KAKAO\"}")
            UserRequestDTO userRequestDTO) {
        User entity = new User();
        entity.setUsername(userRequestDTO.username());
        entity.setRole(userRequestDTO.role());
        entity.setPlatform(userRequestDTO.platform());
        return UserResponseDTO.from(userRepository.save(entity));
    }

    @Tool(name = "changeUserRole", description = "사용자의 역할을 변경합니다. 일반 사용자를 관리자로 승격하거나 관리자 권한을 취소해야 할 때 사용하세요. 권한 변경은 시스템의 보안과 관련된 중요한 작업입니다. 예: '홍길동 사용자를 관리자로 변경해줘', '이 계정의 권한을 일반 사용자로 바꿔줘', '특정 사용자의 역할을 업데이트해줘'")
    @Transactional
    public UserResponseDTO updateUserRole(
            @ToolParam(description = "사용자 이름과 변경할 역할 정보. UserRequestDTO 객체는 다음 필드로 구성됩니다: username(사용자 이름, 필수), role(변경할 역할, ROLE_USER 또는 ROLE_ADMIN). platform 필드는 이 메서드에서 사용되지 않습니다. 예: {\"username\": \"hong123\", \"role\": \"ROLE_ADMIN\"}")
            UserRequestDTO userRequestDTO) {
        User user = userRepository.findByUsername(userRequestDTO.username())
                .orElseThrow();
        user.setRole(userRequestDTO.role());
        return UserResponseDTO.from(userRepository.save(user));
    }

    @Tool(name = "checkUserExists", description = "특정 사용자 이름이 시스템에 이미 존재하는지 확인합니다. 새 사용자를 등록하기 전에 이름 중복 여부를 검사하거나, 특정 사용자의 존재 여부를 확인할 때 사용하세요. 결과는 true(존재함) 또는 false(존재하지 않음)입니다. 예: '홍길동이라는 사용자가 있는지 확인해줘', '이 사용자 이름이 이미 사용 중인지 알려줘', '계정 생성 전에 이름 중복 확인해줘'")
    public boolean existsUserByUsername(
            @ToolParam(description = "확인할 사용자 이름. 대소문자를 구분합니다. 예: 'hong123', 'admin_user'")
            String username) {
        return userRepository.existsByUsername(username);
    }
}
