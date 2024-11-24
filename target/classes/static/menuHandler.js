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

    console.log("Wysłanie zapytania na /auth/check...");
    fetch(`${API_BASE_URL}/api/auth/check`, {
        method: "GET",
        credentials: "include",
    })
        .then((response) => {
            console.log("Odpowiedź z /auth/check:", response);
            if (response.ok) {
                return response.text();
            } else {
                throw new Error("User not logged in");
            }
        })
        .then(() => {
            console.log("Użytkownik jest zalogowany. Dodawanie linków do menu...");
            const profileLink = document.createElement("li");
            profileLink.innerHTML = '<a href="user.html">Profil</a>';
            menuList.appendChild(profileLink);

            console.log("Dodaję przycisk Wyloguj...");
            const logoutButton = document.createElement("li");
            logoutButton.innerHTML = '<button id="logout-button">Wyloguj</button>';
            menuList.appendChild(logoutButton);

            console.log("Dodano przycisk Wyloguj. Ustawianie event listenera...");
            document
                .getElementById("logout-button")
                .addEventListener("click", function () {
                    console.log("Kliknięcie przycisku Wyloguj. Wysyłanie zapytania...");
                    fetch(`${API_BASE_URL}/api/auth/logout`, {
                        method: "DELETE",
                        credentials: "include",
                    })
                        .then((response) => {
                            console.log("Odpowiedź z /auth/logout:", response);
                            if (response.ok) {
                                document.cookie = "accessToken=; Max-Age=0; path=/";
                                document.cookie = "refreshToken=; Max-Age=0; path=/";
                                localStorage.removeItem("userId");
                                localStorage.removeItem("isAdmin");
                                window.location.href = "index.html";
                            } else {
                                return response.text().then((text) => {
                                    throw new Error(text);
                                });
                            }
                        })
                        .catch((error) => {
                            console.error(`Błąd podczas wylogowywania: ${error.message}`);
                        });
                });
        })
        .catch((error) => {
            console.error("Użytkownik nie jest zalogowany:", error.message);
            const loginLink = document.createElement("li");
            loginLink.innerHTML = '<a href="login.html">Logowanie</a>';
            menuList.appendChild(loginLink);

            const registerLink = document.createElement("li");
            registerLink.innerHTML = '<a href="registry.html">Rejestracja</a>';
            menuList.appendChild(registerLink);
        });
});
