import { MESSAGE_TYPE, Chat } from './components.js';
import { Client } from '@stomp/stompjs';

class SupportUI {
    client = null;
    chats = [];
    handledContainer = null;
    waitingContainer = null;
    chatsContainer = null;
    currentChat = null;
    wrapper = null;


    constructor() {
    }

    displayChat(userName) {
        if(this.currentChat != userName) {
            this.chatsContainer.innerHTML = '';
            this.chatsContainer.appendChild(this.chats[userName].element);
            this.currentChat = userName;
            this.handledContainer.childNodes.forEach(userContainer => {
                userContainer.className = 'user '+(userContainer.firstChild.innerHTML == userName ? ' active' : '')
                // reset dyslayed new messages count
                userContainer.childNodes[1].innerHTML = '';
            });
        }
    }

    createChat(messageObject) {
        let url = this.client.webSocket._transport.url;
        this.socketSessionId = url.match(/\/ws\/[^/]+\/([^/]+)\/websocket/)[1];
        this.chats[messageObject.sender] = new Chat(this.client, "support", messageObject.sender,`/user/queue/messages-user${this.socketSessionId}`, "/app/private");
        // first message has to be handled
        this.chats[messageObject.sender].receiveMessageObject(messageObject);
        const userElement = document.createElement('div');
        userElement.className = 'user';
        userElement.innerHTML = `<span class="username">${messageObject.sender}</span><span class="messages-count"></span>`;
        userElement.addEventListener('click', e => {e.preventDefault(); this.displayChat(messageObject.sender);});
        this.handledContainer.appendChild(userElement);
        this.displayChat(messageObject.sender);
    }

    addWaitingUser(username) {
        if (typeof this.chats[username] == "undefined") {
            // add user to queue
            const userElement = document.createElement('div');
            userElement.className = 'user';
            userElement.innerHTML = `<span class="username">${username}</span><span class="messages-count"></span>`;
            userElement.addEventListener('click', e => {
                e.preventDefault();
                this.client.publish({
                    destination: '/app/support',
                    body: JSON.stringify({
                        sender: this.socketSessionId,
                        recipient: username,
                        type: MESSAGE_TYPE.HANDLE,
                        content: username
                    })
                });
            });
            this.waitingContainer.appendChild(userElement);
        }
    }

    removeWaitingUser(username) {

    }

    handleMessage(message) {
        const messageObject = JSON.parse(message.body);
        // user has arrived and waits to be handled by someone from support
        switch(messageObject.type) {
            case MESSAGE_TYPE.START :
                    this.addWaitingUser(messageObject.sender)
                break;
            case MESSAGE_TYPE.HANDLE :
                // FIXME : it does not really make sense to consider that the user is the recipient
                this.removeWaitingUser(messageObject.content);
                if (typeof this.chats[messageObject.sender] == "undefined") {
                    // remove waiting user from queue
                }
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