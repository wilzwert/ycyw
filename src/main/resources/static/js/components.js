export const MESSAGE_TYPE = {
    START: 'START',
    HANDLE: 'HANDLE',
    MESSAGE: 'MESSAGE',
    TYPING: 'TYPING',
    STOP_TYPING: 'STOP_TYPING',
    QUIT: 'QUIT'
}

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
    messageInput = null;

    constructor(client, recipient, source, destination) {
        this.client = client;
        this.recipient = recipient;
        this.source = source;
        this.destination = destination;
        this.buildUi();
        this.subscribe();
    }

    subscribe() {
        this.client.subscribe(this.source, this.receiveMessage.bind(this));
    }

    displayTyping() {
        this.typing.style.display = 'block';

    }

    hideTyping() {
        this.typing.style.display = '';
    }

    userQuit() {
        const messageElement = document.createElement('div');
        messageElement.className = 'quit-message';
        messageElement.innerHTML = 'User has left the chat.';
        this.addMessage(messageElement);
    }

    addReceivedMessage(messageContent) {
        const messageElement = document.createElement('div');
        messageElement.className = 'received-message';
        messageElement.innerHTML = messageContent;
        this.addMessage(messageElement);
    }

    receiveMessageObject(messageObject) {
        console.log(messageObject);
        if(messageObject.sender == this.recipient) {
            switch(messageObject.type) {
                case MESSAGE_TYPE.MESSAGE: this.addReceivedMessage(messageObject.content); break;
                case MESSAGE_TYPE.TYPING: this.displayTyping(); break;
                case MESSAGE_TYPE.STOP_TYPING: this.hideTyping(); break;
                case MESSAGE_TYPE.QUIT: this.userQuit(); break;
            }
        }
    }

    receiveMessage(message) {
        this.receiveMessageObject(JSON.parse(message.body));
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

    sendMessage(messageType, content) {
        this.client.publish({
            destination: this.destination,
            body: JSON.stringify({ recipient: this.recipient, type: messageType, content: content})
        });
        if(messageType == MESSAGE_TYPE.MESSAGE) {
            this.addSentMessage(content);
        }
    }

    buildUi() {
        this.wrapper = document.createElement('div');
        this.wrapper.className = 'chat';

        // build container for sent and received messages
        this.messagesContainer = document.createElement('div');
        this.messagesContainer.className = 'messages';
        this.wrapper.appendChild(this.messagesContainer);


        // build message form
        const messageFormContainer = document.createElement('div');
        messageFormContainer.className = 'message-form';
        const formHtml = `
            <form method="post">
                <input type="text" name="message" placeholder="Type your message">
                <button type="submit">Send</button>
            </form>`;
        messageFormContainer.innerHTML = formHtml;
        this.wrapper.appendChild(messageFormContainer);
        this.messageForm = messageFormContainer.getElementsByTagName('form')[0];
        this.messageInput = this.messageForm.elements['message'];

        // add listeners for form events
        let timeout = null;
        let isTyping = false;
        this.messageInput.addEventListener('keydown', (e) => {
            if(!isTyping && e.keyCode != "Enter") {
                isTyping = true;
                timeout = setTimeout(() => {
                    timeout = null;
                    this.sendMessage(MESSAGE_TYPE.TYPING, '')
                }, 500);
            }
        });

        this.messageInput.addEventListener('blur', (e) => {
           if(timeout) {
               clearTimeout(timeout);
           }
           if(isTyping) {
               isTyping = false;
               timeout = setTimeout(() => {
                   timeout = null;
                   this.sendMessage(MESSAGE_TYPE.STOP_TYPING, '')
               }, 500);
           }
        });

        this.messageForm.addEventListener('submit', (e) => {
            e.preventDefault();
            if(timeout) {
                clearTimeout(timeout);
                timeout = null;
            }
            isTyping = false;
            this.sendMessage(MESSAGE_TYPE.STOP_TYPING, '');
            this.sendMessage(MESSAGE_TYPE.MESSAGE, e.target.message.value);
            e.target.message.value = '';
        });

        window.addEventListener("beforeunload", (e) => {
            this.sendMessage(MESSAGE_TYPE.QUIT, '');
        });

        // create container to display "User is typing..." when needed
        this.typing = document.createElement('div');
        this.typing.className = 'display-typing';
        this.typing.innerHTML = 'User is typing...';
        this.messagesContainer.appendChild(this.typing);
    }

    get element() {
        return this.wrapper;
    }
}