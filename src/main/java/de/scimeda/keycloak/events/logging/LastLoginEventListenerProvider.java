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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
        if (!EventType.LOGIN.equals(event.getType())) {
            return;
        }
        RealmModel realm = this.model.getRealm(event.getRealmId());
        UserModel user = this.session.users().getUserById(realm, event.getUserId());

        if (user == null) {
            return;
        }
        log.info("Updating last login status for user: " + event.getUserId());

        Map<String, List<String>> userAttrs = user.getAttributes();
        if (userAttrs.containsKey("last-login")) {
            List<String> userLastLogin = userAttrs.get("last-login");
            if (userLastLogin != null && !userLastLogin.isEmpty()) {
                user.setSingleAttribute("prior-login", userLastLogin.get(0));
            }
        }

        // Use current server time for login event
        OffsetDateTime loginTime = OffsetDateTime.now(ZoneOffset.UTC);
        String loginTimeS = DateTimeFormatter.ISO_DATE_TIME.format(loginTime);
        user.setSingleAttribute("last-login", loginTimeS);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
    }

    @Override
    public void close() {
        // Nothing to close
    }

}
