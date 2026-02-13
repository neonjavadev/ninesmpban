const express = require('express');
const cors = require('cors');
const { MongoClient } = require('mongodb');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// MongoDB connection
let db;
let bansCollection;

const connectDB = async () => {
    try {
        const client = new MongoClient(process.env.MONGODB_URI);
        await client.connect();
        db = client.db('ninesmpban');
        bansCollection = db.collection('bans');
        console.log('✅ Connected to MongoDB');
    } catch (error) {
        console.error('❌ MongoDB connection error:', error);
        process.exit(1);
    }
};

// API Key authentication middleware
const authenticateApiKey = (req, res, next) => {
    const apiKey = req.headers['x-api-key'];

    if (!apiKey || apiKey !== process.env.API_KEY) {
        return res.status(401).json({ error: 'Unauthorized - Invalid API Key' });
    }

    next();
};

// Health check (no auth required)
app.get('/api/health', (req, res) => {
    res.json({
        status: 'ok',
        timestamp: new Date().toISOString(),
        mongodb: db ? 'connected' : 'disconnected'
    });
});

// Get all bans
app.get('/api/bans', authenticateApiKey, async (req, res) => {
    try {
        const bans = await bansCollection.find({}).toArray();
        res.json({ success: true, data: bans });
    } catch (error) {
        console.error('Error fetching bans:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get active bans only
app.get('/api/bans/active', authenticateApiKey, async (req, res) => {
    try {
        const bans = await bansCollection.find({ active: true }).toArray();
        res.json({ success: true, data: bans });
    } catch (error) {
        console.error('Error fetching active bans:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get specific ban by ID
app.get('/api/bans/:banId', authenticateApiKey, async (req, res) => {
    try {
        const ban = await bansCollection.findOne({ banId: req.params.banId });

        if (!ban) {
            return res.status(404).json({ success: false, error: 'Ban not found' });
        }

        res.json({ success: true, data: ban });
    } catch (error) {
        console.error('Error fetching ban:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get ban by UUID
app.get('/api/bans/uuid/:uuid', authenticateApiKey, async (req, res) => {
    try {
        const ban = await bansCollection.findOne({
            playerUuid: req.params.uuid,
            active: true
        });

        res.json({ success: true, data: ban });
    } catch (error) {
        console.error('Error fetching ban by UUID:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get ban by IP
app.get('/api/bans/ip/:ip', authenticateApiKey, async (req, res) => {
    try {
        const ban = await bansCollection.findOne({
            ipAddress: req.params.ip,
            active: true
        });

        res.json({ success: true, data: ban });
    } catch (error) {
        console.error('Error fetching ban by IP:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Create new ban
app.post('/api/bans', authenticateApiKey, async (req, res) => {
    try {
        const banData = {
            ...req.body,
            timestamp: new Date(),
            active: true
        };

        const result = await bansCollection.insertOne(banData);

        res.status(201).json({
            success: true,
            data: { ...banData, _id: result.insertedId }
        });
    } catch (error) {
        console.error('Error creating ban:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Deactivate ban (unban)
app.put('/api/bans/:banId/deactivate', authenticateApiKey, async (req, res) => {
    try {
        const result = await bansCollection.updateOne(
            { banId: req.params.banId },
            { $set: { active: false } }
        );

        if (result.matchedCount === 0) {
            return res.status(404).json({ success: false, error: 'Ban not found' });
        }

        res.json({ success: true, message: 'Ban deactivated successfully' });
    } catch (error) {
        console.error('Error deactivating ban:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Check for expired bans and deactivate them
app.post('/api/bans/check-expired', authenticateApiKey, async (req, res) => {
    try {
        const result = await bansCollection.updateMany(
            {
                active: true,
                expiry: { $ne: null, $lt: new Date() }
            },
            { $set: { active: false } }
        );

        res.json({
            success: true,
            message: `Deactivated ${result.modifiedCount} expired bans`
        });
    } catch (error) {
        console.error('Error checking expired bans:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({ success: false, error: 'Internal server error' });
});

// Start server
const startServer = async () => {
    await connectDB();

    app.listen(PORT, '0.0.0.0', () => {
        console.log(`🚀 Server running on port ${PORT}`);
        console.log(`📡 Health check: http://localhost:${PORT}/api/health`);
    });
};

startServer();
