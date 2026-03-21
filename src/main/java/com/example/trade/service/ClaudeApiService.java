package com.example.trade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
        this.webClient = webClientBuilder.build();
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
                "system", systemPrompt,       // 역할 정의 (매 요청마다 토큰 소비되므로 최소화)
                "messages", List.of(
                        Map.of("role", "user", "content", userMessage)
                )
        );

        // WebClient로 Claude API POST 요청
        Map response = webClient.post()
                .uri(apiUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // 동기 방식으로 응답 대기

        // Claude 응답 구조: { "content": [ { "type": "text", "text": "응답내용" } ] }
        // content 배열 첫 번째 항목에서 텍스트 추출
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.get("content");

        // 토큰 사용량 로그 출력
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        System.out.println("[Claude 토큰 사용량] input: " + usage.get("input_tokens") + ", output: " + usage.get("output_tokens"));

        return (String) contentList.get(0).get("text");
    }
}