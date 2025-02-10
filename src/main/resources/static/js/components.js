// at first, the user has to choose a username
export class MessageForm extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <form class="message-form" method="post">
                <input type="text" name="message" placeholder="Type your message">
                <button type="submit">Send</button>
            </form>
        `;
    }
}

customElements.define("message-form", MessageForm);

export class LoginForm extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <div  class="form">
            <form id="loginForm" method="post">
                <label for="username">Username</label>
                <input name="username" type="text" placeholder="Type your username" />
                <button type="submit">Login</button>
            </form>
            </div>
        `;
    }
}

customElements.define("login-form", LoginForm);

export class Chat {
    // WS client
    client = null;
    // username
    sender = null;
    // recipient
    recipient = null;
    // subscribe to source to get messages
    source = null;
    // set destination for sent messages
    destination = null;

    wrapper = null;
    messagesContainer = null;
    messageForm = null;

    constructor(client, sender, recipient, source, destination) {
        this.client = client;
        this.sender = sender;
        this.recipient = recipient;
        this.source = source;
        this.destination = destination;
        this.buildUi();
        this.subscribe();
    }

    subscribe() {
        console.log(`subscribe ${this.source}`);
        this.client.subscribe(this.source, this.receiveMessage.bind(this));
    }

    addReceivedMessage(messageContent) {
        console.log('add received', messageContent);
        const messageElement = document.createElement('div');
        messageElement.className = 'received-message';
        messageElement.innerHTML = messageContent;
        this.addMessage(messageElement);
    }

    receiveMessage(message) {
        console.log('receive');
        const messageObject = JSON.parse(message.body);
        if(messageObject.sender == this.recipient) {
            this.addReceivedMessage(JSON.parse(message.body).content);
        }
    }

    addMessage(messageElement) {
        const message = document.createElement('div');
        message.className = 'message';
        message.appendChild(messageElement);
        this.messagesContainer.appendChild(message);
    }

    addSentMessage(messageContent) {
        const messageElement = document.createElement('div');
        messageElement.className = 'sent-message';
        messageElement.innerHTML = messageContent;
        this.addMessage(messageElement);
    }

    sendMessage(message) {
        console.log('sendMessage to '+this.destination);
        this.client.publish({
            destination: this.destination,
            body: JSON.stringify({ sender: this.sender, recipient: this.recipient, content: message})
        });
        this.addSentMessage(message);
    }

    buildUi() {
        this.wrapper = document.createElement('div');
        this.wrapper.className = 'chat';

        this.messagesContainer = document.createElement('div');
        this.messagesContainer.className = 'messages';
        this.wrapper.appendChild(this.messagesContainer);

        this.messageForm = document.createElement('message-form');
        this.wrapper.appendChild(this.messageForm);
        this.messageForm.addEventListener('submit', (e) => {
            e.preventDefault();
            console.log('sendmessage')
            this.sendMessage(e.target.message.value)
            e.target.message.value = '';
        })
    }

    get element () {
        return this.wrapper;
    }
}