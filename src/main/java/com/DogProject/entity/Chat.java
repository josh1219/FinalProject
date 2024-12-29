package com.DogProject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "chat")
@Getter
@Setter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("채팅 고유 ID")
    private int cIdx;

    @Comment("채팅 내용")
    @Column(name = "content", columnDefinition = "VARCHAR(3000)")
    private String content;

    @Comment("채팅 전송 시간")
    @Column(name = "send_time")
    private String sendTime;

    @ManyToOne
    @JoinColumn(name = "sender_midx")
    @Comment("채팅을 보낸 회원")
    private Member sender;

    @ManyToOne
    @JoinColumn(name = "receiver_midx")
    @Comment("채팅을 받은 회원")
    private Member receiver;

    @Comment("읽음 여부")
    @Column(name = "is_read")
    private boolean isRead;

    @Transient // DB에 저장하지 않는 필드
    private int unreadCount;
}
