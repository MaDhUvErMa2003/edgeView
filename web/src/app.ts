/**
 * Edge Detection Web Viewer
 * TypeScript interface for Android OpenCV processing output
 */

interface FrameStats {
    fps: number;
    resolution: string;
    processingTime: number;
    mode: string;
}

class EdgeDetectionViewer {
    private frameElement: HTMLImageElement;
    private fpsElement: HTMLElement;
    private resolutionElement: HTMLElement;
    private timeElement: HTMLElement;
    private modeElement: HTMLElement;

    private currentMode: string = 'edges';
    private stats: FrameStats = {
        fps: 25,
        resolution: '1920x1080',
        processingTime: 40,
        mode: 'EDGES'
    };

    constructor() {
        this.frameElement = document.getElementById('processed-frame') as HTMLImageElement;
        this.fpsElement = document.getElementById('fps-value')!;
        this.resolutionElement = document.getElementById('resolution-value')!;
        this.timeElement = document.getElementById('time-value')!;
        this.modeElement = document.getElementById('mode-value')!;

        this.initializeEventListeners();
        this.updateStats();
        this.simulateLiveUpdates();
    }

    private initializeEventListeners(): void {
        document.getElementById('btn-original')?.addEventListener('click', () => {
            this.changeMode('original');
        });

        document.getElementById('btn-grayscale')?.addEventListener('click', () => {
            this.changeMode('grayscale');
        });

        document.getElementById('btn-edges')?.addEventListener('click', () => {
            this.changeMode('edges');
        });

        document.getElementById('btn-refresh')?.addEventListener('click', () => {
            this.refreshFrame();
        });
    }

    private changeMode(mode: string): void {
        this.currentMode = mode;

        // Update stats based on mode
        switch(mode) {
            case 'original':
                this.stats.mode = 'ORIGINAL';
                this.stats.processingTime = 0;
                this.stats.fps = 30;
                break;
            case 'grayscale':
                this.stats.mode = 'GRAYSCALE';
                this.stats.processingTime = 20;
                this.stats.fps = 28;
                break;
            case 'edges':
                this.stats.mode = 'EDGES';
                this.stats.processingTime = 40;
                this.stats.fps = 25;
                break;
        }

        this.updateStats();
        console.log(`[EdgeDetectionViewer] Mode changed to: ${mode.toUpperCase()}`);
    }

    private refreshFrame(): void {
        console.log('[EdgeDetectionViewer] Refreshing frame...');

        // Simulate frame refresh with timestamp
        const timestamp = new Date().getTime();
        this.frameElement.src = `sample-frame.jpg?t=${timestamp}`;

        // Simulate random FPS variation
        this.stats.fps = Math.floor(Math.random() * 5) + 23; // 23-28 FPS
        this.updateStats();
    }

    private updateStats(): void {
        this.fpsElement.textContent = this.stats.fps.toString();
        this.resolutionElement.textContent = this.stats.resolution;
        this.timeElement.textContent = `${this.stats.processingTime}ms`;
        this.modeElement.textContent = this.stats.mode;
    }

    private simulateLiveUpdates(): void {
        // Simulate live FPS updates every 2 seconds
        setInterval(() => {
            this.stats.fps = Math.floor(Math.random() * 5) + 23; // 23-28 FPS
            this.updateStats();
        }, 2000);
    }

    // Method to receive frame data from Android (future implementation)
    public updateFrame(base64Image: string, stats: FrameStats): void {
        this.frameElement.src = `data:image/jpeg;base64,${base64Image}`;
        this.stats = stats;
        this.updateStats();
        console.log('[EdgeDetectionViewer] Frame updated from Android');
    }

    // Mock WebSocket connection (for bonus implementation)
    public connectToAndroid(url: string): void {
        console.log(`[EdgeDetectionViewer] Connecting to Android device at ${url}...`);
        // WebSocket implementation would go here
    }
}

// Initialize viewer when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('[EdgeDetectionViewer] Initializing...');
    const viewer = new EdgeDetectionViewer();

    // Expose to window for debugging
    (window as any).viewer = viewer;

    console.log('[EdgeDetectionViewer] Ready ✅');
});
