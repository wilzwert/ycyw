"use strict";

import { TokenService} from "./token.js";

export const MESSAGE_TYPE = {
    START: 'START',
    HANDLE: 'HANDLE',
    MESSAGE: 'MESSAGE',
    TYPING: 'TYPING',
    STOP_TYPING: 'STOP_TYPING',
    QUIT: 'QUIT',
    CLOSE: 'CLOSE',
    JOIN: 'JOIN',
    PING: 'PING',
    PING_RESPONSE: 'PING_RESPONSE'
}

export class ChatHistory {

    static _INSTANCE = null;

    #entries = null;
    #owner = null;

    constructor(entries, owner = null) {
        this.#entries = entries;
        this.#owner = owner;
    }

    get entries() {
        return this.#entries;
    }
    get owner() {
        return this.#owner;
    }
    set owner(owner) {
        this.#owner = owner;
    }
    
    save() {
        localStorage.setItem("chatHistory", JSON.stringify({owner: this.#owner, entries: this.#entries}));
    }

    createEntry(conversationId, distantUser) {
        if(!this.#entries) {
            this.#entries = [];
        }

        let entry = this.#entries.find(e => e.conversationId === conversationId);
        if(!entry) {
            entry = {conversationId: conversationId, distantUser: distantUser, messages: []};
            this.#entries.push(entry);
            this.save();
        }
    }

    addMessage(conversationId, distantUser, messageObject) {
        // find entry for the user
        if(!this.#entries) {
            this.#entries = [];
        }

        let entry = this.#entries.find(e => e.conversationId === conversationId);
        if(!entry) {
            entry = {conversationId: conversationId, distantUser: distantUser, messages: []};
            this.#entries.push(entry);
        }
        entry.messages.push(messageObject);
        this.save();
    }

    removeConversation(conversationId) {
        if(this.#entries) {
            let index = this.#entries.findIndex(e => e.conversationId === conversationId);
            if(index >= 0) {
                this.#entries.splice(index, 1);
                this.save();
            }
        }
    }

    clear() {
        localStorage.removeItem("chatHistory");
        this.#entries = null;
        this.#owner = null;
    }

    static async get() {
        if(ChatHistory._INSTANCE == null) {
            let history = localStorage.getItem("chatHistory");
            let entries, owner = null;
            if(history) {
                try {
                    let h = JSON.parse(history);
                    entries = h.entries;
                    owner = h.owner;
                }
                catch(e) {
                    console.log(e);
                }
            }
            ChatHistory._INSTANCE = new ChatHistory(entries, owner);

            if(ChatHistory._INSTANCE.entries && ChatHistory._INSTANCE.entries.length) {
                // history should be cleared if no user is associated with the current token
                // or if user is not the same as the history owner
                let username = await TokenService.getUsername();
                if(!username || username !== ChatHistory._INSTANCE.owner) {
                    ChatHistory._INSTANCE.clear();
                }
            }
        }
        return ChatHistory._INSTANCE;
    }
}

/**
 * Chat User Interface
 * Handles all UI related operations
 *  - DOM elements generation and updates
 *  - event listeners
 */
class ChatUI {
    #wrapper = null;
    #messagesContainer = null;
    #messageForm = null;
    #messageInput = null;
    #typingMessage = null;
    #isTyping = false;
    #typingTimeout = null;
    #closeLink = null;

    #onTyping = () => {};
    #onStopTyping = () => {};
    #onSendMessage = () => {};
    #onClose = () =>  {};

    constructor({onTyping, onStopTyping, onSendMessage, onClose}) {
        this.#onTyping = onTyping;
        this.#onStopTyping = onStopTyping;
        this.#onSendMessage = onSendMessage;
        this.#onClose = onClose;
        this.buildUi();
    }

    get element() {
        return this.#wrapper;
    }

    createListeners() {
        this.#messageInput.addEventListener('keydown', this.isTyping.bind(this));
        this.#messageInput.addEventListener('blur', this.stopTyping.bind(this));
        this.#messageForm.addEventListener('submit', this.sendMessage.bind(this));
        this.#closeLink.addEventListener('click', this.close.bind(this));
    }

    // build the html and set listeners elements needed
    buildUi() {
        this.#wrapper = document.createElement('div');
        this.#wrapper.className = 'chat';

        // build container for sent and received messages
        this.#messagesContainer = document.createElement('div');
        this.#messagesContainer.className = 'messages';
        this.#wrapper.appendChild(this.#messagesContainer);

        // build message form
        const messageFormContainer = document.createElement('div');
        messageFormContainer.className = 'message-form';
        messageFormContainer.innerHTML = `
            <form method="post">
                <input type="text" name="message" placeholder="Type your message">
                <button type="submit">Send</button>
            </form>`;
        this.#wrapper.appendChild(messageFormContainer);
        this.#messageForm = messageFormContainer.getElementsByTagName('form')[0];
        this.#messageInput = this.#messageForm.elements['message'];

        // create container to display "User is typing..." when needed
        this.#typingMessage = document.createElement('div');
        this.#typingMessage.className = 'display-typing';
        this.#typingMessage.innerHTML = 'User is typing...';
        this.#wrapper.appendChild(this.#typingMessage);

        // create container to display "User is typing..." when needed
        this.#closeLink = document.createElement('a');
        this.#closeLink.className = 'chat-close';
        this.#closeLink.innerHTML = 'End chat';
        this.#wrapper.appendChild(this.#closeLink);

        // add listeners for form events
        this.createListeners();
    }

    isTyping(e) {
        // typing
        if(!this.#isTyping && e.keyCode !== "Enter") {
            this.#isTyping = true;
            this.#typingTimeout = setTimeout(() => {
                this.#typingTimeout = null;

                this.#onTyping();
            }, 500);
        }
    }

    // stop typing
    stopTyping() {
        if(this.#typingTimeout) {
            clearTimeout(this.#typingTimeout);
        }
        if(this.#isTyping) {
            this.#isTyping = false;
            this.#typingTimeout = setTimeout(() => {
                this.#typingTimeout = null;
                this.#onStopTyping();
            }, 500);
        }
    }

    // submit message form -> send message
    sendMessage(e) {
        e.preventDefault();
        this.stopTyping();
        this.#onSendMessage(e.target.message);
        e.target.message.value = '';
    }

    userInactive() {
        const messageElement = document.createElement('div');
        messageElement.className = 'timeout-message';
        messageElement.innerHTML = 'User is unreachable ; chat history will be deleted.';
        this.addMessage(messageElement);
    }

    // display the "User is typing" hint
    displayTyping() {
        this.#typingMessage.style.display = 'block';
    }

    // hide the "User is typing" hint
    hideTyping() {
        this.#typingMessage.style.display = '';
    }

    userQuit() {
        const messageElement = document.createElement('div');
        messageElement.className = 'quit-message';
        messageElement.innerHTML = 'User has left the chat.';
        this.addMessage(messageElement);
    }

    // user joined the chat
    userJoin() {
        const messageElement = document.createElement('div');
        messageElement.className = 'join-message';
        messageElement.innerHTML = 'User has joined the chat.';
        this.addMessage(messageElement);
    }

    close() {
        this.#onClose();
    }

    // add received message to the messages displayed
    addReceivedMessage(messageContent) {
        const messageElement = document.createElement('div');
        messageElement.className = 'received-message';
        messageElement.innerHTML = messageContent;
        this.addMessage(messageElement);
    }

    // adds a messageElement in the messages container
    addMessage(messageElement) {
        const message = document.createElement('div');
        message.className = 'message';
        message.appendChild(messageElement);
        this.#messagesContainer.appendChild(message);
        this.#messagesContainer.scrollTop = this.#messagesContainer.scrollHeight;
    }

    // builds a specific message html element and then delegates to addMessage to add it to the ui
    addSentMessage(messageContent) {
        const messageElement = document.createElement('div');
        messageElement.className = 'sent-message';
        messageElement.innerHTML = messageContent;
        this.addMessage(messageElement);
    }
}

/**
 * A Chat
 * Handles all websocket related operations
 * Delegates UI updates and event listeners to embedded ChatUi, providing callbacks when needed
 * Delegates history operations to embedded ChatHistory
 */
export class Chat {
    static TIMEOUT = 5;
    static PING_DELAY = 2;
    // WS client
    #client = null;
    // conversation UUID
    #conversationId = null;
    // username
    #sender = null;
    // recipient
    #recipient = null;
    // subscribe to source to get messages
    #source = null;
    // set destination for sent messages
    #destination = null;
    // embedded ChatHistory object
    #chatHistory = null;
    // callback for ping timeout
    #onPingTimeout = null;
    // callback when chat is closed (ie is ended manually by a user)
    #onClose = null;
    // last time data was received
    #lastReceived = null;
    // ChatUi
    #ui = null;

    constructor({client, conversationId, sender, recipient, source, destination, chatHistory, onPingTimeout = null, onClose = () => location.reload()}) {
        this.#client = client;
        this.#conversationId = conversationId;
        this.#sender = sender;
        this.#recipient = recipient;
        this.#source = source;
        this.#destination = destination;
        this.#chatHistory = chatHistory;
        this.#onPingTimeout = onPingTimeout;
        this.#onClose = onClose;
        this.#lastReceived = Date.now();

        // build UI
        this.#ui = new ChatUI({
            onTyping: this.onIsTyping.bind(this),
            onStopTyping: this.onStopTyping.bind(this),
            onSendMessage: this.onSendMessage.bind(this),
            onClose: this.onClose.bind(this)
        });

        // watches page navigation or closing
        this.watchUnload();
        // subscribe to the source
        this.subscribe();
        // start pings to watch for recipient leaving the chat
        this.ping();
    }

    get recipient() {
        return this.#recipient;
    }

    get conversationId() {
        return this.#conversationId;
    }

    watchUnload() {
        // send quit message on page quit
        window.addEventListener("beforeunload", () => {
            this.sendMessage(MESSAGE_TYPE.QUIT, '');
        });
    }
    // provided to the Chat UI as a callback called when user is typing
    onIsTyping() {
        this.sendMessage(MESSAGE_TYPE.TYPING, '');
    }

    // provided to the Chat UI as a callback called when user stops typing
    onStopTyping() {
        this.sendMessage(MESSAGE_TYPE.STOP_TYPING, '');
    }

    // provided to the Chat UI as a callback called when user submits message forme
    onSendMessage(messageInput) {
        if(messageInput.value !== '') {
            this.sendMessage(MESSAGE_TYPE.MESSAGE, messageInput.value);
        }
    }

    checkPingTimeout() {
        let delay = Math.floor((Date.now() - this.#lastReceived) / 1000);
        // nothing happened
        if(delay > Chat.TIMEOUT) {
            this.userInactive();
            this.#chatHistory.removeConversation(this.#conversationId);
            // if specific callback provided we call it
            if(this.#onPingTimeout) {
                this.#onPingTimeout(this);
            }
            // otherwise it defaults to only reloading the page
            else {
                setTimeout(() => location.reload(), 2000);
            }
        }
        // otherwise we juste ping again
        else {
            setTimeout(this.ping.bind(this), Chat.PING_DELAY*1000);
        }
    }

    // ping the recipient to check if it is available
    ping() {
        let delay = Math.floor((Date.now() - this.#lastReceived) / 1000);
        // nothing happened for a certain amount of time
        if(delay >= Chat.TIMEOUT) {
            // let's send a ping message
            this.sendMessage(MESSAGE_TYPE.PING, "");
            // next time we check we specifically check if something happend after our ping message
            setTimeout(this.checkPingTimeout.bind(this), Chat.PING_DELAY*1000);
        }
        // everything's fine for now, let's check later
        else {
            setTimeout(this.ping.bind(this), Chat.PING_DELAY*1000);
        }
    }

    onClose() {
        alert(`Chat with ${this.recipient} has ended ; thank you for chatting !`);
        this.sendMessage(MESSAGE_TYPE.CLOSE, '' );
        this.#chatHistory.removeConversation(this.#conversationId);
        this.#onClose(this);
    }

    recipientClosed() {
        alert(`Chat with ${this.recipient} has ended ; thank you for chatting !`);
        this.#chatHistory.removeConversation(this.#conversationId);
        this.#onClose(this);
    }

    // restore messages from history
    restoreFromHistory(chatHistoryEntry) {
        chatHistoryEntry.messages.forEach(messageObject => {
            if(messageObject.sender === this.#sender) {
                this.#ui.addSentMessage(messageObject.content);
            }
            else {
                this.#ui.addReceivedMessage(messageObject.content);
            }
        });
        // inform the recipient that the user rejoined the chat
        this.sendMessage(MESSAGE_TYPE.JOIN, "");
    }

    // subscribe to the source queue and set handler
    subscribe() {
        this.#client.subscribe(this.#source, this.receiveMessage.bind(this));
    }

    // display the "User is typing" hint
    recipientIsTyping() {
        this.#ui.displayTyping();
    }

    // hide the "User is typing" hint
    recipientStopTyping() {
        this.#ui.hideTyping();
    }

    userInactive() {
        this.#ui.userInactive();
    }

    // recipient quit the chat
    recipientQuit() {
        this.#ui.userQuit();
    }

    // recipient joined the chat
    recipientJoin() {
        this.#ui.userJoin();
    }

    // receive a message
    receiveMessageObject(messageObject) {
        switch(messageObject.type) {
            case MESSAGE_TYPE.MESSAGE:
                this.#ui.addReceivedMessage(messageObject.content);
                this.#chatHistory.addMessage(this.#conversationId, this.#recipient, messageObject);
                break;
            case MESSAGE_TYPE.TYPING:
                this.recipientIsTyping();
                break;
            case MESSAGE_TYPE.STOP_TYPING:
                this.recipientStopTyping();
                break;
            case MESSAGE_TYPE.QUIT:
                this.recipientQuit();
                break;
            case MESSAGE_TYPE.CLOSE:
                this.recipientClosed();
                break;
            case MESSAGE_TYPE.JOIN:
                this.recipientJoin();
                break;
            case MESSAGE_TYPE.PING:
                this.sendMessage(MESSAGE_TYPE.PING_RESPONSE, "");
                break;
        }
    }

    // receive a raw message
    receiveMessage(message) {
        this.#lastReceived = Date.now();
        this.receiveMessageObject(JSON.parse(message.body));
    }

    // send a message
    sendMessage(messageType, content) {
        const messageObject = { sender: this.#sender, recipient: this.#recipient, type: messageType, content: content, conversationId: this.#conversationId};
        this.#client.publish({
            destination: this.#destination,
            body: JSON.stringify(messageObject)
        });
        if(messageType === MESSAGE_TYPE.MESSAGE) {
            this.#ui.addSentMessage(content);
            this.#chatHistory.addMessage(this.#conversationId, this.#recipient, messageObject);
        }
    }

    get element() {
        return this.#ui.element;
    }
}