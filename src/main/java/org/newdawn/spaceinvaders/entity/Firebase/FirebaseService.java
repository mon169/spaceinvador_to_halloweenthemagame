/*package org.newdawn.spaceinvaders.Firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;

public class FirebaseService {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        try {
            InputStream serviceAccount = FirebaseService.class.getResourceAsStream("/firebase-key.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://<YOUR_PROJECT_ID>.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            initialized = true;
            System.out.println("🔥 Firebase initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }
}
*/