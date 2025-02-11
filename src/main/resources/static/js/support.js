import {MESSAGE_TYPE, Chat, ChatHistory} from './chat.js';
import { Client } from '@stomp/stompjs';

class WaitingUser {
    username = null;
    callback = null;
    htmlElement = null;

    constructor(username, callback) {
        this.username = username;
        this.callback = callback;

        this.htmlElement = document.createElement('div');
        this.htmlElement.className = 'user';
        this.htmlElement.innerHTML = `<span class="username">${username}</span>`;
        this.htmlElement.addEventListener('click', e => {
            e.preventDefault();
            callback(username);
        });
    }

    get element() {
        return this.htmlElement;
    }
}

class ChatUser {

    username = null;
    callback = null;
    htmlElement = null;

    constructor(username, callback) {
        this.username = username;
        this.callback = callback;

        this.htmlElement = document.createElement('div');
        this.htmlElement.className = 'user';
        this.htmlElement.innerHTML = `<span class="username">${username}</span><span class="messages-count"></span>`;
        this.htmlElement.addEventListener('click', e => {e.preventDefault(); callback(username)});
    }

    get element() {
        return this.htmlElement;
    }
}

class SupportUI {
    client = null;
    chats = [];
    waitingUsers = [];
    handledContainer = null;
    waitingContainer = null;
    chatsContainer = null;
    currentChat = null;
    wrapper = null;


    constructor() {
    }

    displayChat(username) {
        if(this.currentChat !== username) {
            this.chatsContainer.innerHTML = '';
            this.chatsContainer.appendChild(this.chats[username].element);
            this.currentChat = username;
            this.handledContainer.childNodes.forEach(userContainer => {
                userContainer.className = 'user '+(userContainer.firstChild.innerHTML === username ? ' active' : '')
                // reset displayed new messages count
                userContainer.childNodes[1].innerHTML = '';
            });
        }
    }

    createChat(username, chatHistoryEntry = null) {
        const url = this.client.webSocket._transport.url;
        console.log(url);
        this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)(\/websocket)?/)[1];
        this.chats[username] = new Chat(this.client, username,`/user/queue/messages/${username}-user${this.socketSessionId}`, "/app/private");
        if(chatHistoryEntry) {
            this.chats[username].restoreFromHistory(chatHistoryEntry);
        }

        const user = new ChatUser(username, this.displayChat.bind(this));
        this.handledContainer.appendChild(user.element);
        this.displayChat(username);

        this.client.subscribe(`/user/queue/messages/${username}-user${this.socketSessionId}`, this.handleUserMessage.bind(this))
    }

    // support user wants to handle chat with username
    handleUser(username) {
        // brodacast the handle message
        this.client.publish({
            destination: '/app/support',
            body: JSON.stringify({
                sender: this.socketSessionId,
                recipient: username,
                type: MESSAGE_TYPE.HANDLE,
                content: username
            })
        });
        // display chat for the user
        this.createChat(username);
    }

    // display new waiting user
    addWaitingUser(username) {
        if (typeof this.chats[username] == "undefined" && typeof this.waitingUsers[username] == "undefined") {
            this.waitingUsers[username] = new WaitingUser(username, this.handleUser.bind(this));
            this.waitingContainer.appendChild(this.waitingUsers[username].element);
        }
    }

    // remove user from waiting users list
    removeWaitingUser(username) {
        if(typeof this.waitingUsers[username] != "undefined") {
            let elmt = this.waitingUsers[username].element;
            elmt.parentNode.removeChild(elmt);
            delete this.waitingUsers[username];
        }
    }

    // handle message broadcast on /topic/support
    handleMessage(message) {
        const messageObject = JSON.parse(message.body);
        console.log(messageObject);

        switch(messageObject.type) {
            // new user has arrived and waits to be handled by someone from support
            case MESSAGE_TYPE.START :
                    this.addWaitingUser(messageObject.sender)
                break;
            // a user has quit before they have been handled by someone from support
            case MESSAGE_TYPE.QUIT :
                this.removeWaitingUser(messageObject.sender)
                break;
            // a waiting user is handled by a support agent
            case MESSAGE_TYPE.HANDLE :
                // FIXME : it does not really make sense to consider that the user is the recipient
                this.removeWaitingUser(messageObject.recipient);
                break;
            default: break;
        }
    }

    handleUserMessage(message) {
        const messageObject = JSON.parse(message.body);

        if(messageObject.type === MESSAGE_TYPE.MESSAGE && messageObject.sender !== this.currentChat) {
            let user = this.handledContainer.childNodes.values().find(u => u.firstChild.innerHTML === messageObject.sender);
            let count = user.childNodes[1].innerHTML === '' ? 0 : parseInt(user.childNodes[1].innerHTML);
            count++;
            user.childNodes[1].innerHTML = count;
        }
    }

    // let's see if we can restore previous interrupted chat(s)
    // 1. from localstorage
    // 2. TODO from backend with fetch
    async restoreFromHistory() {
        let chatHistoryEntries = ChatHistory.get().entries;
        if(!chatHistoryEntries) {
            // TODO load chat history from backend
        }
        console.log(chatHistoryEntries);
        // from the user point of view, there is one and only one chat with a support agent
        if(chatHistoryEntries == null || !Array.isArray(chatHistoryEntries)) {
            return false;
        }

        chatHistoryEntries.forEach(chatHistoryEntry => {
            this.createChat(chatHistoryEntry.user, chatHistoryEntry);
        });
    }

    restoreWaitingUsers() {
        fetch("/api/chat/users?filter=waiting").then(r => r.json().then(json => {
           if(Array.isArray(json)) {
               json.forEach(u => this.addWaitingUser(u));
           }
        }));
    }

    createUi() {
        // create containers for chats, waiting users and currently handled users
        this.wrapper = document.getElementById('chatWrapper');
        this.wrapper.innerHTML = '';

        this.waitingContainer = document.createElement('div');
        this.waitingContainer.className = 'waiting-users';

        this.handledContainer = document.createElement('div');
        this.handledContainer.className = 'handled-users';
        const usersContainer = document.createElement('div');
        usersContainer.className = 'users';
        usersContainer.appendChild(this.handledContainer);
        usersContainer.appendChild(this.waitingContainer);
        this.wrapper.appendChild(usersContainer);

        this.chatsContainer = document.createElement('div');
        this.chatsContainer.className = 'chats';
        this.wrapper.appendChild(this.chatsContainer);
    }

    // at first, the user has to wait for a connection with a support agent
    start() {
        try {
            this.client = new Client({
                debug: console.log,
                // Typical usage with SockJS
                webSocketFactory: function () {
                    return new SockJS("http://localhost:8080/ws");
                },
                onConnect: () => {
                    this.createUi();

                    this.client.subscribe('/topic/support', this.handleMessage.bind(this));

                    // let's see if we can restore chats from history
                    try {
                        this.restoreFromHistory();
                        this.restoreWaitingUsers();
                    }
                    catch(e) {}
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
    };
}

const ui = new SupportUI();
ui.start();