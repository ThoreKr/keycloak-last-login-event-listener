package de.scimeda.keycloak.events.logging;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmProvider;

import java.time.Instant;
import java.time.ZoneOffset;

public class LastLoginEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(LastLoginEventListenerProvider.class);

    private final KeycloakSession session;
    private final RealmProvider model;

    public LastLoginEventListenerProvider(KeycloakSession session) {
        this.session = session;
        this.model = session.realms();
    }

    @Override
    public void onEvent(Event event) {
        if (!EventType.LOGIN.equals(event.getType())) {
            return;
        }
        var realm = model.getRealm(event.getRealmId());
        var user = session.users().getUserById(realm, event.getUserId());

        if (user == null) {
            return;
        }
        log.info("Updating last login status for user: " + user.getUsername());

        var userAttrs = user.getAttributes();
        if (userAttrs.containsKey("last-login")) {
            var userLastLogin = userAttrs.get("last-login");
            if (userLastLogin != null && !userLastLogin.isEmpty()) {
                user.setSingleAttribute("prior-login", userLastLogin.get(0));
            }
        }

        // event timestamp
        var loginTime = event.getTime();
        var loginTimeS = Instant.ofEpochMilli(loginTime).atOffset(ZoneOffset.UTC).toString();

        user.setSingleAttribute("last-login", loginTimeS);
        user.setSingleAttribute("last-login-timestamp", Long.toString(loginTime));
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
    }

    @Override
    public void close() {
        // Nothing to close
    }

}
