# Cloud-Feed-Backend
GCP App Engine app that provides real time notifications for Cloud-Feed on supported Rss feeds. The app listens to Firestore changes and manages feed supcriptions/unsubcriptions and triggers notifications.

# Getting started
```
git clone
```
Make sure you add your firebase service account key in Firebase.java. 

To run:
```
mvn appengine:run
```
To deploy:
```
mvn appengine:deploy
```
You need to install Cloud SDK and java components beforehand.

# Firestore 

The firestore database has the following structure:

![](https://github.com/GrigoreAlexandru/Cloud-Feed-Backend/blob/master/firestore.png?raw=true)

Every document in the "feeds" collection represents a feed, its id is the url in base64. The app listens to document changes in this collections and subcribes to the feed when a new document is created. Likewise, when a document is deleted, it unsubscribes.

The "users" collection represents a user containing all the subscribed feeds, used to sync between devices.
