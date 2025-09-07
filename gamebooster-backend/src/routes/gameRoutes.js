import express from 'express';
import { getGameDetails, searchGames, getAppPermissions } from '../controllers/gameController.js';

const router = express.Router();

// Get game details by package name
router.get('/details/:packageName', getGameDetails);

// Search for games
router.get('/search', searchGames);

// Get app permissions
router.get('/:packageName/permissions', getAppPermissions);

export default router;
