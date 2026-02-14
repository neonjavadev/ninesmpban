// Dashboard state
let allBans = [];
let filteredBans = [];
let refreshInterval;

// Initialize dashboard
document.addEventListener('DOMContentLoaded', async () => {
    await loadBans();
    setupEventListeners();
    startAutoRefresh();
});

/**
 * Load all bans from MongoDB
 */
async function loadBans() {
    try {
        allBans = await getAllBans();
        filteredBans = [...allBans];
        updateStatistics();
        renderBansTable();
    } catch (error) {
        console.error('Error loading bans:', error);
        showError('Failed to load bans. Please check your MongoDB configuration.');
    }
}

/**
 * Update statistics cards
 */
function updateStatistics() {
    const activeBans = allBans.filter(ban => ban.active && !isBanExpired(ban));
    const tempBans = activeBans.filter(ban => ban.expiry !== null);

    document.getElementById('totalBans').textContent = allBans.length;
    document.getElementById('activeBans').textContent = activeBans.length;
    document.getElementById('tempBans').textContent = tempBans.length;
}

/**
 * Render bans table
 */
function renderBansTable() {
    const tbody = document.getElementById('bansTableBody');

    if (filteredBans.length === 0) {
        tbody.innerHTML = `
            <tr class="loading-row">
                <td colspan="10">
                    <p>No bans found</p>
                </td>
            </tr>
        `;
        return;
    }

    // Sort by timestamp (newest first)
    const sortedBans = [...filteredBans].sort((a, b) => {
        return new Date(b.timestamp) - new Date(a.timestamp);
    });

    tbody.innerHTML = sortedBans.map(ban => {
        const isExpired = isBanExpired(ban);
        const isActive = ban.active && !isExpired;

        return `
            <tr>
                <td><span class="ban-id">${ban.banId}</span></td>
                <td>${ban.playerName}</td>
                <td><code style="font-size: 11px; color: var(--text-muted);">${ban.playerUuid.substring(0, 18)}...</code></td>
                <td>${ban.ipAddress || 'Unknown'}</td>
                <td>${ban.reason}</td>
                <td>${ban.bannedBy}</td>
                <td>${formatDate(ban.timestamp)}</td>
                <td>
                    ${ban.expiry
                ? `<span class="badge badge-temporary">${formatDate(ban.expiry)}</span>`
                : `<span class="badge badge-permanent">Permanent</span>`
            }
                </td>
                <td><span class="badge badge-${(ban.type || 'BAN').toLowerCase()}">${(ban.type || 'BAN')}</span></td>
                <td><span class="badge badge-${ban.source}">${ban.source}</span></td>
                <td>
                    ${isActive
                ? `<button class="btn-unban" onclick="unbanPlayer('${ban.banId}')">Unban</button>`
                : `<span style="color: var(--text-muted); font-size: 13px;">Inactive</span>`
            }
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Search functionality
    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        filteredBans = allBans.filter(ban =>
            ban.playerName.toLowerCase().includes(query) ||
            ban.playerUuid.toLowerCase().includes(query) ||
            ban.banId.toLowerCase().includes(query) ||
            (ban.ipAddress && ban.ipAddress.toLowerCase().includes(query))
        );
        renderBansTable();
    });

    // New ban button
    const newBanBtn = document.getElementById('newBanBtn');
    const newBanModal = document.getElementById('newBanModal');
    const closeModal = document.getElementById('closeModal');
    const cancelBan = document.getElementById('cancelBan');

    newBanBtn.addEventListener('click', () => {
        newBanModal.classList.add('active');
    });

    closeModal.addEventListener('click', () => {
        newBanModal.classList.remove('active');
    });

    cancelBan.addEventListener('click', () => {
        newBanModal.classList.remove('active');
    });

    // Close modal on outside click
    newBanModal.addEventListener('click', (e) => {
        if (e.target === newBanModal) {
            newBanModal.classList.remove('active');
        }
    });

    // Ban type change
    const banType = document.getElementById('banType');
    const expiryGroup = document.getElementById('expiryGroup');
    banType.addEventListener('change', (e) => {
        if (e.target.value === 'temporary') {
            expiryGroup.style.display = 'block';
        } else {
            expiryGroup.style.display = 'none';
        }
    });

    // New ban form submission
    const newBanForm = document.getElementById('newBanForm');
    newBanForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        await createNewBan();
    });
}

/**
 * Create a new ban from the web interface
 */
async function createNewBan() {
    const username = document.getElementById('banUsername').value;
    let uuid = document.getElementById('banUuid').value;
    const ip = document.getElementById('banIp').value || 'Unknown';
    const reason = document.getElementById('banReason').value;
    const punishmentType = document.getElementById('punishmentType').value;
    const banType = document.getElementById('banType').value;
    const durationStr = document.getElementById('banExpiry').value;

    let expiry = null;
    if (banType === 'temporary' && durationStr) {
        const duration = parseDuration(durationStr);
        if (duration > 0) {
            expiry = new Date(Date.now() + duration).toISOString();
        } else {
            showError("Invalid duration format. Use 10m, 1h, 1d etc.");
            return;
        }
    }

    // Auto-resolve UUID if missing
    if (!uuid) {
        try {
            showSuccess(`Resolving UUID for ${username}...`);
            const response = await fetch(`https://api.ashcon.app/mojang/v2/user/${username}`);
            if (!response.ok) throw new Error('Player not found');
            const data = await response.json();
            uuid = data.uuid;
        } catch (error) {
            showError(`Could not find player: ${username}`);
            return;
        }
    }

    const banData = {
        banId: generateBanId(),
        playerUuid: uuid,
        playerName: username,
        ipAddress: ip,
        reason: reason,
        bannedBy: 'admin',
        timestamp: new Date().toISOString(),
        expiry: expiry,
        active: true,
        source: 'web',
        type: punishmentType
    };

    try {
        await createBan(banData);

        // Close modal
        document.getElementById('newBanModal').classList.remove('active');

        // Reset form
        document.getElementById('newBanForm').reset();

        // Reload bans
        await loadBans();

        // Show success message
        showSuccess(`Successfully banned ${username} (${banData.banId})`);
    } catch (error) {
        showError('Failed to create ban. Please try again.');
    }
}

/**
 * Unban a player
 */
async function unbanPlayer(banId) {
    if (!confirm(`Are you sure you want to unban ${banId}?`)) {
        return;
    }

    try {
        await deactivateBan(banId);
        await loadBans();
        showSuccess(`Successfully unbanned ${banId}`);
    } catch (error) {
        showError('Failed to unban player. Please try again.');
    }
}

/**
 * Start auto-refresh
 */
function startAutoRefresh() {
    // Refresh every 5 seconds
    refreshInterval = setInterval(async () => {
        await loadBans();
    }, 5000);
}

/**
 * Show success message
 */
function showSuccess(message) {
    // Create toast notification
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, rgba(16, 185, 129, 0.9), rgba(5, 150, 105, 0.9));
        color: white;
        padding: 16px 24px;
        border-radius: 12px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
        z-index: 10000;
        animation: slideIn 0.3s ease;
        font-weight: 600;
    `;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/**
 * Show error message
 */
function showError(message) {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, rgba(239, 68, 68, 0.9), rgba(220, 38, 38, 0.9));
        color: white;
        padding: 16px 24px;
        border-radius: 12px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
        z-index: 10000;
        animation: slideIn 0.3s ease;
        font-weight: 600;
    `;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Add toast animations
const toastStyle = document.createElement('style');
toastStyle.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(toastStyle);

/**
 * Parse duration string (10s, 10m, 10h, 1d) to milliseconds
 */
function parseDuration(duration) {
    const regex = /(\d+)([smhd])/g;
    let totalMillis = 0;
    let match;
    let hasMatch = false;

    while ((match = regex.exec(duration.toLowerCase())) !== null) {
        hasMatch = true;
        const value = parseInt(match[1]);
        const unit = match[2];

        switch (unit) {
            case 's': totalMillis += value * 1000; break;
            case 'm': totalMillis += value * 60 * 1000; break;
            case 'h': totalMillis += value * 60 * 60 * 1000; break;
            case 'd': totalMillis += value * 24 * 60 * 60 * 1000; break;
        }
    }

    return hasMatch ? totalMillis : -1;
}
