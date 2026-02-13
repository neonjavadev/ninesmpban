package com.ninesmp.bansystem.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ban {
    private String banId; // Random generated ID like #hjknbsa
    private String playerUuid; // Player's UUID
    private String playerName; // Player's username
    private String ipAddress; // Player's IP address
    private String reason; // Ban reason
    private String bannedBy; // Admin who banned
    private Date timestamp; // When the ban was created
    private Date expiry; // Null for permanent, timestamp for temp bans
    private boolean active; // Whether the ban is currently active
    private String source; // "game" or "web" - where the ban originated

    /**
     * Check if this is a permanent ban
     */
    public boolean isPermanent() {
        return expiry == null;
    }

    /**
     * Check if this ban has expired
     */
    public boolean isExpired() {
        if (isPermanent())
            return false;
        return new Date().after(expiry);
    }

    /**
     * Get formatted expiry time
     */
    public String getFormattedExpiry() {
        if (isPermanent())
            return "Never";
        return expiry.toString();
    }
}
