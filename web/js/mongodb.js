// Backend API Configuration
const API_CONFIG = {
    baseUrl: 'https://your-app.fly.dev/api', // Replace with your Fly.io URL
    apiKey: 'your-api-key-here' // Same as backend API_KEY
};

/**
 * Make authenticated API request
 */
async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'X-API-Key': API_CONFIG.apiKey
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_CONFIG.baseUrl}${endpoint}`, options);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || `HTTP ${response.status}`);
        }

        return data;
    } catch (error) {
        console.error('API request error:', error);
        throw error;
    }
}

/**
 * Get all bans from backend
 */
async function getAllBans() {
    try {
        const result = await apiRequest('/bans');
        return result.data || [];
    } catch (error) {
        console.error('Error fetching bans:', error);
        return [];
    }
}

/**
 * Get all active bans
 */
async function getActiveBans() {
    try {
        const result = await apiRequest('/bans/active');
        return result.data || [];
    } catch (error) {
        console.error('Error fetching active bans:', error);
        return [];
    }
}

/**
 * Create a new ban
 */
async function createBan(banData) {
    try {
        const result = await apiRequest('/bans', 'POST', banData);
        return result.data;
    } catch (error) {
        console.error('Error creating ban:', error);
        throw error;
    }
}

/**
 * Deactivate a ban (unban)
 */
async function deactivateBan(banId) {
    try {
        const result = await apiRequest(`/bans/${banId}/deactivate`, 'PUT');
        return result.success;
    } catch (error) {
        console.error('Error deactivating ban:', error);
        throw error;
    }
}

/**
 * Generate a random ban ID
 */
function generateBanId() {
    const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
    let banId = '#';
    for (let i = 0; i < 7; i++) {
        banId += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return banId;
}

/**
 * Format date to readable string
 */
function formatDate(dateString) {
    if (!dateString) return 'Never';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Check if a ban has expired
 */
function isBanExpired(ban) {
    if (!ban.expiry) return false; // Permanent ban
    return new Date() > new Date(ban.expiry);
}
