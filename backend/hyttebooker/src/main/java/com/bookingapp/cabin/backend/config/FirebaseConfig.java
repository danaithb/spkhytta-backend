package com.bookingapp.cabin.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

// Henter Firebase-nøkkelen fra Google Secret Manager i stedet for å lagre den som en fil

@Configuration
public class FirebaseConfig {

    private static final String GCP_PROJECT_ID = "test2-hyttebooker";
    private static final String SECRET_ID = "firebase-admin-key";

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // Hent hemmeligheten fra GCP Secret Manager
            String secretPayload = getSecret();
            try (InputStream serviceAccount = new ByteArrayInputStream(secretPayload.getBytes())) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                return FirebaseApp.initializeApp(options);
            }
        }
        return FirebaseApp.getInstance();
    }

    private String getSecret() throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            String secretName = String.format("projects/%s/secrets/%s/versions/latest", GCP_PROJECT_ID, SECRET_ID);
            AccessSecretVersionResponse response = client.accessSecretVersion(secretName);
            return response.getPayload().getData().toStringUtf8();
        }
    }
}