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
//source:https://stackoverflow.com/questions/44185432/firebase-admin-sdk-with-java/47247539#47247539

@Configuration
public class FirebaseConfig {

    private static final String GCP_PROJECT_ID = "spkhytta";
    private static final String SECRET_ID = "firebase-admin-key";

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount;

            if (System.getenv("GOOGLE_CLOUD_PROJECT") != null) {
                // Kjører i GCP – bruk Secret Manager
                String secretPayload = getSecret();
                serviceAccount = new ByteArrayInputStream(secretPayload.getBytes());
            } else {
                // Kjører lokalt – bruk lokal JSON-nøkkel
                serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-adminsdk.json");
                if (serviceAccount == null) {
                    throw new IOException("firebase-adminsdk.json not found in resources.");
                }
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options);
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

/*
package com.bookingapp.cabin.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.io.InputStream;
//source:https://stackoverflow.com/questions/44185432/firebase-admin-sdk-with-java/47247539#47247539

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()){
            try(InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-adminsdk.json")) {
                if (serviceAccount == null) {
                    throw new IOException("Kunne ikke finne firebase-adminsdk.json.");
                }

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options);
            }
        }
        return FirebaseApp.getInstance();
    }
}
*/