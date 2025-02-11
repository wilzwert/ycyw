import { MESSAGE_TYPE, Chat } from './components.js';
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
        this.htmlElement.innerHTML = `<span class="username">${username}</span><span class="messages-count"></span>`;
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
        if(this.currentChat != username) {
            this.chatsContainer.innerHTML = '';
            this.chatsContainer.appendChild(this.chats[username].element);
            this.currentChat = username;
            this.handledContainer.childNodes.forEach(userContainer => {
                userContainer.className = 'user '+(userContainer.firstChild.innerHTML == username ? ' active' : '')
                // reset dyslayed new messages count
                userContainer.childNodes[1].innerHTML = '';
            });
        }
    }

    createChat(username) {
        const url = this.client.webSocket._transport.url;
        this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)\/websocket/)[1];
        this.chats[username] = new Chat(this.client, username,`/user/queue/messages-user${this.socketSessionId}`, "/app/private");

        const user = new ChatUser(username, this.displayChat.bind(this));
        this.handledContainer.appendChild(user.element);
        this.displayChat(username);
    }

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

    addWaitingUser(username) {
        if (typeof this.chats[username] == "undefined" && typeof this.waitingUsers[username] == "undefined") {
            this.waitingUsers[username] = new WaitingUser(username, this.handleUser.bind(this));
            this.waitingContainer.appendChild(this.waitingUsers[username].element);
        }
    }

    removeWaitingUser(username) {
        console.log('removing ', username);
        console.log(this.waitingUsers);
        if(typeof this.waitingUsers[username] != "undefined") {
            let elmt = this.waitingUsers[username].element;
            elmt.parentNode.removeChild(elmt);
            delete this.waitingUsers[username];
        }
    }

    handleMessage(message) {
        const messageObject = JSON.parse(message.body);
        console.log(messageObject);
        // user has arrived and waits to be handled by someone from support
        switch(messageObject.type) {
            case MESSAGE_TYPE.START :
                    this.addWaitingUser(messageObject.sender)
                break;
            case MESSAGE_TYPE.HANDLE :
                // FIXME : it does not really make sense to consider that the user is the recipient
                this.removeWaitingUser(messageObject.recipient);
                break;
            default:
                if (typeof this.chats[messageObject.sender] == "undefined") {
                    this.createChat(messageObject);
                } else if (messageObject.sender != this.currentChat) {
                    let user = this.handledContainer.childNodes.values().find(u => u.firstChild.innerHTML == messageObject.sender);
                    let count = user.childNodes[1].innerHTML == '' ? 0 : parseInt(user.childNodes[1].innerHTML);
                    count++;
                    user.childNodes[1].innerHTML = count;
                }
                break;

        }
    }

    // at first, the user has to choose a username
    start() {
        try {
            this.client = new Client({
                connectHeaders: {
                    username: "support"
                },
                debug: console.log,
                // Typical usage with SockJS
                webSocketFactory: function () {
                    return new SockJS("http://localhost:8080/ws");
                },
                onConnect: () => {
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

                    this.client.subscribe('/topic/support', this.handleMessage.bind(this));
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



// OLD
/*
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    // Le support s'abonne aux messages des utilisateurs
    stompClient.subscribe('/topic/support', (message) => {
        console.log("Message d'un utilisateur:", JSON.parse(message.body));
    });
});

// TODO
// Envoyer un message au support
function sendMessage(content) {
    // stompClient.send("/app/support", {}, JSON.stringify({ sender: "user1", recipient: "support", content }));
}
*/