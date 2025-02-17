"use strict";

import {MESSAGE_TYPE, Chat, ChatHistory} from './chat.js';
import { Client } from '@stomp/stompjs';
import { TokenService } from './token.js';

class UserChat {

    chatHistory = null;
    client = null;
    username = null;
    conversationId = null;

    constructor() {
    }

    displayChat(conversationId, username, chatHistoryEntry = null) {
        const chat = new Chat({
            sender: this.username,
            conversationId: conversationId,
            client: this.client,
            recipient: username,
            source: `/user/queue/messages/${conversationId}`,
            destination: '/app/private',
            chatHistory: this.chatHistory,
            onClose: () => location.href = "/"
        });

        if(chatHistoryEntry) {
            chat.restoreFromHistory(chatHistoryEntry);
        }
        else {
            this.chatHistory.createEntry(conversationId, username);
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
        this.displayChat(chatHistoryEntry.conversationId, chatHistoryEntry.distantUser, chatHistoryEntry);
    }

    async startChat() {
        this.wrapper.innerHTML = "You should soon be connected to one of our agents";
        this.client.subscribe(`/user/queue/messages`, (message) => {
            let messageObject = JSON.parse(message.body);
            if(messageObject.type === MESSAGE_TYPE.HANDLE) {
                this.conversationId = messageObject.conversationId;
                this.displayChat(messageObject.conversationId, messageObject.sender);
            }
        });

        this.client.publish({
            destination: '/app/support',
            body: JSON.stringify({ sender: this.username, recipient: 'support', type: MESSAGE_TYPE.START, content: ''})
        });

        window.addEventListener("beforeunload", () => {
            console.log(this.client);
            this.client.publish({
                destination: '/app/support',
                body: JSON.stringify({ sender: this.username, recipient: 'support', type: MESSAGE_TYPE.QUIT, content: '', conversationId: this.conversationId})
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
        this.username = await TokenService.getUsername();
        // pass current username to the ChatHistory if needed
        if(this.chatHistory.owner === null) {
            this.chatHistory.owner = this.username;
        }
        // changing current user implies that all chats have been closed one way or another
        else if(this.chatHistory.owner !== this.username) {
            this.chatHistory.clear();
        }

        await this.createChat();
    }

    // called when websocket closed
    websocketClosed() {
        // if the broker closed the connection, it may indicate a problem with the server
        // or that an authenticated user logged out
        // in that specific case, page should be reloaded to try to reload chat
        // location.reload();
    }


    async connect() {
        try {
            this.chatHistory = await ChatHistory.get();
            let token = await TokenService.getToken();

            this.client = new Client({
                // debug: console.log,
                brokerURL: "ws://localhost:8080/ws?token="+token,
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