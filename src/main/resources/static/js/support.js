import {MESSAGE_TYPE, Chat, ChatHistory} from './chat.js';
import { TokenService} from "./token.js";
import { Client } from '@stomp/stompjs';

/**
 * A user waiting for connection with a support agent
 */
class WaitingUser {
    username = null;
    conversationId = null;
    callback = null;
    htmlElement = null;

    constructor(conversationId, username, callback) {
        this.conversationId = conversationId;
        this.username = username;
        this.callback = callback;

        this.htmlElement = document.createElement('div');
        this.htmlElement.setAttribute('data-conversation-id', conversationId);
        this.htmlElement.className = 'user';
        this.htmlElement.innerHTML = `<span class="username">${username}</span>`;
        this.htmlElement.addEventListener('click', e => {
            e.preventDefault();
            callback(conversationId, username);
        });
    }

    get element() {
        return this.htmlElement;
    }
}

/**
 * A user connected with a support agent
 */
class ChatUser {

    conversationId = null;
    username = null;
    callback = null;
    htmlElement = null;

    constructor(conversationId, username, callback) {
        this.conversationId = conversationId;
        this.username = username;
        this.callback = callback;

        this.htmlElement = document.createElement('div');
        this.htmlElement.setAttribute('data-conversation-id', conversationId);
        this.htmlElement.className = 'user';
        this.htmlElement.innerHTML = `<span class="username">${username}</span><span class="messages-count"></span>`;
        this.htmlElement.addEventListener('click', e => {e.preventDefault(); callback(conversationId, username)});
    }

    get element() {
        return this.htmlElement;
    }
}

class SupportChat {
    #client = null;
    #chats = {};
    #waitingUsers = {};
    #handledContainer = null;
    #waitingContainer = null;
    #chatsContainer = null;
    #currentChat = null;
    #wrapper = null;

    constructor() {
    }

    displayChat(conversationId) {
        if(this.#currentChat !== conversationId) {
            this.#currentChat = conversationId;
            this.#chatsContainer.innerHTML = '';
            this.#chatsContainer.appendChild(this.#chats[conversationId].element);
            this.#handledContainer.childNodes.forEach(userContainer => {
                userContainer.className = 'user '+(userContainer.getAttribute('data-conversation-id') === conversationId ? ' active' : '')
                // reset displayed new messages count
                userContainer.childNodes[1].innerHTML = '';
            });
        }
    }

    removeChat(chat) {
        let username = chat.recipient;
        let conversationId = chat.conversationId;
        // remove chat container
        if(typeof this.#chats[conversationId] != 'undefined') {
            delete(this.#chats[conversationId]);

            // remove user container
            let handled = Array.from(this.#handledContainer.childNodes).find(e => e.firstChild.innerHTML === username);
            if(handled) {
                handled.parentNode.removeChild(handled);
            }

            // set active chat if needed and possible
            if(this.#currentChat === conversationId) {
                this.#currentChat = null;
                this.#chatsContainer.innerHTML = '';
                if(this.#handledContainer.childNodes.length) {
                    this.displayChat(this.#handledContainer.childNodes[0].getAttribute('data-conversation-id'));
                }
            }
        }
    }

    async createChat(conversationId, username, chatHistoryEntry = null) {
        this.#chats[conversationId] = new Chat({
            client: this.#client,
            conversationId: conversationId,
            sender: await TokenService.getUsername(),
            recipient: username,
            source: `/user/queue/messages/${conversationId}`,
            destination: "/app/private",
            chatHistory: this.chatHistory,
            onPingTimeout: (chat) => {alert(`User is unreachable ; history will be removed.`); this.removeChat(chat);}
        });
        if(chatHistoryEntry) {
            this.#chats[conversationId].restoreFromHistory(chatHistoryEntry);
        }
        else {
            this.chatHistory.createEntry(conversationId, username);
        }

        const user = new ChatUser(conversationId, username, this.displayChat.bind(this));
        this.#handledContainer.appendChild(user.element);
        this.displayChat(conversationId);

        this.#client.subscribe(
            // `/user/queue/messages/${username}-user${this.socketSessionId}`,
            `/user/queue/messages/${conversationId}`,
            this.handleUserMessage.bind(this)
        )
    }

    // support user wants to handle chat with username for conversationId
    handleUser(conversationId, username) {
        // broadcast the handle message
        this.#client.publish({
            destination: '/app/support',
            body: JSON.stringify({
                sender: this.username,
                recipient: username,
                type: MESSAGE_TYPE.HANDLE,
                content: username,
                conversationId: conversationId
            })
        });
        // display chat for the user
        this.createChat(conversationId, username);
        this.removeWaitingUser(conversationId,username);
    }

    // display new waiting user
    addWaitingUser(conversationId, username) {
        if (typeof this.#chats[conversationId] == "undefined" && typeof this.#waitingUsers[conversationId] == "undefined") {
            this.#waitingUsers[conversationId] = new WaitingUser(conversationId, username, this.handleUser.bind(this));
            this.#waitingContainer.appendChild(this.#waitingUsers[conversationId].element);
        }
    }

    // remove user from waiting users list
    removeWaitingUser(conversationId, username) {
        if(conversationId != null) {
            if (typeof this.#waitingUsers[conversationId] != "undefined") {
                let element = this.#waitingUsers[conversationId].element;
                element.parentNode.removeChild(element);
                delete this.#waitingUsers[conversationId];
            }
        }
        else if(username != null) {
            let u = Object.values(this.#waitingUsers).find(v => v.username === username);
            if(u && u.element) {
                u.element.parentNode.removeChild(u.element);
            }
            delete this.#waitingUsers[u.conversationId];
        }
    }

    // handle message broadcast on /topic/support
    handleMessage(message) {
        const messageObject = JSON.parse(message.body);
        switch(messageObject.type) {
            // new user has arrived and waits to be handled by someone from support
            case MESSAGE_TYPE.START :
                    this.addWaitingUser(messageObject.conversationId, messageObject.sender)
                break;
            // a user has quit before they have been handled by someone from support
            case MESSAGE_TYPE.QUIT :
                this.removeWaitingUser(messageObject.conversationId, messageObject.sender)
                break;
            // a waiting user is handled by a support agent
            case MESSAGE_TYPE.HANDLE :
                this.removeWaitingUser(messageObject.conversationId, messageObject.sender);
                break;
            default: break;
        }
    }

    // handle a message received and update unread messages if needed
    handleUserMessage(message) {
        const messageObject = JSON.parse(message.body);

        if(messageObject.type === MESSAGE_TYPE.MESSAGE && messageObject.conversationId !== this.#currentChat) {
            let user = this.#handledContainer.childNodes.values().find(u => u.getAttribute('data-conversation-id') === messageObject.conversationId);
            let count = user.childNodes[1].innerHTML === '' ? 0 : parseInt(user.childNodes[1].innerHTML);
            count++;
            user.childNodes[1].innerHTML = count;
        }
    }

    // Restore previous interrupted chat(s) if possible
    async restoreFromHistory() {

        let chatHistoryEntries = this.chatHistory.entries;

        // no entries mean there is nothing we can restore
        if(chatHistoryEntries == null || !Array.isArray(chatHistoryEntries)) {
            return false;
        }

        chatHistoryEntries.forEach(chatHistoryEntry => {
            if(chatHistoryEntry !== null) {
                this.createChat(chatHistoryEntry.conversationId, chatHistoryEntry.distantUser, chatHistoryEntry);
            }
        });
    }

    // restore waiting users list from API call result
    restoreWaitingUsers() {
        fetch("/api/chat/users?filter=waiting").then(r => r.json().then(json => {
           if(Array.isArray(json)) {
               json.forEach(u => this.addWaitingUser(u.conversationId, u.username));
           }
        }));
    }

    // create user interface html elements and add them to the wrapper
    createUi() {
        // create containers for chats, waiting users and currently handled users
        this.#wrapper = document.getElementById('chatWrapper');
        this.#wrapper.innerHTML = '';

        this.#waitingContainer = document.createElement('div');
        this.#waitingContainer.className = 'waiting-users';

        this.#handledContainer = document.createElement('div');
        this.#handledContainer.className = 'handled-users';
        const usersContainer = document.createElement('div');
        usersContainer.className = 'users';
        usersContainer.appendChild(this.#handledContainer);
        usersContainer.appendChild(this.#waitingContainer);
        this.#wrapper.appendChild(usersContainer);

        this.#chatsContainer = document.createElement('div');
        this.#chatsContainer.className = 'chats';
        this.#wrapper.appendChild(this.#chatsContainer);
    }

    // called when websocket is connected
    async websocketConnected() {
        // this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)(\/websocket)?/)[1];
        this.username = await TokenService.getUsername()
        // websocket connected means we now have a username
        // so we pass it to the ChatHistory if needed
        if(this.chatHistory.owner === null) {
            this.chatHistory.owner = this.username;
        }

        this.createUi();

        // subscribe to messages sent to the support topic
        this.#client.subscribe('/topic/support', this.handleMessage.bind(this));

        // restore chats and waiting users if available
        try {
            await this.restoreFromHistory();
            this.restoreWaitingUsers();
        }
        catch(e) {}
    }

    // called when websocket closed
    websocketClosed() {
        // if the broker closed the connection, it may indicate a problem with the server
        // or that an authenticated user logged out
        // in that specific case, page should be reloaded to try to reload chat
        // location.reload();
    }

    async start() {
        try {
            let role = await TokenService.getRole();
            if(role !== "SUPPORT") {
                location.href = "/login";
            }
            this.chatHistory = await ChatHistory.get();
            let token = await TokenService.getToken();
            this.#client = new Client({
                brokerURL: "ws://localhost:8080/ws?token="+token,
                // debug: console.log,
                onConnect: this.websocketConnected.bind(this),
                onWebSocketClose: this.websocketClosed.bind(this)
            });
            this.#client.activate();
        }
        catch(ex) {
            console.log(ex);
        }
    };
}

const chat = new SupportChat();
chat.start();