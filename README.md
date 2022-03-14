# Keycloak Last Login Event Listener

Sets a last login attribute on the user model when they login.

This is based on the templates provided by [zonaut](https://github.com/zonaut/keycloak-extensions).

Building can be done via `gradlew build`, which will locate a `.jar` in the the `build/libs`. Copy/Move it in your keycloak `providers` folders. Add `last_login` to the event listeners under "Events"->"Config" in the Keycloak UI. Please take a look at the aforementioned repository for a far more detailed introduction.

This repository will most likely not keep up with the keycloak release cycle. You have been warned.
