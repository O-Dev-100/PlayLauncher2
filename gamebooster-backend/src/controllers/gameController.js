import gplay from 'google-play-scraper';
import { logger } from '../index.js';

/**
 * Get detailed information about a game by its package name
 */
export const getGameDetails = async (req, res, next) => {
  try {
    const { packageName } = req.params;
    const { lang = 'en', country = 'us' } = req.query;

    logger.info(`Fetching details for package: ${packageName}`);
    
    const app = await gplay.app({
      appId: packageName,
      lang: lang,
      country: country
    });

    // Format the response
    const response = {
      id: app.appId,
      title: app.title,
      description: app.description,
      descriptionHTML: app.descriptionHTML,
      summary: app.summary,
      installs: app.installs,
      minInstalls: app.minInstalls,
      maxInstalls: app.maxInstalls,
      score: app.score,
      scoreText: app.scoreText,
      ratings: app.ratings,
      reviews: app.reviews,
      histogram: app.histogram,
      price: app.price,
      free: app.free,
      currency: app.currency,
      priceText: app.priceText,
      offersIAP: app.offersIAP,
      size: app.size,
      androidVersion: app.androidVersion,
      androidVersionText: app.androidVersionText,
      developer: {
        id: app.developer,
        email: app.developerEmail,
        website: app.developerWebsite,
        address: app.developerAddress
      },
      genre: app.genre,
      genreId: app.genreId,
      icon: app.icon,
      headerImage: app.headerImage,
      screenshots: app.screenshots,
      video: app.video,
      videoImage: app.videoImage,
      contentRating: app.contentRating,
      adSupported: app.adSupported,
      released: app.released,
      updated: app.updated,
      version: app.version,
      recentChanges: app.recentChanges,
      comments: app.comments,
      url: app.url,
      similar: app.similarApps
    };

    res.json(response);
  } catch (error) {
    logger.error(`Error fetching game details: ${error.message}`);
    next(error);
  }
};

/**
 * Search for games on Google Play Store
 */
export const searchGames = async (req, res, next) => {
  try {
    const { query, num = 20, lang = 'en', country = 'us' } = req.query;
    
    if (!query) {
      return res.status(400).json({ error: 'Search query is required' });
    }

    logger.info(`Searching for games with query: ${query}`);
    
    const results = await gplay.search({
      term: query,
      num: parseInt(num),
      lang: lang,
      country: country,
      fullDetail: true
    });

    // Format the response
    const formattedResults = results.map(app => ({
      id: app.appId,
      title: app.title,
      summary: app.summary,
      installs: app.installs,
      score: app.score,
      scoreText: app.scoreText,
      price: app.price,
      free: app.free,
      currency: app.currency,
      priceText: app.priceText,
      size: app.size,
      androidVersion: app.androidVersion,
      genre: app.genre,
      genreId: app.genreId,
      icon: app.icon,
      headerImage: app.headerImage,
      released: app.released,
      updated: app.updated,
      version: app.version,
      url: app.url
    }));

    res.json({
      count: formattedResults.length,
      results: formattedResults
    });
  } catch (error) {
    logger.error(`Error searching games: ${error.message}`);
    next(error);
  }
};

/**
 * Get permissions required by an app
 */
export const getAppPermissions = async (req, res, next) => {
  try {
    const { packageName } = req.params;
    const { lang = 'en' } = req.query;

    logger.info(`Fetching permissions for package: ${packageName}`);
    
    const permissions = await gplay.permissions({
      appId: packageName,
      lang: lang
    });

    res.json({
      packageName,
      permissions
    });
  } catch (error) {
    logger.error(`Error fetching app permissions: ${error.message}`);
    next(error);
  }
};
