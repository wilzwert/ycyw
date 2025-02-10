import {LoginForm, MessageForm, Chat} from './components.js';
import { Client } from '@stomp/stompjs';

class UI {
    client = null;

    constructor() {
    }

    displayChat() {
        let url = this.client.webSocket._transport.url;
        let sessionId = url.match(/\/ws\/[^/]+\/([^/]+)\/websocket/)[1];
        console.log(sessionId);
        const chat = new Chat(this.client, sessionId, "support", '/user/'+/*username+'/'+*/'queue/messages-user'+sessionId, '/app/support');
        this.wrapper.innerHTML = '';
        this.wrapper.appendChild(chat.element);
    }

    connect() {
        try {
            this.client = new Client({
                // debug: console.log,
                // Typical usage with SockJS
                webSocketFactory: function () {
                    return new SockJS("http://localhost:8080/ws");
                },
                onConnect: () => {
                    this.displayChat();
                }
            });
            this.client.activate();
        }
        catch(ex) {
            console.log(ex);
        }
    }

    // at first, the user has to choose a username
    start() {
        this.wrapper = document.getElementById('chatWrapper');
        this.wrapper.innerHTML = '';
        this.connect();
    };
}

const ui = new UI();
ui.start();