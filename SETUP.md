# NineSMP Ban System - Complete Setup Guide

This guide will walk you through setting up the entire ban system, from MongoDB Atlas to deploying the web panel.

## Table of Contents

1. [MongoDB Atlas Setup](#1-mongodb-atlas-setup)
2. [MongoDB Data API Configuration](#2-mongodb-data-api-configuration)
3. [Plugin Installation](#3-plugin-installation)
4. [Web Panel Deployment](#4-web-panel-deployment)
5. [Testing](#5-testing)

---

## 1. MongoDB Atlas Setup

### Create a Free MongoDB Atlas Cluster

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Sign up or log in
3. Click **"Build a Database"**
4. Select **"M0 Free"** tier
5. Choose a cloud provider and region (closest to your servers)
6. Name your cluster (e.g., `ninesmp`)
7. Click **"Create"**

### Configure Network Access

1. In Atlas dashboard, go to **"Network Access"**
2. Click **"Add IP Address"**
3. Click **"Allow Access from Anywhere"** (or add your server IPs)
4. Click **"Confirm"**

### Create Database User

1. Go to **"Database Access"**
2. Click **"Add New Database User"**
3. Username: `ninesmp`
4. Password: `hemanthsp132` (or use your own)
5. Database User Privileges: **"Read and write to any database"**
6. Click **"Add User"**

### Get Connection String

1. Go to **"Database"** → Click **"Connect"**
2. Select **"Connect your application"**
3. Copy the connection string (looks like):
   ```
   mongodb+srv://ninesmp:<password>@ninesmp.c6wco7a.mongodb.net/
   ```
4. Replace `<password>` with your actual password
5. Add `/ninesmpban` at the end:
   ```
   mongodb+srv://ninesmp:hemanthsp132@ninesmp.c6wco7a.mongodb.net/ninesmpban
   ```

---

## 2. MongoDB Data API Configuration

### Enable Data API

1. In Atlas dashboard, go to **"App Services"**
2. Click **"Create a New App"**
3. Name it `ninesmp-ban-api`
4. Link it to your cluster
5. Click **"Create"**

### Configure Data API

1. In your App, go to **"HTTPS Endpoints"**
2. Click **"Data API"**
3. Click **"Enable the Data API"**
4. Note the **Data API URL** (looks like):
   ```
   https://data.mongodb-api.com/app/YOUR_APP_ID/endpoint/data/v1
   ```

### Create API Key

1. Go to **"Authentication"** → **"API Keys"**
2. Click **"Create API Key"**
3. Name it `web-panel-key`
4. Copy the API key (you won't see it again!)
5. Click **"Save"**

### Configure CORS

1. Go to **"App Settings"** → **"HTTPS Endpoints"**
2. Under **"Data API"**, find **"CORS"**
3. Add your Netlify domain (or `*` for testing):
   ```
   https://your-site-name.netlify.app
   ```

---

## 3. Plugin Installation

### Build the Plugin

1. Make sure you have Java 17+ and Maven installed
2. Navigate to the project directory:
   ```bash
   cd /home/neonjavadev/projects/ninesmp/bansystem
   ```
3. Build with Maven:
   ```bash
   mvn clean package
   ```
4. The plugin JAR will be in `target/bansystem-1.0.0.jar`

### Install on Server

1. Copy `bansystem-1.0.0.jar` to your server's `plugins/` folder
2. Start the server (this will generate the config)
3. Stop the server
4. Edit `plugins/NineSMPBanSystem/config.yml`:
   ```yaml
   mongodb:
     uri: "mongodb+srv://ninesmp:hemanthsp132@ninesmp.c6wco7a.mongodb.net/ninesmpban"
     database: "ninesmpban"
     collection: "bans"
   
   sync:
     interval: 5
   
   messages:
     ban-format: |
       &c&lConnection Lost
       
       &cYou were kicked from lobby: %reason%
       &7Time left: &fPermanent
       
       &7Ban ID: &f%banid%
       &7You may be able to appeal this ban on
       &7discord.gg/yourserver
     # ... (rest of messages)
   ```
5. Customize the Discord link and messages as needed
6. Start the server

### Install on Multiple Servers

Repeat the above steps for each server, using the **same MongoDB connection string**. This ensures all servers share the same ban database.

---

## 4. Web Panel Deployment

### Update MongoDB Configuration

1. Open `web/js/mongodb.js`
2. Update the configuration:
   ```javascript
   const MONGODB_CONFIG = {
       dataApiUrl: 'https://data.mongodb-api.com/app/YOUR_APP_ID/endpoint/data/v1',
       apiKey: 'YOUR_API_KEY_HERE',
       dataSource: 'ninesmp',
       database: 'ninesmpban',
       collection: 'bans'
   };
   ```
3. Replace `YOUR_APP_ID` with your actual App ID
4. Replace `YOUR_API_KEY_HERE` with your API key from step 2

### Deploy to Netlify

#### Option 1: Drag and Drop

1. Go to [Netlify](https://www.netlify.com/)
2. Sign up or log in
3. Click **"Add new site"** → **"Deploy manually"**
4. Drag the entire `web/` folder to the upload area
5. Wait for deployment
6. Your site will be live at `https://random-name.netlify.app`

#### Option 2: Git Deploy

1. Create a Git repository with the `web/` folder
2. Push to GitHub
3. In Netlify, click **"Add new site"** → **"Import from Git"**
4. Connect your repository
5. Set build settings:
   - Base directory: `web`
   - Build command: (leave empty)
   - Publish directory: `.`
6. Click **"Deploy"**

### Update CORS Settings

1. Go back to MongoDB Atlas App Services
2. Update CORS settings with your actual Netlify URL:
   ```
   https://your-actual-site-name.netlify.app
   ```

---

## 5. Testing

### Test Plugin

1. Join your Minecraft server
2. Run command:
   ```
   /ban TestPlayer Hacking
   ```
3. Check MongoDB Atlas:
   - Go to **"Database"** → **"Browse Collections"**
   - You should see a new document in `ninesmpban.bans`

### Test Cross-Server Sync

1. Ban a player on Server A
2. Wait 5 seconds
3. Try to join Server B with that player
4. Player should be kicked with the ban message

### Test Web Panel

1. Go to your Netlify URL
2. Login with:
   - Username: `admin`
   - Password: `ninesmpdev2026`
3. You should see the ban you created
4. Try unbanning from the web panel
5. Player should be able to join the server

### Test Web Ban

1. In the web panel, click **"New Ban"**
2. Fill in player details
3. Click **"Create Ban"**
4. Try to join the server with that player
5. Player should be kicked within 5 seconds

---

## Troubleshooting

### Plugin won't connect to MongoDB

- Check your connection string in `config.yml`
- Verify Network Access allows your server IP
- Check server logs for error messages

### Web panel shows "Failed to load bans"

- Open browser console (F12) to see errors
- Verify API key and App ID in `mongodb.js`
- Check CORS settings in Atlas App Services
- Verify Data API is enabled

### Bans not syncing between servers

- Verify all servers use the same MongoDB connection string
- Check sync interval in config (default 5 seconds)
- Look for errors in server logs

### Player can still join after ban

- Wait 5 seconds for sync to occur
- Check if ban is marked as `active: true` in MongoDB
- Verify player UUID matches the ban record

---

## Security Notes

> [!WARNING]
> The admin credentials are hardcoded in the frontend JavaScript. Anyone who inspects the code can see them. This is a limitation of the "no backend" requirement.

> [!WARNING]
> The MongoDB API key is exposed in the frontend. Restrict it to:
> - Only the `ninesmpban` database
> - Read/Write permissions only
> - IP allowlist if possible

For production use, consider adding a backend API layer for better security.

---

## Support

If you encounter issues:
1. Check server logs: `logs/latest.log`
2. Check browser console: Press F12
3. Verify MongoDB Atlas connection
4. Ensure all configuration values are correct

---

## Next Steps

- Customize ban messages in `config.yml`
- Update Discord link for ban appeals
- Consider adding more admin users (requires code modification)
- Set up automated backups of MongoDB database
