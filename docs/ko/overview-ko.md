# 개요

> Model Context Protocol (MCP) Java SDK 소개

Java SDK for the [Model Context Protocol](https://modelcontextprotocol.org/docs/concepts/architecture)은 
AI 모델과 도구 간의 표준화된 통합을 가능하게 합니다.

### 0.8.x 버전의 주요 변경사항 ⚠️

**참고:** 0.8.x 버전은 새로운 세션 기반 아키텍처를 포함한 여러 주요 변경사항을 도입했습니다.
0.7.0에서 업그레이드하는 경우, 자세한 지침은 [마이그레이션 가이드](https://github.com/modelcontextprotocol/java-sdk/blob/main/migration-0.8.0.md)를 참조하세요.

## 특징

* MCP 클라이언트 및 MCP 서버 구현 지원:
    * 프로토콜 [버전 호환성 협상](/specification/2024-11-05/basic/lifecycle/#initialization)
    * [도구](/specification/2024-11-05/server/tools/) 검색, 실행, 목록 변경 알림
    * URI 템플릿이 있는 [리소스](/specification/2024-11-05/server/resources/) 관리
    * [루트](/specification/2024-11-05/client/roots/) 목록 관리 및 알림
    * [프롬프트](/specification/2024-11-05/server/prompts/) 처리 및 관리
    * AI 모델 상호작용을 위한 [샘플링](/specification/2024-11-05/client/sampling/) 지원
* 다양한 전송 구현:
    * 기본 전송 (핵심 `mcp` 모듈에 포함, 외부 웹 프레임워크 불필요):
        * 프로세스 기반 통신을 위한 Stdio 기반 전송
        * HTTP SSE 클라이언트 측 스트리밍을 위한 Java HttpClient 기반 SSE 클라이언트 전송
        * HTTP SSE 서버 스트리밍을 위한 Servlet 기반 SSE 서버 전송
    * 선택적 Spring 기반 전송 (Spring Framework 사용 시 편리함):
        * 반응형 HTTP 스트리밍을 위한 WebFlux SSE 클라이언트 및 서버 전송
        * 서블릿 기반 HTTP 스트리밍을 위한 WebMVC SSE 전송
* 동기 및 비동기 프로그래밍 패러다임 지원

**참고:** 핵심 `io.modelcontextprotocol.sdk:mcp` 모듈은 외부 웹 프레임워크 없이도 기본 STDIO 및 SSE 클라이언트와 서버 전송 구현을 제공합니다.

Spring 관련 전송은 [Spring Framework](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)를 사용할 때 편의를 위한 선택적 종속성으로 제공됩니다.

## 아키텍처

SDK는 명확한 관심사 분리를 갖춘 계층화된 아키텍처를 따릅니다:

![MCP 스택 아키텍처](https://mintlify.s3.us-west-1.amazonaws.com/mcp/images/java/mcp-stack.svg)

* **클라이언트/서버 계층(McpClient/McpServer)**: 둘 다 동기/비동기 작업을 위해 McpSession을 사용하며,
  McpClient는 클라이언트 측 프로토콜 작업을 처리하고 McpServer는 서버 측 프로토콜 작업을 관리합니다.
* **세션 계층(McpSession)**: DefaultMcpSession 구현을 사용하여 통신 패턴과 상태를 관리합니다.
* **전송 계층(McpTransport)**: 다음을 통해 JSON-RPC 메시지 직렬화/역직렬화를 처리합니다:
    * 핵심 모듈의 StdioTransport(stdin/stdout)
    * 전용 전송 모듈의 HTTP SSE 전송(Java HttpClient, Spring WebFlux, Spring WebMVC)

MCP 클라이언트는 Model Context Protocol(MCP) 아키텍처의 핵심 구성 요소로, MCP 서버와의 연결을 설정하고 관리하는 역할을 합니다.
프로토콜의 클라이언트 측을 구현합니다.

![Java MCP 클라이언트 아키텍처](https://mintlify.s3.us-west-1.amazonaws.com/mcp/images/java/java-mcp-client-architecture.jpg)

MCP 서버는 Model Context Protocol(MCP) 아키텍처의 기본 구성 요소로, 클라이언트에게 도구, 리소스 및 기능을 제공합니다.
프로토콜의 서버 측을 구현합니다.

![Java MCP 서버 아키텍처](https://mintlify.s3.us-west-1.amazonaws.com/mcp/images/java/java-mcp-server-architecture.jpg)

주요 상호작용:

* **클라이언트/서버 초기화**: 전송 설정, 프로토콜 호환성 확인, 기능 협상 및 구현 세부 정보 교환.
* **메시지 흐름**: 검증, 타입 안전 응답 처리 및 오류 처리가 포함된 JSON-RPC 메시지 처리.
* **리소스 관리**: 리소스 검색, URI 템플릿 기반 액세스, 구독 시스템 및 콘텐츠 검색.

## 종속성

프로젝트에 다음 Maven 종속성을 추가하세요:

### Maven
핵심 MCP 기능:

```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
</dependency>
```

핵심 `mcp` 모듈에는 이미 기본 STDIO 및 SSE 전송 구현이 포함되어 있으며 외부 웹 프레임워크가 필요하지 않습니다.

Spring Framework를 사용하고 Spring 특정 전송 구현을 사용하려면 다음 선택적 종속성 중 하나를 추가하세요:

```xml
<!-- 선택 사항: Spring WebFlux 기반 SSE 클라이언트 및 서버 전송 -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webflux</artifactId>
</dependency>

<!-- 선택 사항: Spring WebMVC 기반 SSE 서버 전송 -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webmvc</artifactId>
</dependency>
```

### Gradle
핵심 MCP 기능:

```groovy
dependencies {
  implementation platform("io.modelcontextprotocol.sdk:mcp")
  //...
}
```

핵심 `mcp` 모듈에는 이미 기본 STDIO 및 SSE 전송 구현이 포함되어 있으며 외부 웹 프레임워크가 필요하지 않습니다.

Spring Framework를 사용하고 Spring 특정 전송 구현을 사용하려면 다음 선택적 종속성 중 하나를 추가하세요:

```groovy
// 선택 사항: Spring WebFlux 기반 SSE 클라이언트 및 서버 전송
dependencies {
  implementation platform("io.modelcontextprotocol.sdk:mcp-spring-webflux")
}

// 선택 사항: Spring WebMVC 기반 SSE 서버 전송
dependencies {
  implementation platform("io.modelcontextprotocol.sdk:mcp-spring-webmvc")
}
```

Gradle 사용자는 Gradle(5.0+)의 Maven BOM을 사용하여 종속성 제약 조건을 선언하는 네이티브 지원을 활용하여 Spring AI MCP BOM을 사용할 수도 있습니다.
이는 Gradle 빌드 스크립트의 종속성 섹션에 'platform' 종속성 핸들러 메서드를 추가하여 구현됩니다.
위의 스니펫에서 볼 수 있듯이 이후에는 사용하려는 하나 이상의 spring-ai 모듈(예: spring-ai-openai)에 대한 버전이 없는 스타터 종속성 선언이 이어질 수 있습니다.

### 자재 명세서(BOM)

자재 명세서(BOM)는 주어진 릴리스에서 사용되는 모든 종속성의 권장 버전을 선언합니다.
애플리케이션의 빌드 스크립트에서 BOM을 사용하면 종속성 버전을 직접 지정하고 유지할 필요가 없습니다.
대신, 사용 중인 BOM의 버전이 사용되는 종속성 버전을 결정합니다.
또한 직접 재정의하지 않는 한 기본적으로 지원되고 테스트된 종속성 버전을 사용하고 있음을 보장합니다.

프로젝트에 BOM을 추가하세요:

### Maven
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.modelcontextprotocol.sdk</groupId>
            <artifactId>mcp-bom</artifactId>
            <version>0.9.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Gradle
```groovy
dependencies {
  implementation platform("io.modelcontextprotocol.sdk:mcp-bom:0.9.0")
  //...
}
```

Gradle 사용자는 Gradle(5.0+)의 Maven BOM을 사용하여 종속성 제약 조건을 선언하는 네이티브 지원을 활용하여 Spring AI MCP BOM을 사용할 수도 있습니다.
이는 Gradle 빌드 스크립트의 종속성 섹션에 'platform' 종속성 핸들러 메서드를 추가하여 구현됩니다.
위의 스니펫에서 볼 수 있듯이 이후에는 사용하려는 하나 이상의 spring-ai 모듈(예: spring-ai-openai)에 대한 버전이 없는 스타터 종속성 선언이 이어질 수 있습니다.

버전 번호를 사용하려는 BOM의 버전으로 바꾸세요.

### 사용 가능한 종속성

다음 종속성은 BOM에서 관리됩니다:

* 핵심 종속성
    * `io.modelcontextprotocol.sdk:mcp` - Model Context Protocol 구현을 위한 기본 기능과 API를 제공하는 핵심 MCP 라이브러리로, 기본 STDIO 및 SSE 클라이언트와 서버 전송 구현을 포함합니다. 외부 웹 프레임워크가 필요하지 않습니다.
* 선택적 전송 종속성(Spring Framework 사용 시 편리함)
    * `io.modelcontextprotocol.sdk:mcp-spring-webflux` - 반응형 애플리케이션을 위한 WebFlux 기반 Server-Sent Events(SSE) 전송 구현.
    * `io.modelcontextprotocol.sdk:mcp-spring-webmvc` - 서블릿 기반 애플리케이션을 위한 WebMVC 기반 Server-Sent Events(SSE) 전송 구현.
* 테스팅 종속성
    * `io.modelcontextprotocol.sdk:mcp-test` - MCP 기반 애플리케이션을 위한 테스팅 유틸리티 및 지원.
