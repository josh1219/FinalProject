// 채팅 알림 관련 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Chat notification script loaded');
    
    const chatBadge = document.getElementById('chatBadge');
    if (!chatBadge) return;

    const userId = chatBadge.getAttribute('data-user-id');
    if (!userId) {
        console.error('User ID not found');
        return;
    }
    
    // WebSocket 연결 (사용자 ID를 쿼리 파라미터로 전달)
    const socket = new WebSocket(`ws://edu.e-tops.co.kr/ws/notification?userId=${userId}`);
    
    socket.onopen = function() {
        console.log('알림 웹소켓 연결됨 (사용자 ID:', userId, ')');
    };
    
    socket.onmessage = function(event) {
        console.log('알림 메시지 수신:', event.data);
        const data = JSON.parse(event.data);
        updateBadge(data.unreadCount);
    };
    
    socket.onerror = function(error) {
        console.error('웹소켓 오류:', error);
    };
    
    socket.onclose = function() {
        console.log('웹소켓 연결 종료');
    };
    
    function updateBadge(count) {
        if (count > 0) {
            chatBadge.textContent = count;
            chatBadge.style.display = 'block';
            // 새 메시지 애니메이션 추가
            chatBadge.classList.add('new-message');
            // 애니메이션이 끝나면 클래스 제거
            setTimeout(() => {
                chatBadge.classList.remove('new-message');
            }, 1000);
        } else {
            chatBadge.style.display = 'none';
        }
    }
    
    // 페이지 언로드 시 웹소켓 연결 종료
    window.addEventListener('beforeunload', function() {
        if (socket.readyState === WebSocket.OPEN) {
            socket.close();
        }
    });
});
