# NineSMP Ban System - Backend API Setup

## Quick Start

### 1. Backend (Railway)

1. Connect your GitHub repo to Railway.
2. Set Root Directory to `/backend`.
3. Add variables: `MONGODB_URI`, `API_KEY`.
4. Deploy!

Your API will be at: `https://your-project.up.railway.app`

### 2. Minecraft Plugin

Update `config.yml`:

```yaml
api:
  url: "https://your-project.up.railway.app/api"
  key: "your-api-key"
```

Build and install:

```bash
mvn clean package
# Copy target/bansystem-1.0.0.jar to plugins/ folder
```

### 3. Web Frontend

Update `web/js/mongodb.js`:

```javascript
const API_CONFIG = {
    baseUrl: 'https://your-project.up.railway.app/api',
    apiKey: 'your-api-key'
};
```

Deploy to Netlify:
- Drag `web/` folder to Netlify
- Done!

## Architecture

```
┌─────────────┐
│  MC Plugin  │──┐
└─────────────┘  │
                 ├──→ ┌────────────┐ ──→ ┌──────────┐
┌─────────────┐  │    │ Backend API│     │ MongoDB  │
│ Web Frontend│──┘    │  (Railway) │     │  Atlas   │
└─────────────┘       └────────────┘     └──────────┘
```

## API Endpoints

All endpoints require `X-API-Key` header.

- `GET /api/health` - Health check
- `GET /api/bans` - Get all bans
- `GET /api/bans/active` - Get active bans
- `GET /api/bans/:banId` - Get specific ban
- `GET /api/bans/uuid/:uuid` - Get ban by UUID
- `GET /api/bans/ip/:ip` - Get ban by IP
- `POST /api/bans` - Create new ban
- `PUT /api/bans/:banId/deactivate` - Unban
- `POST /api/bans/check-expired` - Check expired bans

## Commands

- `/ban <player> <reason>` - Permanent ban
- `/tempban <player> <time> <reason>` - Temporary ban
- `/kick <player> <reason>` - Kick player

## Configuration

### Customizable Ban Reasons

Edit `config.yml`:

```yaml
ban-reasons:
  - "Hacking"
  - "Exploiting"
  - "Your_Custom_Reason"
```

### Ban Messages

Customize in `config.yml`:

```yaml
messages:
  ban-format: |
    &c&lConnection Lost
    
    &cYou were kicked from lobby: %reason%
    &7Time left: &fPermanent
    
    &7Ban ID: &f%banid%
```

## Deployment Guides

- **Backend**: [backend/DEPLOY.md](file:///home/neonjavadev/projects/ninesmp/bansystem/backend/DEPLOY.md)
- **Full Setup**: [SETUP.md](file:///home/neonjavadev/projects/ninesmp/bansystem/SETUP.md)

## Features

✅ Cross-server synchronization
✅ IP + UUID banning
✅ Temporary & permanent bans
✅ Web admin panel
✅ Customizable messages
✅ Tab completion
✅ REST API
✅ Free hosting (Fly.io + Netlify)
