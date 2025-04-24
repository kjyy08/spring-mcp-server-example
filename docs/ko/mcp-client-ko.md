# MCP 클라이언트

> Model Context Protocol (MCP) 클라이언트를 사용하여 MCP 서버와 상호작용하는 방법 알아보기

# Model Context Protocol 클라이언트

MCP 클라이언트는 Model Context Protocol(MCP) 아키텍처의 핵심 구성 요소로, MCP 서버와의 연결을 설정하고 관리하는 역할을 합니다. 프로토콜의 클라이언트 측을 구현하며 다음을 처리합니다:

* 서버와의 호환성을 보장하기 위한 프로토콜 버전 협상
* 사용 가능한 기능을 결정하기 위한 기능 협상
* 메시지 전송 및 JSON-RPC 통신
* 도구 검색 및 실행
* 리소스 접근 및 관리
* 프롬프트 시스템 상호작용
* 루트 관리 및 샘플링 지원과 같은 선택적 기능

**참고:** 핵심 `io.modelcontextprotocol.sdk:mcp` 모듈은 외부 웹 프레임워크 없이도 STDIO 및 SSE 클라이언트 전송 구현을 제공합니다.

Spring 관련 전송 구현은 [Spring Framework](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) 사용자를 위한 **선택적** 종속성 `io.modelcontextprotocol.sdk:mcp-spring-webflux`로 제공됩니다.

클라이언트는 다양한 애플리케이션 컨텍스트에서 유연성을 제공하기 위해 동기 및 비동기 API를 모두 제공합니다.

## 동기 API 예제
```java
// 사용자 정의 구성으로 동기 클라이언트 생성
McpSyncClient client = McpClient.sync(transport)
    .requestTimeout(Duration.ofSeconds(10))
    .capabilities(ClientCapabilities.builder()
        .roots(true)      // 루트 기능 활성화
        .sampling()       // 샘플링 기능 활성화
        .build())
    .sampling(request -> new CreateMessageResult(response))
    .build();

// 연결 초기화
client.initialize();

// 사용 가능한 도구 나열
ListToolsResult tools = client.listTools();

// 도구 호출
CallToolResult result = client.callTool(
    new CallToolRequest("calculator", 
        Map.of("operation", "add", "a", 2, "b", 3))
);

// 리소스 나열 및 읽기
ListResourcesResult resources = client.listResources();
ReadResourceResult resource = client.readResource(
    new ReadResourceRequest("resource://uri")
);

// 프롬프트 나열 및 사용
ListPromptsResult prompts = client.listPrompts();
GetPromptResult prompt = client.getPrompt(
    new GetPromptRequest("greeting", Map.of("name", "Spring"))
);

// 루트 추가/제거
client.addRoot(new Root("file:///path", "description"));
client.removeRoot("file:///path");

// 클라이언트 닫기
client.closeGracefully();
```

## 비동기 API 예제
```java
// 사용자 정의 구성으로 비동기 클라이언트 생성
McpAsyncClient client = McpClient.async(transport)
    .requestTimeout(Duration.ofSeconds(10))
    .capabilities(ClientCapabilities.builder()
        .roots(true)      // 루트 기능 활성화
        .sampling()       // 샘플링 기능 활성화
        .build())
    .sampling(request -> Mono.just(new CreateMessageResult(response)))
    .toolsChangeConsumer(tools -> Mono.fromRunnable(() -> {
        logger.info("도구 업데이트됨: {}", tools);
    }))
    .resourcesChangeConsumer(resources -> Mono.fromRunnable(() -> {
        logger.info("리소스 업데이트됨: {}", resources);
    }))
    .promptsChangeConsumer(prompts -> Mono.fromRunnable(() -> {
        logger.info("프롬프트 업데이트됨: {}", prompts);
    }))
    .build();

// 연결 초기화 및 기능 사용
client.initialize()
    .flatMap(initResult -> client.listTools())
    .flatMap(tools -> {
        return client.callTool(new CallToolRequest(
            "calculator", 
            Map.of("operation", "add", "a", 2, "b", 3)
        ));
    })
    .flatMap(result -> {
        return client.listResources()
            .flatMap(resources -> 
                client.readResource(new ReadResourceRequest("resource://uri"))
            );
    })
    .flatMap(resource -> {
        return client.listPrompts()
            .flatMap(prompts ->
                client.getPrompt(new GetPromptRequest(
                    "greeting", 
                    Map.of("name", "Spring")
                ))
            );
    })
    .flatMap(prompt -> {
        return client.addRoot(new Root("file:///path", "description"))
            .then(client.removeRoot("file:///path"));            
    })
    .doFinally(signalType -> {
        client.closeGracefully().subscribe();
    })
    .subscribe();
```

## 클라이언트 전송

전송 계층은 MCP 클라이언트와 서버 간의 통신을 처리하며, 다양한 사용 사례에 대한 다양한 구현을 제공합니다. 클라이언트 전송은 메시지 직렬화, 연결 설정 및 프로토콜별 통신 패턴을 관리합니다.

### STDIO
프로세스 내 통신을 위한 전송 생성

```java
ServerParameters params = ServerParameters.builder("npx")
    .args("-y", "@modelcontextprotocol/server-everything", "dir")
    .build();
McpTransport transport = new StdioClientTransport(params);
```

### SSE (HttpClient)
프레임워크에 구애받지 않는(순수 Java API) SSE 클라이언트 전송을 생성합니다. 핵심 mcp 모듈에 포함되어 있습니다.

```java
McpTransport transport = new HttpClientSseClientTransport("http://your-mcp-server");
```

### SSE (WebFlux)
WebFlux 기반 SSE 클라이언트 전송을 생성합니다. mcp-webflux-sse-transport 종속성이 필요합니다.

```java
WebClient.Builder webClientBuilder = WebClient.builder()
    .baseUrl("http://your-mcp-server");
McpTransport transport = new WebFluxSseClientTransport(webClientBuilder);
```

## 클라이언트 기능

클라이언트는 다양한 기능으로 구성할 수 있습니다:

```java
var capabilities = ClientCapabilities.builder()
    .roots(true)      // 파일 시스템 루트 지원 및 목록 변경 알림 활성화
    .sampling()       // LLM 샘플링 지원 활성화
    .build();
```

### 루트 지원

루트는 서버가 파일 시스템 내에서 작동할 수 있는 경계를 정의합니다:

```java
// 동적으로 루트 추가
client.addRoot(new Root("file:///path", "description"));

// 루트 제거
client.removeRoot("file:///path");

// 루트 변경 알림
client.rootsListChangedNotification();
```

루트 기능을 통해 서버는 다음을 수행할 수 있습니다:

* 접근 가능한 파일 시스템 루트 목록 요청
* 루트 목록이 변경될 때 알림 수신
* 접근 권한이 있는 디렉토리와 파일 이해

### 샘플링 지원

샘플링을 통해 서버는 클라이언트를 통해 LLM 상호작용("완성" 또는 "생성")을 요청할 수 있습니다:

```java
// 샘플링 핸들러 구성
Function<CreateMessageRequest, CreateMessageResult> samplingHandler = request -> {
    // LLM과 인터페이스하는 샘플링 구현
    return new CreateMessageResult(response);
};

// 샘플링 지원이 있는 클라이언트 생성
var client = McpClient.sync(transport)
    .capabilities(ClientCapabilities.builder()
        .sampling()
        .build())
    .sampling(samplingHandler)
    .build();
```

이 기능을 통해 다음이 가능합니다:

* 서버가 API 키 없이도 AI 기능을 활용
* 클라이언트가 모델 접근 및 권한에 대한 제어 유지
* 텍스트 및 이미지 기반 상호작용 모두 지원
* MCP 서버 컨텍스트를 프롬프트에 선택적으로 포함

### 로깅 지원

클라이언트는 서버로부터 로그 메시지를 수신하기 위한 로깅 소비자를 등록하고 메시지를 필터링하기 위한 최소 로깅 수준을 설정할 수 있습니다:

```java
var mcpClient = McpClient.sync(transport)
        .loggingConsumer(notification -> {
            System.out.println("로그 메시지 수신: " + notification.data());
        })
        .build();

mcpClient.initialize();

mcpClient.setLoggingLevel(McpSchema.LoggingLevel.INFO);

// 로깅 알림을 보낼 수 있는 도구 호출
CallToolResult result = mcpClient.callTool(new McpSchema.CallToolRequest("logging-test", Map.of()));
```

클라이언트는 `mcpClient.setLoggingLevel(level)` 요청을 통해 수신하는 최소 로깅 수준을 제어할 수 있습니다. 설정된 수준 미만의 메시지는 필터링됩니다.
지원되는 로깅 수준(심각도 증가 순): DEBUG (0), INFO (1), NOTICE (2), WARNING (3), ERROR (4), CRITICAL (5), ALERT (6), EMERGENCY (7)

## MCP 클라이언트 사용하기

### 도구 실행

도구는 클라이언트가 검색하고 실행할 수 있는 서버 측 함수입니다. MCP 클라이언트는 사용 가능한 도구를 나열하고 특정 매개변수로 실행하는 메서드를 제공합니다. 각 도구는 고유한 이름을 가지며 매개변수 맵을 받습니다.

#### 동기 API 예제
```java
// 사용 가능한 도구 및 이름 나열
var tools = client.listTools();
tools.forEach(tool -> System.out.println(tool.getName()));

// 매개변수로 도구 실행
var result = client.callTool("calculator", Map.of(
    "operation", "add",
    "a", 1,
    "b", 2
));
```

#### 비동기 API 예제
```java
// 비동기적으로 사용 가능한 도구 나열
client.listTools()
    .doOnNext(tools -> tools.forEach(tool -> 
        System.out.println(tool.getName())))
    .subscribe();

// 비동기적으로 도구 실행
client.callTool("calculator", Map.of(
        "operation", "add",
        "a", 1,
        "b", 2
    ))
    .subscribe();
```

### 리소스 접근

리소스는 클라이언트가 URI 템플릿을 사용하여 접근할 수 있는 서버 측 데이터 소스를 나타냅니다. MCP 클라이언트는 사용 가능한 리소스를 검색하고 표준화된 인터페이스를 통해 내용을 검색하는 메서드를 제공합니다.

#### 동기 API 예제
```java
// 사용 가능한 리소스 및 이름 나열
var resources = client.listResources();
resources.forEach(resource -> System.out.println(resource.getName()));

// URI 템플릿을 사용하여 리소스 내용 검색
var content = client.getResource("file", Map.of(
    "path", "/path/to/file.txt"
));
```

#### 비동기 API 예제
```java
// 비동기적으로 사용 가능한 리소스 나열
client.listResources()
    .doOnNext(resources -> resources.forEach(resource -> 
        System.out.println(resource.getName())))
    .subscribe();

// 비동기적으로 리소스 내용 검색
client.getResource("file", Map.of(
        "path", "/path/to/file.txt"
    ))
    .subscribe();
```

### 프롬프트 시스템

프롬프트 시스템은 서버 측 프롬프트 템플릿과의 상호작용을 가능하게 합니다. 이러한 템플릿은 검색하고 사용자 정의 매개변수로 실행할 수 있어 미리 정의된 패턴을 기반으로 동적 텍스트 생성이 가능합니다.

#### 동기 API 예제
```java
// 사용 가능한 프롬프트 템플릿 나열
var prompts = client.listPrompts();
prompts.forEach(prompt -> System.out.println(prompt.getName()));

// 매개변수로 프롬프트 템플릿 실행
var response = client.executePrompt("echo", Map.of(
    "text", "Hello, World!"
));
```

#### 비동기 API 예제
```java
// 비동기적으로 사용 가능한 프롬프트 템플릿 나열
client.listPrompts()
    .doOnNext(prompts -> prompts.forEach(prompt -> 
        System.out.println(prompt.getName())))
    .subscribe();

// 비동기적으로 프롬프트 템플릿 실행
client.executePrompt("echo", Map.of(
        "text", "Hello, World!"
    ))
    .subscribe();
```

### 완성 사용하기

[완성 기능](/specification/2025-03-26/server/utilities/completion)의 일부로, MCP는 서버가 프롬프트 및 리소스 URI에 대한 인수 자동 완성 제안을 제공하는 표준화된 방법을 제공합니다.

서버 측에서 완성 기능을 활성화하고 구성하는 방법은 [서버 완성 기능](/sdk/java/mcp-server#completion-specification)을 참조하세요.

클라이언트 측에서 MCP 클라이언트는 자동 완성을 요청하는 메서드를 제공합니다:

#### 동기 API 예제
```java
CompleteRequest request = new CompleteRequest(
        new PromptReference("code_review"),
        new CompleteRequest.CompleteArgument("language", "py"));

CompleteResult result = syncMcpClient.completeCompletion(request);
```

#### 비동기 API 예제
```java
CompleteRequest request = new CompleteRequest(
        new PromptReference("code_review"),
        new CompleteRequest.CompleteArgument("language", "py"));

Mono<CompleteResult> result = mcpClient.completeCompletion(request);
```