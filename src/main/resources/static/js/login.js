document.getElementById('formLogin').addEventListener('submit', (e) => {
e.preventDefault();

fetch(
    "/api/auth/login",
    {
        method: "POST",
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username: e.target.username.value, password: e.target.password.value})
    }).then(
    (response) => {
        if(response.ok) {
            response.json().then((json) => {
                localStorage.setItem("authToken", JSON.stringify(json));
                window.location = '/';
            })
        }
    }
);
});