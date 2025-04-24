# MCP 서버

> Model Context Protocol (MCP) 서버를 구현하고 구성하는 방법 알아보기

### 0.8.x 버전의 주요 변경사항 ⚠️

**참고:** 0.8.x 버전은 새로운 세션 기반 아키텍처를 포함한 여러 주요 변경사항을 도입했습니다.
0.7.0에서 업그레이드하는 경우, 자세한 지침은 [마이그레이션 가이드](https://github.com/modelcontextprotocol/java-sdk/blob/main/migration-0.8.0.md)를 참조하세요.

## 개요

MCP 서버는 Model Context Protocol(MCP) 아키텍처의 기본 구성 요소로, 클라이언트에게 도구, 리소스 및 기능을 제공합니다. 프로토콜의 서버 측을 구현하며 다음을 담당합니다:

* 클라이언트가 검색하고 실행할 수 있는 도구 노출
* URI 기반 접근 패턴을 가진 리소스 관리
* 프롬프트 템플릿 제공 및 프롬프트 요청 처리
* 클라이언트와의 기능 협상 지원
* 서버 측 프로토콜 작업 구현
* 동시 클라이언트 연결 관리
* 구조화된 로깅 및 알림 제공

**참고:** 핵심 `io.modelcontextprotocol.sdk:mcp` 모듈은 외부 웹 프레임워크 없이도 STDIO 및 SSE 서버 전송 구현을 제공합니다.

Spring 관련 전송 구현은 [Spring Framework](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) 사용자를 위한 **선택적** 종속성 `io.modelcontextprotocol.sdk:mcp-spring-webflux`, `io.modelcontextprotocol.sdk:mcp-spring-webmvc`로 제공됩니다.

서버는 다양한 애플리케이션 컨텍스트에서 유연한 통합을 위해 동기 및 비동기 API를 모두 지원합니다.

## 동기 API 예제
```java
// 사용자 정의 구성으로 서버 생성
McpSyncServer syncServer = McpServer.sync(transportProvider)
    .serverInfo("my-server", "1.0.0")
    .capabilities(ServerCapabilities.builder()
        .resources(true)     // 리소스 지원 활성화
        .tools(true)         // 도구 지원 활성화
        .prompts(true)       // 프롬프트 지원 활성화
        .logging()           // 로깅 지원 활성화
        .completions()      // 완성 지원 활성화
        .build())
    .build();

// 도구, 리소스 및 프롬프트 등록
syncServer.addTool(syncToolSpecification);
syncServer.addResource(syncResourceSpecification);
syncServer.addPrompt(syncPromptSpecification);

// 완료 시 서버 닫기
syncServer.close();
```

## 비동기 API 예제
```java
// 사용자 정의 구성으로 비동기 서버 생성
McpAsyncServer asyncServer = McpServer.async(transportProvider)
    .serverInfo("my-server", "1.0.0")
    .capabilities(ServerCapabilities.builder()
        .resources(true)     // 리소스 지원 활성화
        .tools(true)         // 도구 지원 활성화
        .prompts(true)       // 프롬프트 지원 활성화
        .logging()           // 로깅 지원 활성화
        .build())
    .build();

// 도구, 리소스 및 프롬프트 등록
asyncServer.addTool(asyncToolSpecification)
    .doOnSuccess(v -> logger.info("도구 등록됨"))
    .subscribe();

asyncServer.addResource(asyncResourceSpecification)
    .doOnSuccess(v -> logger.info("리소스 등록됨"))
    .subscribe();

asyncServer.addPrompt(asyncPromptSpecification)
    .doOnSuccess(v -> logger.info("프롬프트 등록됨"))
    .subscribe();

// 완료 시 서버 닫기
asyncServer.close()
    .doOnSuccess(v -> logger.info("서버 닫힘"))
    .subscribe();
```

## 서버 전송 제공자

MCP SDK의 전송 계층은 클라이언트와 서버 간의 통신을 처리하는 역할을 합니다.
다양한 통신 프로토콜과 패턴을 지원하기 위해 여러 구현을 제공합니다.
SDK에는 여러 내장 전송 제공자 구현이 포함되어 있습니다:

### STDIO

프로세스 내 기반 전송 생성:

```java
StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());
```

비차단 메시지 처리, 직렬화/역직렬화 및 정상 종료 지원과 함께 표준 입력/출력 스트림을 통한 양방향 JSON-RPC 메시지 처리를 제공합니다.

주요 기능:
- stdin/stdout을 통한 양방향 통신
- 프로세스 기반 통합 지원
- 간단한 설정 및 구성
- 경량 구현

### SSE (WebFlux)

WebFlux 기반 SSE 서버 전송을 생성합니다.
`mcp-spring-webflux` 종속성이 필요합니다.

```java
@Configuration
class McpConfig {
    @Bean
    WebFluxSseServerTransportProvider webFluxSseServerTransportProvider(ObjectMapper mapper) {
        return new WebFluxSseServerTransportProvider(mapper, "/mcp/message");
    }

    @Bean
    RouterFunction<?> mcpRouterFunction(WebFluxSseServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }
}
```

MCP HTTP와 SSE 전송 사양을 구현하여 다음을 제공합니다:
- WebFlux를 사용한 반응형 HTTP 스트리밍
- SSE 엔드포인트를 통한 동시 클라이언트 연결
- 메시지 라우팅 및 세션 관리
- 정상 종료 기능

### SSE (WebMvc)

WebMvc 기반 SSE 서버 전송을 생성합니다.
`mcp-spring-webmvc` 종속성이 필요합니다.

```java
@Configuration
@EnableWebMvc
class McpConfig {
    @Bean
    WebMvcSseServerTransportProvider webMvcSseServerTransportProvider(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(mapper, "/mcp/message");
    }

    @Bean
    RouterFunction<ServerResponse> mcpRouterFunction(WebMvcSseServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }
}
```

MCP HTTP와 SSE 전송 사양을 구현하여 다음을 제공합니다:
- 서버 측 이벤트 스트리밍
- Spring WebMVC와의 통합
- 전통적인 웹 애플리케이션 지원
- 동기식 작업 처리

### SSE (Servlet)

Servlet 기반 SSE 서버 전송을 생성합니다. 핵심 `mcp` 모듈에 포함되어 있습니다.
`HttpServletSseServerTransport`는 모든 Servlet 컨테이너와 함께 사용할 수 있습니다.
Spring 웹 애플리케이션에서 사용하려면 Servlet 빈으로 등록할 수 있습니다:

```java
@Configuration
@EnableWebMvc
public class McpServerConfig implements WebMvcConfigurer {

    @Bean
    public HttpServletSseServerTransportProvider servletSseServerTransportProvider() {
        return new HttpServletSseServerTransportProvider(new ObjectMapper(), "/mcp/message");
    }

    @Bean
    public ServletRegistrationBean customServletBean(HttpServletSseServerTransportProvider transportProvider) {
        return new ServletRegistrationBean(transportProvider);
    }
}
```

전통적인 Servlet API를 사용하여 MCP HTTP와 SSE 전송 사양을 구현하여 다음을 제공합니다:
- Servlet 6.0 비동기 지원을 사용한 비동기 메시지 처리
- 여러 클라이언트 연결을 위한 세션 관리
- 두 가지 유형의 엔드포인트:
  - 서버-클라이언트 이벤트를 위한 SSE 엔드포인트(`/sse`)
  - 클라이언트-서버 요청을 위한 메시지 엔드포인트(구성 가능)
- 오류 처리 및 응답 형식 지정
- 정상 종료 지원

## 서버 기능

서버는 다양한 기능으로 구성할 수 있습니다:

```java
var capabilities = ServerCapabilities.builder()
    .resources(false, true)  // 목록 변경 알림이 있는 리소스 지원
    .tools(true)            // 목록 변경 알림이 있는 도구 지원
    .prompts(true)          // 목록 변경 알림이 있는 프롬프트 지원
    .logging()              // 로깅 지원 활성화(기본적으로 로깅 수준 INFO로 활성화됨)
    .build();
```

### 로깅 지원

서버는 다양한 심각도 수준으로 클라이언트에게 로그 메시지를 보낼 수 있는 구조화된 로깅 기능을 제공합니다:

```java
// 클라이언트에게 로그 메시지 보내기
server.loggingNotification(LoggingMessageNotification.builder()
    .level(LoggingLevel.INFO)
    .logger("custom-logger")
    .data("사용자 정의 로그 메시지")
    .build());
```

클라이언트는 `mcpClient.setLoggingLevel(level)` 요청을 통해 수신하는 최소 로깅 수준을 제어할 수 있습니다. 설정된 수준 미만의 메시지는 필터링됩니다.
지원되는 로깅 수준(심각도 증가 순): DEBUG (0), INFO (1), NOTICE (2), WARNING (3), ERROR (4), CRITICAL (5), ALERT (6), EMERGENCY (7)

### 도구 명세

Model Context Protocol은 서버가 언어 모델이 호출할 수 있는 [도구를 노출](/specification/2024-11-05/server/tools/)할 수 있게 합니다.
Java SDK는 핸들러 함수와 함께 도구 명세를 구현할 수 있게 합니다.
도구를 통해 AI 모델은 계산을 수행하고, 외부 API에 접근하고, 데이터베이스를 쿼리하고, 파일을 조작할 수 있습니다.

#### 동기 도구 명세 예제
```java
// 동기 도구 명세
var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string"
                },
                "a" : {
                  "type" : "number"
                },
                "b" : {
                  "type" : "number"
                }
              }
            }
            """;
var syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
    new Tool("calculator", "기본 계산기", schema),
    (exchange, arguments) -> {
        // 도구 구현
        return new CallToolResult(result, false);
    }
);
```

#### 비동기 도구 명세 예제
```java
// 비동기 도구 명세
var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string"
                },
                "a" : {
                  "type" : "number"
                },
                "b" : {
                  "type" : "number"
                }
              }
            }
            """;
var asyncToolSpecification = new McpServerFeatures.AsyncToolSpecification(
    new Tool("calculator", "기본 계산기", schema),
    (exchange, arguments) -> {
        // 도구 구현
        return Mono.just(new CallToolResult(result, false));
    }
);
```

도구 명세는 `이름`, `설명` 및 `매개변수 스키마`가 있는 도구 정의와 도구의 로직을 구현하는 호출 핸들러를 포함합니다.
함수의 첫 번째 인수는 클라이언트 상호작용을 위한 `McpAsyncServerExchange`이고, 두 번째는 도구 인수의 맵입니다.

### 리소스 명세

핸들러 함수가 있는 리소스 명세입니다.
리소스는 다음과 같은 데이터를 노출하여 AI 모델에 컨텍스트를 제공합니다: 파일 내용, 데이터베이스 레코드, API 응답, 시스템 정보, 애플리케이션 상태.

#### 동기 리소스 명세 예제
```java
// 동기 리소스 명세
var syncResourceSpecification = new McpServerFeatures.SyncResourceSpecification(
    new Resource("custom://resource", "name", "description", "mime-type", null),
    (exchange, request) -> {
        // 리소스 읽기 구현
        return new ReadResourceResult(contents);
    }
);
```

#### 비동기 리소스 명세 예제
```java
// 비동기 리소스 명세
var asyncResourceSpecification = new McpServerFeatures.AsyncResourceSpecification(
    new Resource("custom://resource", "name", "description", "mime-type", null),
    (exchange, request) -> {
        // 리소스 읽기 구현
        return Mono.just(new ReadResourceResult(contents));
    }
);
```

리소스 명세는 리소스 정의와 리소스 읽기 핸들러로 구성됩니다.
리소스 정의에는 `이름`, `설명` 및 `MIME 유형`이 포함됩니다.
리소스 읽기 요청을 처리하는 함수의 첫 번째 인수는 연결된 클라이언트와 상호작용할 수 있는 `McpAsyncServerExchange`입니다.
두 번째 인수는 `McpSchema.ReadResourceRequest`입니다.

### 프롬프트 명세

[프롬프팅 기능](/specification/2024-11-05/server/prompts/)의 일부로, MCP는 서버가 클라이언트에게 프롬프트 템플릿을 노출하는 표준화된 방법을 제공합니다.
프롬프트 명세는 일관된 메시지 형식, 매개변수 대체, 컨텍스트 주입, 응답 형식 지정 및 지시 템플릿을 가능하게 하는 AI 모델 상호작용을 위한 구조화된 템플릿입니다.

#### 동기 프롬프트 명세 예제
```java
// 동기 프롬프트 명세
var syncPromptSpecification = new McpServerFeatures.SyncPromptSpecification(
    new Prompt("greeting", "description", List.of(
        new PromptArgument("name", "description", true)
    )),
    (exchange, request) -> {
        // 프롬프트 구현
        return new GetPromptResult(description, messages);
    }
);
```

#### 비동기 프롬프트 명세 예제
```java
// 비동기 프롬프트 명세
var asyncPromptSpecification = new McpServerFeatures.AsyncPromptSpecification(
    new Prompt("greeting", "description", List.of(
        new PromptArgument("name", "description", true)
    )),
    (exchange, request) -> {
        // 프롬프트 구현
        return Mono.just(new GetPromptResult(description, messages));
    }
);
```

프롬프트 정의에는 프롬프트의 이름(식별자), 설명(프롬프트의 목적) 및 인수 목록(템플릿용 매개변수)이 포함됩니다.
핸들러 함수는 요청을 처리하고 형식이 지정된 템플릿을 반환합니다.
첫 번째 인수는 클라이언트 상호작용을 위한 `McpAsyncServerExchange`이고, 두 번째 인수는 `GetPromptRequest` 인스턴스입니다.

### 완성 명세

[완성 기능](/specification/2025-03-26/server/utilities/completion)의 일부로, MCP는 서버가 프롬프트 및 리소스 URI에 대한 인수 자동 완성 제안을 제공하는 표준화된 방법을 제공합니다.

#### 동기 완성 명세 예제
```java
// 동기 완성 명세
var syncCompletionSpecification = new McpServerFeatures.SyncCompletionSpecification(
        new McpSchema.PromptReference("code_review"), (exchange, request) -> {

        // 완성 구현 ...

        return new McpSchema.CompleteResult(
            new CompleteResult.CompleteCompletion(
              List.of("python", "pytorch", "pyside"), 
              10, // 총 개수
              false // 더 있음
            ));
      }
);

// 완성 기능이 있는 동기 서버 생성
var mcpServer = McpServer.sync(mcpServerTransportProvider)
  .capabilities(ServerCapabilities.builder()
    .completions() // 완성 지원 활성화
      // ...
    .build())
  // ...
  .completions(new McpServerFeatures.SyncCompletionSpecification( // 완성 명세 등록
      new McpSchema.PromptReference("code_review"), syncCompletionSpecification))
  .build();
```

#### 비동기 완성 명세 예제
```java
// 비동기 프롬프트 명세
var asyncCompletionSpecification = new McpServerFeatures.AsyncCompletionSpecification(
        new McpSchema.PromptReference("code_review"), (exchange, request) -> {

        // 완성 구현 ...

        return Mono.just(new McpSchema.CompleteResult(
            new CompleteResult.CompleteCompletion(
              List.of("python", "pytorch", "pyside"), 
              10, // 총 개수
              false // 더 있음
            )));
      }
);

// 완성 기능이 있는 비동기 서버 생성
var mcpServer = McpServer.async(mcpServerTransportProvider)
  .capabilities(ServerCapabilities.builder()
    .completions() // 완성 지원 활성화
      // ...
    .build())
  // ...
  .completions(new McpServerFeatures.AsyncCompletionSpecification( // 완성 명세 등록
      new McpSchema.PromptReference("code_review"), asyncCompletionSpecification))
  .build();
```

`McpSchema.CompletionReference` 정의는 완성 명세(예: 핸들러)의 유형(`PromptRefernce` 또는 `ResourceRefernce`)과 식별자를 정의합니다.
핸들러 함수는 요청을 처리하고 완성 응답을 반환합니다.
첫 번째 인수는 클라이언트 상호작용을 위한 `McpAsyncServerExchange`이고, 두 번째 인수는 `CompleteRequest` 인스턴스입니다.

클라이언트 측에서 완성 기능을 사용하는 방법은 [완성 사용하기](/sdk/java/mcp-client#using-completion)를 참조하세요.

### 서버에서 샘플링 사용하기

[샘플링 기능](/specification/2024-11-05/client/sampling/)을 사용하려면 샘플링을 지원하는 클라이언트에 연결하세요.
특별한 서버 구성은 필요하지 않지만, 요청을 하기 전에 클라이언트 샘플링 지원을 확인하세요.
[클라이언트 샘플링 지원](./mcp-client#sampling-support)에 대해 알아보세요.

호환되는 클라이언트에 연결되면 서버는 언어 모델 생성을 요청할 수 있습니다:

#### 동기 API 예제
```java
// 서버 생성
McpSyncServer server = McpServer.sync(transportProvider)
    .serverInfo("my-server", "1.0.0")
    .build();

// 샘플링을 사용하는 도구 정의
var calculatorTool = new McpServerFeatures.SyncToolSpecification(
    new Tool("ai-calculator", "AI를 사용하여 계산 수행", schema),
    (exchange, arguments) -> {
        // 클라이언트가 샘플링을 지원하는지 확인
        if (exchange.getClientCapabilities().sampling() == null) {
            return new CallToolResult("클라이언트가 AI 기능을 지원하지 않습니다", false);
        }

        // 샘플링 요청 생성
        McpSchema.CreateMessageRequest request = McpSchema.CreateMessageRequest.builder()
            .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                new McpSchema.TextContent("계산: " + arguments.get("expression")))
            .modelPreferences(McpSchema.ModelPreferences.builder()
                .hints(List.of(
                    McpSchema.ModelHint.of("claude-3-sonnet"),
                    McpSchema.ModelHint.of("claude")
                ))
                .intelligencePriority(0.8)  // 지능 우선순위
                .speedPriority(0.5)         // 중간 속도 중요도
                .build())
            .systemPrompt("당신은 도움이 되는 계산기 어시스턴트입니다. 숫자 답변만 제공하세요.")
            .maxTokens(100)
            .build();

        // 클라이언트에 샘플링 요청
        McpSchema.CreateMessageResult result = exchange.createMessage(request);

        // 결과 처리
        String answer = result.content().text();
        return new CallToolResult(answer, false);
    }
);

// 서버에 도구 추가
server.addTool(calculatorTool);
```

#### 비동기 API 예제
```java
// 서버 생성
McpAsyncServer server = McpServer.async(transportProvider)
    .serverInfo("my-server", "1.0.0")
    .build();

// 샘플링을 사용하는 도구 정의
var calculatorTool = new McpServerFeatures.AsyncToolSpecification(
    new Tool("ai-calculator", "AI를 사용하여 계산 수행", schema),
    (exchange, arguments) -> {
        // 클라이언트가 샘플링을 지원하는지 확인
        if (exchange.getClientCapabilities().sampling() == null) {
            return Mono.just(new CallToolResult("클라이언트가 AI 기능을 지원하지 않습니다", false));
        }

        // 샘플링 요청 생성
        McpSchema.CreateMessageRequest request = McpSchema.CreateMessageRequest.builder()
            .content(new McpSchema.TextContent("계산: " + arguments.get("expression")))
            .modelPreferences(McpSchema.ModelPreferences.builder()
                .hints(List.of(
                    McpSchema.ModelHint.of("claude-3-sonnet"),
                    McpSchema.ModelHint.of("claude")
                ))
                .intelligencePriority(0.8)  // 지능 우선순위
                .speedPriority(0.5)         // 중간 속도 중요도
                .build())
            .systemPrompt("당신은 도움이 되는 계산기 어시스턴트입니다. 숫자 답변만 제공하세요.")
            .maxTokens(100)
            .build();

        // 클라이언트에 샘플링 요청
        return exchange.createMessage(request)
            .map(result -> {
                // 결과 처리
                String answer = result.content().text();
                return new CallToolResult(answer, false);
            });
    }
);

// 서버에 도구 추가
server.addTool(calculatorTool)
    .subscribe();
```

`CreateMessageRequest` 객체를 통해 다음을 지정할 수 있습니다: `Content` - 모델에 대한 입력 텍스트 또는 이미지,
`Model Preferences` - 모델 선택을 위한 힌트 및 우선순위, `System Prompt` - 모델 동작에 대한 지침 및
`Max Tokens` - 생성된 응답의 최대 길이.

### 로깅 지원

서버는 다양한 심각도 수준으로 클라이언트에게 로그 메시지를 보낼 수 있는 구조화된 로깅 기능을 제공합니다. 로그 알림은 도구, 리소스 및 프롬프트 호출과 같은 기존 클라이언트 세션 내에서만 보낼 수 있습니다.

예를 들어, 도구 핸들러 함수 내에서 로그 메시지를 보낼 수 있습니다.
클라이언트 측에서는 서버로부터 로그 메시지를 수신하기 위한 로깅 소비자를 등록하고 메시지를 필터링하기 위한 최소 로깅 수준을 설정할 수 있습니다.

```java
var mcpClient = McpClient.sync(transport)
        .loggingConsumer(notification -> {
            System.out.println("로그 메시지 수신: " + notification.data());
        })
        .build();

mcpClient.initialize();

mcpClient.setLoggingLevel(McpSchema.LoggingLevel.INFO);

// 로깅 알림을 보내는 도구 호출
CallToolResult result = mcpClient.callTool(new McpSchema.CallToolRequest("logging-test", Map.of()));
```

서버는 도구/리소스/프롬프트 핸들러 함수에서 `McpAsyncServerExchange`/`McpSyncServerExchange` 객체를 사용하여 로그 메시지를 보낼 수 있습니다:

```java
var tool = new McpServerFeatures.AsyncToolSpecification(
    new McpSchema.Tool("logging-test", "로깅 알림 테스트", emptyJsonSchema),
    (exchange, request) -> {  

      exchange.loggingNotification( // 로그 메시지를 보내기 위해 exchange 사용
          McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.DEBUG)
            .logger("test-logger")
            .data("디버그 메시지")
            .build())
        .block();

      return Mono.just(new CallToolResult("로깅 테스트 완료", false));
    });

var mcpServer = McpServer.async(mcpServerTransportProvider)
  .serverInfo("test-server", "1.0.0")
  .capabilities(
    ServerCapabilities.builder()
      .logging() // 로깅 지원 활성화
      .tools(true)
      .build())
  .tools(tool)
  .build();
```

클라이언트는 `mcpClient.setLoggingLevel(level)` 요청을 통해 수신하는 최소 로깅 수준을 제어할 수 있습니다. 설정된 수준 미만의 메시지는 필터링됩니다.
지원되는 로깅 수준(심각도 증가 순): DEBUG (0), INFO (1), NOTICE (2), WARNING (3), ERROR (4), CRITICAL (5), ALERT (6), EMERGENCY (7)

## 오류 처리

SDK는 McpError 클래스를 통해 프로토콜 호환성, 전송 통신, JSON-RPC 메시징, 도구 실행, 리소스 관리, 프롬프트 처리, 타임아웃 및 연결 문제를 포함하는 포괄적인 오류 처리를 제공합니다. 이 통합된 오류 처리 접근 방식은 동기 및 비동기 작업 모두에서 일관되고 신뢰할 수 있는 오류 관리를 보장합니다.
