package com.noxis;

import com.noxis.auth.persistence.UserEntity;
import com.noxis.auth.persistence.UserRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TestDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataProvider.class);
    private final UserRepository users;

    public TestDataProvider(final UserRepository users) {
        this.users = users;
    }
    @EventListener
    public void init(StartupEvent event) {
        LOG.debug("Event {}: ", event.toString());
        final String email = "alice@exsample.com";
        if (users.findByEmail(email).isEmpty()) {
            final UserEntity alice = new UserEntity();
            alice.setEmail(email);
            alice.setPassword("secret");
            users.save(alice);
            LOG.debug("Insert user {}", email);
        }
    }
}
