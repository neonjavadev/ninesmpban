# Backend API Deployment Guide (Railway)

## Overview

This guide covers deploying the Node.js backend API to **Railway**.

## Prerequisites

- [Railway account](https://railway.app/)
- GitHub account (with this repository pushed)
- MongoDB Atlas cluster with connection string

## Step 1: Prepare Repository

Ensure your backend code is in a GitHub repository (which you have already done!).

## Step 2: Create Project on Railway

1. Go to [Railway Dashboard](https://railway.app/dashboard).
2. Click **"New Project"**.
3. Select **"Deploy from GitHub repo"**.
4. Select your repository (`ninesmpban`).
5. Click **"Add Variables"** before deploying.

## Step 3: Set Environment Variables

Add the following variables in the Railway dashboard:

| Variable | Value |
|----------|-------|
| `MONGODB_URI` | Your MongoDB Atlas connection string (e.g., `mongodb+srv://...`) |
| `API_KEY` | Your secure API key (generate one with `openssl rand -hex 32`) |
| `PORT` | `3000` (Optional, Railway usually detects this or sets its own) |

## Step 4: Deploy

1. Click **"Deploy"**.
2. Railway will detect the `package.json` in the `backend/` folder (you may need to configure the "Root Directory" in Settings if it doesn't detect it automatically).
   - **Note:** Since this is a monorepo (plugin + backend), go to **Settings** > **General** > **Root Directory** and set it to `/backend`.
3. Wait for the build to finish.

## Step 5: specific Settings for Monorepo

Since your backend is in a generic folder:
1. Go to your service **Settings**.
2. Scroll down to **Build**.
3. Set **Root Directory** to `/backend`.
4. Railway will trigger a redeploy.

## Step 6: Generate Public Domain

1. Go to **Settings** > **Networking**.
2. Click **"Generate Domain"** (or add a custom domain).
3. You will get a URL like `ninesmpban-production.up.railway.app`.

## Step 7: Update Plugin & Frontend

### Minecraft Plugin (`config.yml`)

```yaml
api:
  url: "https://your-railway-url.up.railway.app/api"
  key: "your-api-key"
```

### Web Frontend (`web/js/mongodb.js`)

```javascript
const API_CONFIG = {
    baseUrl: 'https://your-railway-url.up.railway.app/api',
    apiKey: 'your-api-key'
};
```

## Troubleshooting

### "Application Error" / Crashes

- Check **Logs** in Railway.
- Ensure `MONGODB_URI` is correct and allowlisted in MongoDB Atlas (Network Access > Allow Access from Anywhere).
- Verify `Root Directory` is set to `/backend`.

### "Module not found"

- Ensure `package.json` includes all dependencies.
- Ensure you set the Root Directory correctly.

## Monitoring

- Railway provides built-in metrics and logs tab.
