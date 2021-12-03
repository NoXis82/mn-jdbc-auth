package com.noxis.auth;

import com.noxis.auth.persistence.UserEntity;
import com.noxis.auth.persistence.UserRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.Nullable;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;


@Singleton
public class JDBCAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCAuthenticationProvider.class);
    private final UserRepository users;

    public JDBCAuthenticationProvider(final UserRepository users) {
        this.users = users;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            @Nullable HttpRequest<?> httpRequest,
            final AuthenticationRequest<?, ?> authenticationRequest
    ) {
        final String identity = (String) authenticationRequest.getIdentity();
        LOG.debug("User {} tries to login", identity);
        return Flowable.create(emitter -> {
            final Optional<UserEntity> maybeUser = users.findByEmail(identity);
            if (maybeUser.isPresent()) {
                LOG.debug("Found user: {}", maybeUser.get().getEmail());
                final String secret = (String) authenticationRequest.getSecret();
                if (maybeUser.get().getPassword().equals(secret)) {
                    //pass
                    LOG.debug("User logged in...");
                    final HashMap<String, Object> attributes = new HashMap<>();
                    attributes.put("hair_color", "brown");
                    attributes.put("language", "en");
                    var userDetails = AuthenticationResponse.success(
                            identity,
                            Collections.singletonList("ROLE_USER"),
                            attributes
                    );
                    emitter.onNext(userDetails);
                    emitter.onComplete();
                    return;
                } else {
                    LOG.debug("Wrong password provided for user {}", identity);
                }
            } else {
                LOG.debug("No user found with email: {}", identity);
            }
            emitter.onError(new AuthenticationException(new AuthenticationFailed("Wrong username or password")));
        }, BackpressureStrategy.ERROR);
    }
}
