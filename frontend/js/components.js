/**
 * CourseConnect UI Components: Modals, Toasts, Confirmations
 */
const UI = {
  showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const iconName = type === 'success' ? 'check_circle' : (type === 'error' ? 'error' : 'info');
    toast.innerHTML = `
      <span class="material-symbols-outlined" style="font-size: 20px;">${iconName}</span>
      <span>${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 10);

    // Auto-remove
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => toast.remove(), 250);
    }, 4000);
  },

  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('open');
      document.body.style.overflow = 'hidden';
    }
  },

  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('open');
      document.body.style.overflow = '';
    }
  },

  confirmDialog({ title, message, confirmText = 'Confirm', confirmClass = 'btn-primary', onConfirm }) {
    document.getElementById('confirmModalTitle').innerText = title;
    document.getElementById('confirmModalMessage').innerText = message;
    
    const confirmBtn = document.getElementById('confirmModalActionBtn');
    confirmBtn.className = `btn ${confirmClass}`;
    confirmBtn.innerText = confirmText;

    // Remove old listeners
    const newBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

    newBtn.addEventListener('click', () => {
      UI.closeModal('confirmModal');
      if (typeof onConfirm === 'function') {
        onConfirm();
      }
    });

    UI.openModal('confirmModal');
  }
};

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
