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
    private String type; // "BAN" or "MUTE"

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

    /**
     * Get formatted time left until expiry
     */
    public String getTimeLeft() {
        if (isPermanent()) {
            return "Permanent";
        }

        long diff = expiry.getTime() - System.currentTimeMillis();
        if (diff <= 0) {
            return "Expired";
        }

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
