# Edge Detection Web Viewer

TypeScript-based web interface for Android OpenCV edge detection output.

## Features

- Display processed frames from Android app
- Real-time FPS and processing stats
- Mode switching (Original/Grayscale/Edges)
- Clean, responsive UI

## Setup

Install dependencies
npm install

Build TypeScript
npm run build

Serve locally
npm run serve


WEB PAGE HOST AT   ----> http://localhost:8000




## Architecture

- **TypeScript:** Type-safe frame handling
- **DOM Updates:** Real-time stats display
- **Future:** WebSocket support for live Android streaming

## Files

- `src/app.ts` - Main TypeScript application
- `public/index.html` - HTML interface
- `public/sample-frame.jpg` - Sample processed frame
