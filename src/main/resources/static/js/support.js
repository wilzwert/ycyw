import {MESSAGE_TYPE, Chat, ChatHistory, ChatService} from './chat.js';
import { Client } from '@stomp/stompjs';

/**
 * A user waiting for connection with a support agent
 */
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

/**
 * A user connected with a support agent
 */
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

    displayChat(username) {
        if(this.#currentChat !== username) {
            this.#currentChat = username;
            this.#chatsContainer.innerHTML = '';
            this.#chatsContainer.appendChild(this.#chats[username].element);
            this.#handledContainer.childNodes.forEach(userContainer => {
                userContainer.className = 'user '+(userContainer.firstChild.innerHTML === username ? ' active' : '')
                // reset displayed new messages count
                userContainer.childNodes[1].innerHTML = '';
            });
        }
    }

    removeChat(chat) {
        let username = chat.recipient;
        // remove chat container
        if(typeof this.#chats[username] != 'undefined') {
            delete(this.#chats[username]);

            // remove user container
            let handled = Array.from(this.#handledContainer.childNodes).find(e => e.firstChild.innerHTML === username);
            if(handled) {
                handled.parentNode.removeChild(handled);
            }

            // set active chat if needed and possible
            if(this.#currentChat === username) {
                this.#currentChat = null;
                this.#chatsContainer.innerHTML = '';
                if(this.#handledContainer.childNodes.length) {
                    this.displayChat(this.#handledContainer.childNodes[0].firstChild.innerHTML);
                }
            }
        }
    }

    async createChat(username, chatHistoryEntry = null) {
        this.#chats[username] = new Chat({
            client: this.#client,
            sender: await ChatService.getUsername(),
            recipient: username,
            // source: `/user/queue/messages/${username}-user${this.socketSessionId}`,
            source: `/user/queue/messages/${username}`,
            destination: "/app/private",
            chatHistory: this.chatHistory,
            onPingTimeout: this.removeChat.bind(this)
        });
        if(chatHistoryEntry) {
            this.#chats[username].restoreFromHistory(chatHistoryEntry);
        }

        const user = new ChatUser(username, this.displayChat.bind(this));
        this.#handledContainer.appendChild(user.element);
        this.displayChat(username);

        this.#client.subscribe(
            // `/user/queue/messages/${username}-user${this.socketSessionId}`,
            `/user/queue/messages/${username}`,
            this.handleUserMessage.bind(this)
        )
    }

    // support user wants to handle chat with username
    handleUser(username) {
        // broadcast the handle message
        this.#client.publish({
            destination: '/app/support',
            body: JSON.stringify({
                // sender: this.socketSessionId,
                sender: this.username,
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
        if (typeof this.#chats[username] == "undefined" && typeof this.#waitingUsers[username] == "undefined") {
            this.#waitingUsers[username] = new WaitingUser(username, this.handleUser.bind(this));
            this.#waitingContainer.appendChild(this.#waitingUsers[username].element);
        }
    }

    // remove user from waiting users list
    removeWaitingUser(username) {
        if(typeof this.#waitingUsers[username] != "undefined") {
            let element = this.#waitingUsers[username].element;
            element.parentNode.removeChild(element);
            delete this.#waitingUsers[username];
        }
    }

    // handle message broadcast on /topic/support
    handleMessage(message) {
        const messageObject = JSON.parse(message.body);

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

    // handle a message received and update unread messages if needed
    handleUserMessage(message) {
        const messageObject = JSON.parse(message.body);

        if(messageObject.type === MESSAGE_TYPE.MESSAGE && messageObject.sender !== this.#currentChat) {
            let user = this.#handledContainer.childNodes.values().find(u => u.firstChild.innerHTML === messageObject.sender);
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
            this.createChat(chatHistoryEntry.user, chatHistoryEntry);
        });
    }

    // restore waiting users list from API call result
    restoreWaitingUsers() {
        fetch("/api/chat/users?filter=waiting").then(r => r.json().then(json => {
           if(Array.isArray(json)) {
               json.forEach(u => this.addWaitingUser(u));
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
    async websocketConnected(ifr) {
        console.log(ifr);
        // const url = this.#client.webSocket._transport.url;
        console.log(this.#client.webSocket);
        // this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)(\/websocket)?/)[1];
        this.username = await ChatService.getUsername()
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
            this.chatHistory = await ChatHistory.get();
            let token = await ChatService.getToken();
            console.log("Token "+token);
            this.#client = new Client({
                brokerURL: "ws://localhost:8080/ws?token="+token,
                // debug: console.log,
                // Typical usage with SockJS
                /*webSocketFactory: function () {
                return new WebSocket("ws://localhost:8080/ws", null, {
                      headers: {
                        "Authorization": "Bearer "+token,
                          "X-Custom": "TRUC"
                      }
                    });
                    // return new SockJS("http://localhost:8080/ws");
                },*/
                onConnect: this.websocketConnected.bind(this),
                onWebSocketClose: this.websocketClosed.bind(this)
            }, console.log);
            this.#client.activate();
        }
        catch(ex) {
            console.log(ex);
        }
    };
}

const chat = new SupportChat();
chat.start();