import { MESSAGE_TYPE, Chat} from './components.js';
import { Client } from '@stomp/stompjs';

class UI {

    client = null;
    socketSessionId = null;

    constructor() {
    }

    displayChat() {
        const chat = new Chat(this.client, this.socketSessionId, "support", `/user/queue/messages-user${this.socketSessionId}`, '/app/private');
        this.wrapper.innerHTML = '';
        this.wrapper.appendChild(chat.element);
    }

    startChat() {
        let url = this.client.webSocket._transport.url;
        this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)\/websocket/)[1];
        this.client.publish({
            destination: '/app/support',
            body: JSON.stringify({ sender: this.socketSessionId, recipient: 'support', type: MESSAGE_TYPE.START, content: ''})
        });

        this.client.subscribe(`/user/queue/messages-user${this.socketSessionId}`, (message) => {
            let messageObject = JSON.parse(message);
            if(messageObject.type == MESSAGE_TYPE.HANDLE) {
                this.displayChat();
            }
        });
        this.wrapper.innerHTML = "You should soon be connected to one of our agents";
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
                    // send start message
                    this.startChat();
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