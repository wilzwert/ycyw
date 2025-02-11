import {MESSAGE_TYPE, Chat, ChatHistory} from './chat.js';
import { Client } from '@stomp/stompjs';

class UI {

    client = null;
    socketSessionId = null;
    handled = false;

    constructor() {
    }

    displayChat(username, chatHistoryEntry = null) {
        const chat = new Chat(this.client, username, `/user/queue/messages/${username}-user${this.socketSessionId}`, '/app/private');
        if(chatHistoryEntry) {
            chat.restoreFromHistory(chatHistoryEntry);
        }
        this.wrapper.innerHTML = '';
        this.wrapper.appendChild(chat.element);
    }

    // let's see if we can restore previous interrupted chat
    // 1. from localstorage
    // 2. TODO from backend with fetch
    async restoreChat() {
        let chatHistoryEntries = ChatHistory.get().entries;
        if(!chatHistoryEntries) {
            // TODO load chat history from backend
        }

        // from the user point of view, there is one and only one chat with a support agent
        if(chatHistoryEntries == null || !Array.isArray(chatHistoryEntries) ||  chatHistoryEntries.length !== 1) {
            return false;
        }

        let chatHistoryEntry = chatHistoryEntries[0];
        this.displayChat(chatHistoryEntry.user, chatHistoryEntry);
    }

    startChat() {
        this.wrapper.innerHTML = "You should soon be connected to one of our agents";
        this.client.publish({
            destination: '/app/support',
            body: JSON.stringify({ sender: "", recipient: 'support', type: MESSAGE_TYPE.START, content: ''})
        });

        window.addEventListener("beforeunload", () => {
            this.client.publish({
                destination: '/app/support',
                body: JSON.stringify({ sender: "", recipient: 'support', type: MESSAGE_TYPE.QUIT, content: ''})
            });
        });

        this.client.subscribe(`/user/queue/messages-user${this.socketSessionId}`, (message) => {
            let messageObject = JSON.parse(message.body);
            if(messageObject.type === MESSAGE_TYPE.HANDLE) {
                this.handled = true;
                this.displayChat(messageObject.sender);
            }
        });
    }

    async createChat() {
        let restored = await this.restoreChat();
        if(false === restored) {
            this.startChat();
        }
    }

    connect() {
        try {
            this.client = new Client({
                debug: console.log,
                // Typical usage with SockJS
                webSocketFactory: function () {
                    return new SockJS("http://localhost:8080/ws");
                },
                onConnect: () => {
                    // send start message
                    let url = this.client.webSocket._transport.url;
                    this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)\/websocket/)[1];
                    this.createChat();
                },
                onWebSocketClose: () => {
                    // if the broker closed the connection, there's a good chance something terrible happened
                    // and the system probably won't be able to reconnect users as we don't persist anything for this POC
                    // in that specific case, all chat history should be deleted, and page should be reloaded
                    alert('An error occurred ; unfortunately your chat session will be lost.');
                    ChatHistory.get().clear();
                    location.reload();
                }
            });
            this.client.activate();
        }
        catch(ex) {
            console.log(ex);
        }
    }

    start() {
        // reset display
        this.wrapper = document.getElementById('chatWrapper');
        this.wrapper.innerHTML = '';
        // connect to websocket
        this.connect();
    };
}

const ui = new UI();
ui.start();