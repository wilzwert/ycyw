export class TokenService {
    static #username = null;
    static #token = null;
    static #role = null;

    static async #load() {
        // try and get the token and username from localStorage if we can
        let tokenStr = localStorage.getItem("authToken");
        let tokenObj = null;
        if(tokenStr) {
            tokenObj = JSON.parse(tokenStr);
        }
        else {
            // or else get one from the API and store it in localStorage
            const response = await fetch("/api/auth/login", {method: "POST"});
            if (response.ok) {
                tokenObj = await response.json();
                localStorage.setItem("authToken", JSON.stringify(tokenObj));
            }
        }
        TokenService.#token = tokenObj?.token;
        TokenService.#username = tokenObj?.username;
        TokenService.#role = tokenObj?.role;
    }

    static async getUsername() {
        if(TokenService.#username == null) {
            await TokenService.#load();
        }
        return TokenService.#username;
    }

    static async getRole() {
        if(TokenService.#role == null) {
            await TokenService.#load();
        }
        return TokenService.#role;
    }

    static async getToken() {
        if(TokenService.#token == null) {
            await TokenService.#load();
        }
        return TokenService.#token;
    }

    static clear() {
        this.#token = this.#username = this.#role = null;
        localStorage.removeItem('authToken');
    }
}