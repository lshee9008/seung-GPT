package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private String sender; // "USER" or "BOT"
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String modelName; // [추가] 사용된 모델명 저장
    
    private LocalDateTime createdAt;

    public ChatMessage() {}

    // 생성자 수정 (modelName 추가)
    public ChatMessage(Long roomId, String sender, String content, String modelName) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.modelName = modelName;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getModelName() { return modelName; } // Getter 추가
    public LocalDateTime getCreatedAt() { return createdAt; }
}