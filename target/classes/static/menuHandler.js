document.addEventListener("DOMContentLoaded", function () {
    console.log("Skrypt załadowany i gotowy.");

    const menuList = document.getElementById("menu-list");
    if (!menuList) {
        console.error("Element #menu-list nie został znaleziony w DOM!");
        return;
    }

    console.log("Dodaję link do strony głównej.");
    const mainPageLink = document.createElement("li");
    mainPageLink.innerHTML = '<a href="index.html">Strona główna</a>';
    menuList.appendChild(mainPageLink);

    console.log("Dodaję link do strony samochodów.");
    const carsPageLink = document.createElement("li");
    carsPageLink.innerHTML = '<a href="carList.html">Nasza flota</a>';
    menuList.appendChild(carsPageLink);

    // Sprawdzenie localStorage dla userId
    const userId = localStorage.getItem("userId");
    const isAdmin = localStorage.getItem("isAdmin");
    if (userId) {
        console.log("Użytkownik jest zalogowany (userId w localStorage). Dodawanie linków do menu...");

        const profileLink = document.createElement("li");
        profileLink.innerHTML = '<a href="user.html">Profil</a>';
        menuList.appendChild(profileLink);

        if(isAdmin==='true') {
            const adminPanelLink = document.createElement("li");
            adminPanelLink.innerHTML = '<a href="adminPanel.html">Panel admina</a>';
            menuList.appendChild(adminPanelLink);

            const adminAddCarLink = document.createElement("li");
            adminAddCarLink.innerHTML = '<a href="carAdd.html">Dodaj samochód</a>';
            menuList.appendChild(adminAddCarLink);
        }

        console.log("Dodaję link Wyloguj...");
        const logoutLink = document.createElement("li");
        logoutLink.innerHTML = '<a href="#" id="logout-link">Wyloguj</a>';
        menuList.appendChild(logoutLink);

        console.log("Dodano link Wyloguj. Ustawianie event listenera...");
        document
            .getElementById("logout-link")
            .addEventListener("click", function (event) {
                event.preventDefault(); // Zapobiega domyślnemu działaniu linku
                console.log("Kliknięcie linku Wyloguj. Usuwanie danych lokalnych...");
                localStorage.removeItem("userId");
                localStorage.removeItem("isAdmin");
                document.cookie = "accessToken=; Max-Age=0; path=/";
                document.cookie = "refreshToken=; Max-Age=0; path=/";
                window.location.href = "index.html";
            });
    } else {
        console.log("Użytkownik nie jest zalogowany. Dodawanie linków logowania i rejestracji...");

        const loginLink = document.createElement("li");
        loginLink.innerHTML = '<a href="login.html">Logowanie</a>';
        menuList.appendChild(loginLink);

        const registerLink = document.createElement("li");
        registerLink.innerHTML = '<a href="registry.html">Rejestracja</a>';
        menuList.appendChild(registerLink);
    }
});
