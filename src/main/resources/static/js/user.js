import {MESSAGE_TYPE, Chat, ChatHistory, ChatService} from './chat.js';
import { Client } from '@stomp/stompjs';

class UserChat {

    chatHistory = null;
    client = null;
    socketSessionId = null;
    username = null;

    constructor() {
    }

    displayChat(username, chatHistoryEntry = null) {
        const chat = new Chat({
            sender: this.username,
            client: this.client,
            recipient: username,
            // source: `/user/queue/messages/${username}-user${this.socketSessionId}`,
            source: `/user/queue/messages/${username}`,
            destination: '/app/private',
            chatHistory: this.chatHistory
        });

        if(chatHistoryEntry) {
            chat.restoreFromHistory(chatHistoryEntry);
        }
        this.wrapper.innerHTML = '';
        this.wrapper.appendChild(chat.element);
    }

    // Restore previous interrupted chat if possible
    async restoreChat() {
        let chatHistoryEntries = this.chatHistory.entries;

        // from the user point of view, there is one and only one chat with a support agent
        if(chatHistoryEntries == null || !Array.isArray(chatHistoryEntries) ||  chatHistoryEntries.length !== 1) {
            return false;
        }
        let chatHistoryEntry = chatHistoryEntries[0];
        this.displayChat(chatHistoryEntry.user, chatHistoryEntry);
    }

    async startChat() {
        this.wrapper.innerHTML = "You should soon be connected to one of our agents";
        // this.client.subscribe(`/user/queue/messages-user${this.socketSessionId}`, (message) => {
        this.client.subscribe(`/user/queue/messages`, (message) => {
            let messageObject = JSON.parse(message.body);
            if(messageObject.type === MESSAGE_TYPE.HANDLE) {
                this.displayChat(messageObject.sender);
            }
        });

        this.client.publish({
            destination: '/app/support',
            body: JSON.stringify({ sender: this.username, recipient: 'support', type: MESSAGE_TYPE.START, content: ''})
        });

        window.addEventListener("beforeunload", () => {
            this.client.publish({
                destination: '/app/support',
                body: JSON.stringify({ sender: this.username, recipient: 'support', type: MESSAGE_TYPE.QUIT, content: ''})
            });
        });


    }

    async createChat() {
        let restored = await this.restoreChat();
        if(false === restored) {
            this.startChat();
        }
    }

    async websocketConnected() {
        // let url = this.client.webSocket._transport.url;
        // this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)\/websocket/)[1];
        this.username = await ChatService.getUsername();
        // websocket connected means we now have a username
        // so we pass it to the ChatHistory if needed
        if(this.chatHistory.owner === null) {
            this.chatHistory.owner = this.username;
            console.log(this.chatHistory.owner);
        }

        await this.createChat();
    }

    // called when websocket closed
    websocketClosed() {
        // if the broker closed the connection, it may indicate a problem with the server
        // or that an authenticated user logged out
        // in that specific case, page should be reloaded to try to reload chat
        location.reload();
    }


    async connect() {
        try {
            this.chatHistory = await ChatHistory.get();
            let token = await ChatService.getToken();

            this.client = new Client({
                // debug: console.log,
                brokerURL: "ws://localhost:8080/ws?token="+token,
                // Typical usage with SockJS
                /* webSocketFactory: function () {
                    return new SockJS("http://localhost:8080/ws");
                },*/
                onConnect: () => this.websocketConnected(),
                onWebSocketClose: () => this.websocketClosed()
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

const chat = new UserChat();
chat.start();