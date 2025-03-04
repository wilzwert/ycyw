# You Car You Way

This is the 13th project of my course as a full-stack Java / Angular developer with OpenClassrooms.

The goal here is to create a simple chat system as a Proof of Concept (POC) in the context of a real-time support chat functionality on a web application.

This project is based on a backend made with Spring Boot using Websockets and SIMP messaging with private queues and topics to handle bidirectional communication. The backend also provides an API to authenticate and get a list of users awaiting to be connected with a support user. Also, the project comes with a vers simple database to prove the possibility of chat conversations and messages persistence.

A simple frontend, made with HTML / CSS and Javascript is also provided. To see a proposal of Angular frontend consuming the same API and message service, please see this repository : https://github.com/wilzwert/ycyw-chat-front.

## Requirements
- Java 21
- Maven
- Git
## Installation
1. Clone this repository on your local machine or server and go to directory

``` bash
git clone https://github.com/wilzwert/ycyw.git
cd ycyw
```

2. Install backend dependencies

``` bash
mvn clean install
```

Or directly in your favorite IDE

## Start the backend

Windows :
``` bash 
mvnw spring-boot:run
```

Linux / unix :
``` bash
mvn spring-boot:run
```

This will make the API and the POC frontend available on http://localhost:8080 

## Test the POC frontend

Go to http://localohst:8080

You will then access the home page of the POC frontend. On this page you can :
- go directly to the chat ; in that case you will be queued as an anonymous user with a unique UUID as username
- go to login
  - to login as a customer / client you can use these test credentials : client / password
  - to login as a support user you can use these test credentials : support / password or agent / password
- if you logged in as a customer, go to the chat ; in that case you will be queues as "client"
- if you logged in as a support user, go to the support interface
  - in the support interface, there are 3 main areas :
    - users currently active, ie handled by the current support user
    - users requesting a chat session
    - the conversation display area
- 
## Features
### Security
- JWT Token authentication
- Websockets are associated with the user based on its JWT token
- Request data validation through DTO

### Persistence
- Conversations and messages are persisted in an H2 database for this POC
- the H2 database itself is persisted in 

```
├── data
  ├── chat_poc.mv.db
```

### Conversations
- A user initiates a chat conversation with the support, which creates a unique conversation in the database
- The user is added in a queue of waiting users
- A support user can select a waiting user,which creates a chat message and informs the appropriate user and the other support users as well
- Support and initiator users are then connected and can send private messages to each other
- Private messages are persisted in the database as soon as they get sent to the backend 
- When a user is typing, the recipient is informed
- The conversation messages are stored as an history in localStorage
- When a user leaves the conversation, to navigate to another page or by mistake, the recipient is informed
- To allow users to reconnect after leaving, the conversation is not closed at once
- Pinging : a ping is sent every 5 seconds to ensure the recipient is still connected
- In case there is not response to the ping in 20 seconds, the conversation is closed
- When a conversation is closed, its end date is also stored in the database and the conversation is considered ended
- A user can also manually close a conversation
