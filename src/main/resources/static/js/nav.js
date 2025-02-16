import {TokenService} from "./token.js";

const nav = document.querySelector('.main-nav ul ');
let token = await TokenService.getToken();
let role = await TokenService.getRole();

if(token) {
    let li = document.createElement('li');
    if(role === 'SUPPORT') {
        let a = document.createElement('a');
        a.innerHTML = 'Support interface';
        a.title = "Go to support interface";
        a.href = "/support";
        li.appendChild(a);
    }
    else {
        let a = document.createElement('a');
        a.innerHTML = 'Chat with support';
        a.title = "Chat with support";
        a.href = "/chat";
        li.appendChild(a);
    }

    nav.appendChild(li);

    if(role !== 'ANONYMOUS') {
        let logout = document.createElement('li');
        let logoutLink = document.createElement('a');
        logoutLink.innerHTML = 'Logout';
        logoutLink.href = "";
        logoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            TokenService.clear();
            window.location = '/';
        });
        logout.appendChild(logoutLink);
        nav.appendChild(logout);
    }
    else {
        let login = document.createElement('li');
        login.innerHTML = '<a href="/login" title="Login">Login</a>';
        nav.appendChild(login);
    }

}
else {
    let li = document.createElement('li');
    li.innerHTML = '<a href="/chat" title="Chat with support">Chat with support</a>';
    nav.appendChild(li);

    let login = document.createElement('li');
    li.innerHTML = '<a href="/login" title="Login">Login</a>';
    nav.appendChild(login);
}
