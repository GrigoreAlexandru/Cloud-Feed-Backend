package com.ga.cloudfeed;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseOptions.Builder;
import com.google.firebase.cloud.FirestoreClient;
import org.threeten.bp.Instant;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;


public class Firebase {
    static Firestore db;
    public static final Logger logger = Logger.getLogger(Firebase.class.getName());

    public Firebase() {
    }

    public static synchronized void start() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.info("starting fbase");
            String fbaseServiceAccountKey = "Your firebase service account key";

            try {
                InputStream is = new ByteArrayInputStream(fbaseServiceAccountKey.getBytes());
                GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                FirebaseOptions options = (new Builder()).setCredentials(credentials).build();
                FirebaseApp.initializeApp(options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            db = FirestoreClient.getFirestore();

            CollectionReference col = db.collection("feeds/");

            col.addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    logger.severe("Listen failed: " + e);
                } else {

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {

                        switch (dc.getType()) {

                            case ADDED:
                                if (dc.getDocument().getCreateTime().getNanos() >= Instant.now().minusSeconds(150L).getNano()) {
                                    dc.getDocument().toObject(com.ga.cloudfeed.Feed.class).subscribe();
                                }
                                break;
                            case MODIFIED:
                                break;
                            case REMOVED:
                                dc.getDocument().toObject(com.ga.cloudfeed.Feed.class).unsubscribe();
                                break;
                            default:
                                break;

                        }
                    }

                }
            });
        }

    }
}
