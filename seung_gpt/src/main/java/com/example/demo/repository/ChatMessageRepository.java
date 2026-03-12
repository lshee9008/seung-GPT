package com.example.demo.repository;

import com.example.demo.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 대화 내용 시간순 조회
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
    
    // [필수 추가] 방 번호로 메시지 전체 삭제 (방 삭제 시 사용)
    void deleteByRoomId(Long roomId);
}