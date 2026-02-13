# MongoDB Realm Setup - Quick Guide

We are using the **MongoDB Realm Web SDK**, which is the modern, supported way to connect from a browser.

## Step 1: Create App Services App

1. Go to your MongoDB Atlas dashboard.
2. Click **"App Services"** tab.
3. Select **"Build your own App"** template.
4. Click **"Next"**.
5. Name it: `ninesmp-ban-app`
6. Link to your cluster: `ninesmp`
7. Click **"Create App Services"**

![App Services](/home/neonjavadev/.gemini/antigravity/brain/4d20c8d2-0104-4347-a3e7-ae2a69dc3039/uploaded_media_1771006620885.png)

## Step 2: Get App ID

1. On the App Services dashboard (top left), look for **App ID**.
2. copy it (e.g., `application-0-xyz`).

## Step 3: Enable API Key Authentication

1. In the left menu, go to **Authentication** > **Providers**.
2. Find **API Keys** and click **Edit**.
3. Toggle it **ON**.
4. Click **Save Draft**.

## Step 4: Create an API Key

1. Still in **authentication/Providers/API Keys**.
2. Click **Create API Key**.
3. Name it: `web-admin`.
4. **COPY THE KEY** immediately. You won't see it again.
5. Click **Create**.

## Step 5: Configure Permissions (Rules)

1. In the left menu, go to **Data Access** > **Rules**.
2. Click on your collection (`ninesmpban.bans`).
3. Select a template: **"Read and Write All"** (simplest for now).
   - Or "Users can read all data, but only write their own data" (but you are admin, so Read/Write All is fine for this single-user tool).
4. Click **Add Preset Role**.
5. Click **Save Draft** and then **REVIEW DEPLOYMENT** to make changes live.

## Step 6: Update Web Panel

1. Open `web/js/mongodb.js`.
2. Update config:
   ```javascript
   const REALM_CONFIG = {
       appId: 'application-0-xyz', // Your App ID from Step 2
       apiKey: 'your-api-key'      // Your API Key from Step 4
   };
   ```

That's it! No more "Data API" deprecation warnings.
