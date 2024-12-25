package com.DogProject.repository;

import com.DogProject.entity.Chat;
import com.DogProject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    
    @Query(value = "SELECT * FROM chat t1 WHERE t1.c_idx IN ("
            + "SELECT MAX(t2.c_idx) FROM chat t2 "
            + "GROUP BY CASE "
            + "WHEN t2.sender_midx = :userId THEN t2.receiver_midx "
            + "ELSE t2.sender_midx END) "
            + "AND (t1.sender_midx = :userId OR t1.receiver_midx = :userId) "
            + "ORDER BY t1.send_time DESC", nativeQuery = true)
    List<Chat> findLatestChatsByUserId(@Param("userId") int userId);

    @Query(value = "SELECT * FROM chat t1 WHERE (t1.sender_midx = :#{#user1.MIdx} AND t1.receiver_midx = :#{#user2.MIdx}) OR (t1.sender_midx = :#{#user2.MIdx} AND t1.receiver_midx = :#{#user1.MIdx}) ORDER BY t1.send_time", nativeQuery = true)
    List<Chat> findChatsBetweenUsers(@Param("user1") Member user1, @Param("user2") Member user2);

    @Modifying
    @Query(value = "DELETE FROM chat WHERE (CONCAT(sender_midx, '_', receiver_midx) = :roomId) OR (CONCAT(receiver_midx, '_', sender_midx) = :roomId)", nativeQuery = true)
    void deleteByRoomId(@Param("roomId") String roomId);

    @Query(value = "SELECT COUNT(*) FROM chat WHERE receiver_midx = :userId AND is_read = false", nativeQuery = true)
    int countUnreadMessages(@Param("userId") int userId);

    @Query(value = "SELECT COUNT(*) FROM chat WHERE receiver_midx = :#{#receiver.MIdx} AND is_read = false", nativeQuery = true)
    int countUnreadMessages(@Param("receiver") Member receiver);

    @Query(value = "SELECT COUNT(*) FROM chat WHERE receiver_midx = :userId AND sender_midx = :senderId AND is_read = false", nativeQuery = true)
    int countUnreadMessages(@Param("userId") int userId, @Param("senderId") int senderId);

    @Modifying
    @Query(value = "UPDATE chat SET is_read = true WHERE receiver_midx = :#{#receiver.MIdx} AND sender_midx = :#{#sender.MIdx}", nativeQuery = true)
    void markAsRead(@Param("receiver") Member receiver, @Param("sender") Member sender);
}
