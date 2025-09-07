# GameBooster Backend Service

Backend service for GameBooster app that provides game data from Google Play Store using google-play-scraper.

## Features

- Get detailed game information by package name
- Search for games on Google Play Store
- Get app permissions
- Multi-language support
- CORS enabled
- Logging with Winston
- Error handling

## Prerequisites

- Node.js >= 14.0.0
- npm or yarn

## Installation

1. Clone the repository
2. Install dependencies:
   ```bash
   cd gamebooster-backend
   npm install
   ```

## Environment Variables

Create a `.env` file in the root directory with the following variables:

```
PORT=3000
NODE_ENV=development
```

## Running the Server

- Development (with hot-reload):
  ```bash
  npm run dev
  ```

- Production:
  ```bash
  npm start
  ```

The server will start on `http://localhost:3000` by default.

## API Endpoints

### Get Game Details

```
GET /api/games/details/:packageName
```

**Parameters:**
- `packageName` (required): The package name of the app (e.g., `com.activision.callofduty.shooter`)
- `lang` (optional): Language code (default: 'en')
- `country` (optional): Country code (default: 'us')

### Search Games

```
GET /api/games/search?query=clash+of+clans
```

**Query Parameters:**
- `query` (required): Search term
- `num` (optional): Number of results (default: 20)
- `lang` (optional): Language code (default: 'en')
- `country` (optional): Country code (default: 'us')

### Get App Permissions

```
GET /api/games/:packageName/permissions
```

**Parameters:**
- `packageName` (required): The package name of the app
- `lang` (optional): Language code (default: 'en')

## Health Check

```
GET /health
```

## Error Handling

Errors are returned in the following format:

```json
{
  "error": {
    "message": "Error message",
    "status": 404
  }
}
```

## Logging

Logs are written to:
- Console
- `error.log` (error logs only)
- `combined.log` (all logs)

## License

MIT
