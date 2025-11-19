import { ReconnectingWebSocket } from './ReconnectingWebSocket.js';
import { BoggleGame } from './BoggleGame.js';

class App {
    constructor() {
        this.ws = new ReconnectingWebSocket('comms', {
                      maxReconnectAttempts: 5,
                      reconnectDelay: 1000,
                      maxReconnectDelay: 10000
                  });
        this.game = new BoggleGame(this.ws);

        this.setupWebSocket();
        this.setupEventListeners();
    }

    setupWebSocket() {
        this.ws.onopen = () => {
            console.log('Connected to Boggle server!');
            this.ws.send("Handshake");
            $("#status").text('Connected 🟢');
        };

        this.ws.onmessage = (event) => {
            let data = JSON.parse(event.data);
            this.handleMessage(data);
        };

        this.ws.onclose = () => {
            console.log('Disconnected from server');
            $("#status").text('Disconnected 🔴');
        };

        this.ws.onreconnecting = (attempt, delay) => {
            console.log(`Reconnecting... Attempt ${attempt}`);
            $('#status').text(`Reconnecting (${attempt}) 🟡`);
        };

        this.ws.onmaxreconnect = () => {
            alert('Could not reconnect to server. Please refresh the page.');
            $("#status").text('Connection failed ❌');
        };
    }

    setupEventListeners() {
         $(document).ready(() => {
            $('#joinButton').click(() => {
                this.joinGame();
            });

            $('#username').keypress((event) => {
                if (event.which === 13) {
                    $('#joinButton').click();
                }
            });

            $('#username').focus();
        });
    };

    handleMessage(data) {
        //console.log('Received message: '+ data.type, data);
        switch (data.type) {
            case "GameJoined":
                this.game.onGameJoined(data.gameData);
                break;
            case "Sync":
                this.game.updateUI(data.gameData);
                break;
            case "WordGuessed":
                this.game.onWordGuessed(data);
                break;
        }
    };

    joinGame() {
        const username = $('#username').val();
        if (!username) {
            alert("Veuillez entrer un nom d'utilisateur");
            return;
        }

        this.game.username = username;

        const data = {
            type: "JoinGame",
            name: username
        };
        const payload = JSON.stringify(data);
        console.log("Sending payload :" + payload);
        this.ws.send(payload);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const app = new App();

    // Optionally expose for debugging
    window.game = app;
});