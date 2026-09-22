# BakeHub — Android Prototype + XAMPP REST API

BakeHub is a Kotlin + Jetpack Compose baking app prototype now connected to a small PHP REST API backed by MySQL. The API is designed to run in **XAMPP** locally and can be deployed to any PHP/MySQL host with a public HTTPS URL.

## What is connected

- Login calls `POST /api.php/auth/login` and falls back to demo mode when the server is unavailable.
- Search calls `GET /api.php/recipes?search=...` and displays API recipes when available.
- Create Recipe calls `POST /api.php/recipes` and persists the submitted recipe, ingredients, and steps.
- Recipe data is stored in normalized MySQL tables: `users`, `recipes`, `ingredients`, `recipe_steps`, and `saved_recipes`.
- The Android client uses `HttpURLConnection`, so no additional networking library is required.

## Run the API with XAMPP

1. Install and start **Apache** and **MySQL** in XAMPP.
2. Copy the `api` folder into the XAMPP web root as `htdocs/bakehub-api/`.
3. Open `http://localhost/phpmyadmin`, select the **Import** tab, and import `bakehub-api/schema.sql`.
4. Verify the API in a browser: `http://localhost/bakehub-api/api.php/health`. It should return `{"ok":true,"service":"BakeHub API","database":"mysql"}`.
5. Demo login credentials are `demo@bakehub.app` / `bakehub123`.

The API currently uses the default XAMPP database account (`root` with an empty password). For production hosting, update the constants at the top of `api/api.php` and use HTTPS.

## Run the Android app against XAMPP

The default endpoint is `http://10.0.2.2/bakehub-api/api.php`, which maps an Android emulator to the host computer's `localhost`. For a physical device, replace `10.0.2.2` with the computer's LAN IP address, such as `http://192.168.1.20/bakehub-api/api.php`.

To point the app at a hosted API, pass a Gradle property when building:

```bash
./gradlew assembleDebug -PbakehubApiUrl=https://your-domain.example/bakehub-api/api.php
```

Android Studio users can add `bakehubApiUrl=https://your-domain.example/bakehub-api/api.php` to `gradle.properties`. Local HTTP cleartext is enabled for development; hosted deployments should use HTTPS.

## REST API contract

| Method | Path | Purpose |
|---|---|---|
| GET | `/api.php/health` | Check API and database connectivity |
| POST | `/api.php/auth/login` | Login with `{ "email": "...", "password": "..." }` |
| GET | `/api.php/recipes?search=banana` | List/search recipes |
| GET | `/api.php/recipes/r1` | Get one recipe with ingredients and steps |
| POST | `/api.php/recipes` | Create a recipe with title, ingredients, and steps |
| POST | `/api.php/recipes/r1/save` | Save a recipe for the demo user |
| GET | `/api.php/me/recipe-box` | List saved recipes for the demo user |

## Project structure

```text
BakeHubApp/
├─ api/
│  ├─ api.php       # PDO REST router
│  └─ schema.sql    # MySQL schema + seed data
└─ app/src/main/java/com/bakehub/app/
   ├─ data/BakeHubApi.kt
   ├─ data/Models.kt
   └─ ui/screens/   # Compose screens wired to the API
```

The original mock dataset remains as a deliberate offline fallback, which lets the UI stay usable before Apache/MySQL is started or when a hosted API is temporarily unreachable.
