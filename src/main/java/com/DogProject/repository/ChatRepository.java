package com.DogProject.repository;

import com.DogProject.entity.Chat;
import com.DogProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    
    @Query("SELECT c FROM Chat c WHERE (c.sender = :user1 AND c.receiver = :user2) OR (c.sender = :user2 AND c.receiver = :user1) ORDER BY c.sendTime")
    List<Chat> findChatsBetweenUsers(@Param("user1") Member user1, @Param("user2") Member user2);

    @Query(value = "SELECT * FROM chat t1 WHERE t1.c_idx IN ("
            + "SELECT MAX(t2.c_idx) FROM chat t2 "
            + "GROUP BY CASE "
            + "WHEN t2.sender_midx = :userId THEN t2.receiver_midx "
            + "ELSE t2.sender_midx END) "
            + "AND (t1.sender_midx = :userId OR t1.receiver_midx = :userId) "
            + "ORDER BY t1.send_time DESC", nativeQuery = true)
    List<Chat> findLatestChatsByUserId(@Param("userId") int userId);

    @Modifying
    @Query("DELETE FROM Chat c WHERE (CONCAT(c.sender.mIdx, '_', c.receiver.mIdx) = :roomId) OR (CONCAT(c.receiver.mIdx, '_', c.sender.mIdx) = :roomId)")
    void deleteByRoomId(@Param("roomId") String roomId);
}
