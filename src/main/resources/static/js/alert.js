function showAlert(message) {
    const alertContainer = document.createElement('div');
    alertContainer.className = 'alert-container';
    
    alertContainer.innerHTML = `
        <div class="alert-content">
            <div class="alert-message">${message}</div>
            <button class="alert-close">확인</button>
        </div>
    `;
    
    document.body.appendChild(alertContainer);
    
    const closeButton = alertContainer.querySelector('.alert-close');
    closeButton.addEventListener('click', () => {
        alertContainer.style.animation = 'slideOut 0.3s ease-out forwards';
        setTimeout(() => {
            document.body.removeChild(alertContainer);
        }, 300);
    });
    
    // 3초 후 자동으로 닫기
    setTimeout(() => {
        if (document.body.contains(alertContainer)) {
            alertContainer.style.animation = 'slideOut 0.3s ease-out forwards';
            setTimeout(() => {
                if (document.body.contains(alertContainer)) {
                    document.body.removeChild(alertContainer);
                }
            }, 300);
        }
    }, 3000);
}

// window.alert를 재정의하여 모든 alert 호출을 새로운 디자인으로 변경
window.alert = showAlert;
