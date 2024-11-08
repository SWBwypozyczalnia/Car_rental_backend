document.addEventListener("DOMContentLoaded", function() {
    
    const menuList = document.getElementById('menu-list');
    const mainPageLink = document.createElement('li');
    mainPageLink.innerHTML = '<a href="index.html">Strona główna</a>';
    menuList.appendChild(mainPageLink);
    
    fetch('http://localhost:8080/api/auth/check', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new Error('User not logged in');
        }
    })
    .then(() => {
        const menuList = document.getElementById('menu-list');
        const profileLink = document.createElement('li');
        profileLink.innerHTML = '<a href="user.html">Profil</a>';
        menuList.appendChild(profileLink);
    })
    .catch(() => {
        const menuList = document.getElementById('menu-list');
        const loginLink = document.createElement('li');
        loginLink.innerHTML = '<a href="login.html">Logowanie</a>';
        menuList.appendChild(loginLink);

        const registerLink = document.createElement('li');
        registerLink.innerHTML = '<a href="registry.html">Rejestracja</a>';
        menuList.appendChild(registerLink);
    });
});
