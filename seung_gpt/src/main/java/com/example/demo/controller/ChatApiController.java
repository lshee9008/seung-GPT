package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.OllamaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ChatApiController {

    private final OllamaService ollamaService;
    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;

    public ChatApiController(OllamaService ollamaService, 
                             ChatRoomRepository roomRepository, 
                             ChatMessageRepository messageRepository) {
        this.ollamaService = ollamaService;
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
    }

    // 모델 목록 조회
    @GetMapping("/models")
    public List<String> getModels() {
        return ollamaService.getAvailableModels();
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public List<ChatRoom> getRooms(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) return new ArrayList<>(); 
        return roomRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    // 채팅방 생성
    @PostMapping("/rooms")
    public ChatRoom createRoom(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) throw new RuntimeException("로그인 필요");
        return roomRepository.save(new ChatRoom("새로운 대화", memberId));
    }

    // 채팅방 삭제 (메시지도 함께 삭제)
    @DeleteMapping("/rooms/{roomId}")
    @Transactional
    public Map<String, Boolean> deleteRoom(@PathVariable("roomId") Long roomId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        
        ChatRoom room = roomRepository.findById(roomId).orElse(null);
        if (room != null && room.getMemberId().equals(memberId)) {
            messageRepository.deleteByRoomId(roomId);
            roomRepository.deleteById(roomId);
            return Map.of("success", true);
        }
        return Map.of("success", false);
    }

    // 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessage> getMessages(@PathVariable("roomId") Long roomId) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // [수정된 핵심 부분] 메시지 전송 및 AI 응답
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> payload) {
        String roomIdStr = String.valueOf(payload.get("roomId"));
        Long roomId = Long.parseLong(roomIdStr);
        String userMsg = payload.get("message");
        String modelName = payload.get("model"); 

        // 1. 과거 대화 내역 조회 (Context 유지용)
        // 이 부분이 추가되어야 에러가 해결됩니다!
        List<ChatMessage> history = messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

        // 2. 유저 질문 저장
        messageRepository.save(new ChatMessage(roomId, "USER", userMsg, modelName));
        
        // 3. AI 답변 요청 (파라미터 3개: history, userMsg, modelName)
        // 이제 서비스와 파라미터 개수가 맞아서 에러가 사라집니다.
        String botResponse = ollamaService.ask(history, userMsg, modelName);
        
        // 4. AI 답변 저장
        messageRepository.save(new ChatMessage(roomId, "BOT", botResponse, modelName));

        // 5. 방 제목 업데이트
        ChatRoom room = roomRepository.findById(roomId).orElse(null);
        if(room != null && "새로운 대화".equals(room.getTitle())) {
            String newTitle = userMsg.length() > 15 ? userMsg.substring(0, 15) + "..." : userMsg;
            room.setTitle(newTitle);
            roomRepository.save(room);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("response", botResponse);
        return result;
    }
}