package com.example.trade.restController;

import com.example.trade.service.ClaudeApiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class QnaAiRestController {

    private final ClaudeApiService claudeApiService;

    public QnaAiRestController(ClaudeApiService claudeApiService) {
        this.claudeApiService = claudeApiService;
    }

    @PostMapping("/admin/ai/qna-draft")
    public Map<String, String> generateQnaDraft(@RequestBody Map<String, String> request) {

        String boardTitle = request.get("boardTitle");
        String boardContent = request.get("boardContent");

        // 컨텍스트 오염 방지: HTML 태그 제거 후 순수 텍스트만 Claude에게 전달
        // HTML 태그가 포함된 채로 넘기면 태그 자체도 토큰으로 소비됨
        // Claude가 HTML 구조를 해석하려다 문의 내용 파악 정확도가 낮아질 수 있음
        String cleanContent = boardContent.replaceAll("<[^>]*>", "")  // HTML 태그 제거
                                          .replaceAll("&nbsp;", " ")  // HTML 엔티티 변환
                                          .replaceAll("\\s+", " ")    // 연속 공백 정리
                                          .trim();

        // token save 1 : 시스템 프롬프트 최소화, 역할만 간결하게 정의
        // String systemPrompt = "당신은 B2B 무역 플랫폼 고객센터 담당자입니다. 고객 문의에 대한 답변 초안을 작성합니다. 답변만 출력하세요.";
        // String systemPrompt = "B2B 무역 플랫폼 고객센터 담당자. 답변 초안만 출력.";
        String systemPrompt = "B2B 무역 플랫폼 고객센터 담당자. 3문장 이내 답변 초안만 출력. 인사말 생략. 문장마다 줄바꿈 적용.";

        // token save 2 : 필요한 정보(제목, 본문)만 전달 - 불필요한 메타데이터 제외
        String userMessage = "문의 제목: " + boardTitle + "\n 문의 내용: " + cleanContent + "\n\n위 문의에 대한 답변 초안을 작성해주세요.";

        String draft = claudeApiService.call(systemPrompt, userMessage);

        return Map.of("draft", draft);
    }
}
