package com.saptarshi.doubtconnect.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleIdentityService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    public String verifyAndGetEmail(String idTokenString)
            throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                        .setAudience(Collections.singletonList(clientId))
                        .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            return null;
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        Boolean emailVerified = (Boolean) payload.get("email_verified");

        if (emailVerified == null || !emailVerified) {
            return null;
        }

        return payload.getEmail();
    }

}