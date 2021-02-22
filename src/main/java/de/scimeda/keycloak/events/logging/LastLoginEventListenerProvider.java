package de.scimeda.keycloak.events.logging;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        // log.infof("## NEW %s EVENT", event.getType());
        if (EventType.LOGIN.equals(event.getType())) {
            RealmModel realm = this.model.getRealm(event.getRealmId());
            UserModel user = this.session.users().getUserById(event.getUserId(), realm);

            if (user != null) {
                log.info("Updating last login status for user: " + event.getUserId());

                Map userAttrs = user.getAttributes();
                if (userAttrs.containsKey("last-login")) {
                    String userLastLogin = userAttrs.get("last-login").toString();
                    user.setSingleAttribute("prior-login", userLastLogin.substring(1, userLastLogin.length()-1));
                }

                // Use current server time for login event
                LocalDateTime loginTime = LocalDateTime.now();
                String loginTimeS = DateTimeFormatter.ISO_DATE_TIME.format(loginTime);
                user.setSingleAttribute("last-login", loginTimeS);
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
    }

    @Override
    public void close() {
        // Nothing to close
    }

}