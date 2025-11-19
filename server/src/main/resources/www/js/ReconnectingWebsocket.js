export class ReconnectingWebSocket {
    constructor(url, options = {}) {
        this.url = url;
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = options.maxReconnectAttempts || 10;
        this.reconnectDelay = options.reconnectDelay || 1000;
        this.maxReconnectDelay = options.maxReconnectDelay || 30000;
        this.shouldReconnect = true;
        this.messageQueue = [];

        // Event handlers
        this.onopen = null;
        this.onmessage = null;
        this.onerror = null;
        this.onclose = null;
        this.onreconnecting = null;
        this.onmaxreconnect = null;

        this.connect();
    }

    connect() {
        console.log('Connecting to WebSocket...');
        this.ws = new WebSocket(this.url);

        this.ws.onopen = (event) => {
            console.log('✅ Connected to WebSocket');
            this.reconnectAttempts = 0;
            this.reconnectDelay = 1000;

            // Send queued messages
            while (this.messageQueue.length > 0) {
                const msg = this.messageQueue.shift();
                this.ws.send(msg);
                console.log('📤 Sent queued message');
            }

            if (this.onopen) {
                this.onopen(event);
            }
        };

        this.ws.onmessage = (event) => {
            if (this.onmessage) {
                this.onmessage(event);
            }
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            if (this.onerror) {
                this.onerror(error);
            }
        };

        this.ws.onclose = (event) => {
            console.log('WebSocket closed:', event.code, event.reason);

            if (this.onclose) {
                this.onclose(event);
            }

            if (this.shouldReconnect) {
                this.reconnect();
            }
        };
    }

    reconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('❌ Max reconnection attempts reached');
            if (this.onmaxreconnect) {
                this.onmaxreconnect();
            }
            return;
        }

        this.reconnectAttempts++;
        const delay = this.reconnectDelay;
        console.log(`Reconnecting in ${delay}ms... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);

        if (this.onreconnecting) {
            this.onreconnecting(this.reconnectAttempts, delay);
        }

        setTimeout(() => {
            this.connect();
        }, delay);

        // Exponential backoff
        this.reconnectDelay = Math.min(this.reconnectDelay * 2, this.maxReconnectDelay);
    }

    send(data) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(data);
        } else {
            console.warn('WebSocket not connected, queueing message');
            this.messageQueue.push(data);
        }
    }

    close() {
        this.shouldReconnect = false;
        this.messageQueue = [];
        if (this.ws) {
            this.ws.close();
        }
    }

    get readyState() {
        return this.ws?.readyState;
    }

    get isConnected() {
        return this.ws?.readyState === WebSocket.OPEN;
    }
}