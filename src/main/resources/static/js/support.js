import { Chat } from './components.js';
import { Client } from '@stomp/stompjs';

class SupportUI {
    client = null;
    chats = [];
    usersContainer = null;
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
            this.usersContainer.childNodes.forEach(userContainer => {
                userContainer.className = 'user '+(userContainer.firstChild.innerHTML == userName ? ' active' : '')
                // reset dyslayed new messages count
                userContainer.childNodes[1].innerHTML = '';
            });
        }
    }

    createChat(messageObject) {
        this.chats[messageObject.sender] = new Chat(this.client, "support", messageObject.sender,"/topic/support", "/app/private");
        this.chats[messageObject.sender].addReceivedMessage(messageObject.content);
        const userElement = document.createElement('div');
        userElement.className = 'user';
        userElement.innerHTML = `<span class="username">${messageObject.sender}</span><span class="messages-count"></span>`;
        userElement.addEventListener('click', e => {e.preventDefault(); this.displayChat(messageObject.sender);});
        this.usersContainer.appendChild(userElement);
        this.displayChat(messageObject.sender);
    }

    handleMessage(message) {
        const messageObject = JSON.parse(message.body);
        console.log("Message d'un utilisateur:", messageObject.sender);
        if(typeof this.chats[messageObject.sender] == "undefined") {
            this.createChat(messageObject);
        }
        else if(messageObject.sender != this.currentChat) {
            let user = this.usersContainer.childNodes.values().find(u => u.firstChild.innerHTML == messageObject.sender);
            let count = user.childNodes[1].innerHTML == '' ? 0 : parseInt(user.childNodes[1].innerHTML);
            count++;
            user.childNodes[1].innerHTML = count;
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
                    this.usersContainer = document.createElement('div');
                    this.usersContainer.className = 'users';
                    this.wrapper.appendChild(this.usersContainer);

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