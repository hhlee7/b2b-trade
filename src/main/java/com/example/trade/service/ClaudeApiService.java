package com.example.trade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeApiService {

    // application.properties에서 Claude API 설정값 주입
    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    private final WebClient webClient;

    // Spring이 관리하는 WebClient.Builder를 주입받아 사용
    public ClaudeApiService(WebClient.Builder webClientBuilder) {
        // HttpClient(Netty) 생성 + 타임아웃 설정 (타임아웃은 네트워크 레벨의 설정이라 HttpClient에 걸어야 함)
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(5));

        // HttpClient를 WebClient가 쓸 수 있는 형태로 감싸기
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        // WebClient 만들 때 커넥터를 장착
        this.webClient = webClientBuilder
                .clientConnector(connector)
                .build();
    }

    /**
     * Claude API 호출 공통 메서드
     *
     * @param systemPrompt AI 역할 정의 - 짧고 명확할수록 input 토큰 절약
     * @param userMessage  실제 요청 내용
     * @return Claude 응답 텍스트
     */
    public String call(String systemPrompt, String userMessage) {

        // Claude API 요청 바디 구성
        // max_tokens: 응답 최대 길이 제한 : output 토큰 비용 상한선 역할
        Map<String, Object> requestBody = Map.of(
                "model", model,
                // "max_tokens", 1024,
                "max_tokens", 500,
                "system", systemPrompt, // 역할 정의 (매 요청마다 토큰 소비되므로 최소화)
                "messages", List.of(
                        Map.of("role", "user", "content", userMessage)
                )
        );

        // WebClient로 Claude API POST 요청
        Map response = webClient.post() // POST 방식
                .uri(apiUrl) // 해당 주소로
                .header("x-api-key", apiKey) // 인증키 헤더
                .header("anthropic-version", "2023-06-01") // API 버전
                .header("content-type", "application/json") // JSON 형태로 전송
                .bodyValue(requestBody) // 위 구간에서 만든 데이터를 body에 담음
                .retrieve() // 응답 받을 준비
                .bodyToMono(Map.class) // 응답을 Map 타입으로 변환
                .timeout(Duration.ofSeconds(5)) // 5초 안에 응답 없으면 예외 발생
                .onErrorReturn(buildFallbackResponse()) // 예외 발생 시 fallback 응답 반환
                .block(); // 동기 방식으로 응답 대기

        /* Claude 응답 구조
        {
          "content": [
            {"type": "text", "text": "실제 AI 답변 내용"}
          ],
          "usage": {
            "input_tokens": 30,
            "output_tokens": 120
          }
        }
        */

        // content 배열 첫 번째 항목에서 텍스트 추출
        List<Map<String, Object>> contentList = (List<Map<String, Object>>)response.get("content");

        // 토큰 사용량 로그 출력
        Map<String, Object> usage = (Map<String, Object>)response.get("usage");
        if(usage != null) {
            System.out.println("[Claude 토큰 사용량] input: " + usage.get("input_tokens") + ", output: " + usage.get("output_tokens"));
        }

        return (String)contentList.get(0).get("text");
    }

    // 타임아웃/오류 발생 시 반환할 fallback 응답 구성
    // 정상 응답과 동일한 JSON 구조를 갖춰야 call 메서드의 return에서 contentList.get(0).get("text")가 오류 없이 동작함
    private Map<String, Object> buildFallbackResponse() {
        // 텍스트를 담은 Map (Anthropic 응답의 content 배열 항목 구조)
        Map<String, Object> textMap = Map.of("type", "text", "text", "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");

        // List에 담기 (Anthropic 응답에서 content가 배열이기 때문)
        List<Map<String, Object>> textList = List.of(textMap);

        // 최종 fallback Map 반환
        return Map.of("content", textList);
    }
}