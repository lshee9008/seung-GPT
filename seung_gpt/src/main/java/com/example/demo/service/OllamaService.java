package com.example.demo.service;

import com.example.demo.entity.ChatMessage; // 엔티티 import 필수
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class OllamaService {

    private final String OLLAMA_URL = "http://localhost:11434/api"; 
    private final RestTemplate restTemplate = new RestTemplate();

    public OllamaService() {}

    public List<String> getAvailableModels() {
        try {
            String url = OLLAMA_URL + "/tags";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("models")) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) body.get("models");
                List<String> modelNames = new ArrayList<>();
                for (Map<String, Object> model : models) {
                    modelNames.add((String) model.get("name"));
                }
                return modelNames;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of("llama3");
    }

    // [핵심 수정] 대화 히스토리(history)를 받아서 함께 전송
    public String ask(List<ChatMessage> history, String newUserMsg, String modelName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 1. Ollama Chat 포맷에 맞춰 메시지 리스트 생성
            List<Map<String, String>> messages = new ArrayList<>();

            // 1-1. 과거 대화 기록 추가 (최대 10~20개 정도로 제한하는 것이 좋음)
            for (ChatMessage msg : history) {
                Map<String, String> message = new HashMap<>();
                // 내 DB의 sender를 Ollama role로 변환 (USER -> user, BOT -> assistant)
                message.put("role", "USER".equals(msg.getSender()) ? "user" : "assistant");
                message.put("content", msg.getContent());
                messages.add(message);
            }

            // 1-2. 현재 질문 추가
            Map<String, String> currentMsg = new HashMap<>();
            currentMsg.put("role", "user");
            currentMsg.put("content", newUserMsg);
            messages.add(currentMsg);

            // 2. 요청 바디 구성
            Map<String, Object> body = new HashMap<>();
            String targetModel = (modelName != null && !modelName.isEmpty()) ? modelName : "llama3";
            
            body.put("model", targetModel);
            body.put("messages", messages); // [변경] prompt 대신 messages 사용
            body.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            // [변경] /api/generate -> /api/chat (채팅 전용 엔드포인트 사용)
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_URL + "/chat", entity, Map.class);

            Map<String, Object> resBody = response.getBody();
            if (resBody != null && resBody.containsKey("message")) {
                // 응답 구조가 다름: response.message.content
                Map<String, Object> messageObj = (Map<String, Object>) resBody.get("message");
                return messageObj.get("content").toString();
            }
            return "AI 응답 없음";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "오류 발생: " + e.getMessage();
        }
    }
}