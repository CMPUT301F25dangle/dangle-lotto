package com.example.dangle_lotto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * FirebaseManager — handles all Firebase-related operations for authentication, users, and events.
 * <p>
 * This class serves as a data manager for Firestore and Firebase Authentication,
 * handling creation, retrieval, update, and deletion (CRUD) operations for both users and events.
 * It also manages bidirectional relationships between users and events via subcollections.
 * </p>
 *
 * @author Mahd
 * @version 1.0
 * @since 2025-11-01
 */
public class FirebaseManager {
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final CollectionReference users;
    private final CollectionReference events;
    private final FirebaseStorage storage;
    private final StorageReference storageRef;

    private final String[] collections = new String[]{"Register", "Chosen", "SignUps", "Cancelled"};
    private final FirebaseIdlingResource idlingResource = new FirebaseIdlingResource();
    private static FirebaseManager instance;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = db.collection("users");
        events = db.collection("events");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public static FirebaseManager getInstance(){
        if (instance == null){
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Returns a reference to the database.
     *
     * @return  Reference to db
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Returns a reference to the authentication.
     *
     * @return  Reference to auth
     */
    public FirebaseAuth getAuth() {
        return mAuth;
    }

    /**
     * Returns the idling resource.
     *
     * @return  Reference to idling resource
     */
    public FirebaseIdlingResource getIdlingResource(){ return idlingResource; }

    public CollectionReference getUsersReference(){ return users; }
    public CollectionReference getEventsReference(){ return events; }

    /**
     * Signing in a user with email and password. Send uid to callback function if successful.
     *
     * @param email  Email of the user
     * @param password  Password of the user
     * @param callback  Callback function to call when user is created
     */
    public void signIn(String email, String password, FirebaseCallback<String> callback){
        // idling resource for testing
        idlingResource.increment();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid;
                        if (user != null) {
                            uid = user.getUid();

                        } else {
                            uid = null;
                        }
                        callback.onSuccess(uid);
                    } else {
                        callback.onFailure(task.getException());
                    }
                    callback.onComplete();

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

    /**
     * Create new user in the database and instantiates a user object with all required attributes.
     * <p>
     * Pass null into phone and photo_id if user has decided not to provide that information.
     *
     * @param email  Email of the user
     * @param password  Password of the user
     * @param name  Name of the user
     * @param phone  Phone number of the user - set null if not provided
     * @param photo_id  Photo id for user profile picture - set null if not provided
     * @param canOrganize  Boolean value indicating whether the user can organize events
     * @param callback  Callback function to call when user is created
     */
    public void signUp(String email, String password, String name, String phone, String photo_id, boolean canOrganize, FirebaseCallback<String> callback) {
        // idling resource for testing
        idlingResource.increment();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null){
                            String uid = user.getUid();
                            this.createNewUser(uid, name, email, phone, photo_id, canOrganize);
                            callback.onSuccess(uid);
                        }else{
                            callback.onFailure(new Exception("User not found"));
                        }
                    }else{
                        callback.onFailure(task.getException());
                    }
                    callback.onComplete();

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

    public void getAllUsers(FirebaseCallback<ArrayList<String>> callback){
        users.get().addOnCompleteListener(task -> {
            ArrayList<String> userList = new ArrayList<>();
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    userList.add(doc.getId());
                }
                callback.onSuccess(userList);
            }else{
                callback.onFailure(task.getException());
            }
        });
    }

    /**
     * Creates and stores a new user document in Firestore.
     *
     * @param uid          Firebase Auth user ID.
     * @param name         User name.
     * @param email        User email.
     * @param phone        User phone number (nullable).
     * @param pid          Profile photo ID (nullable).
     * @param canOrganize  Whether the user can organize events.
     * @return Instantiated {@link GeneralUser} object.
     */
    public GeneralUser createNewUser(String uid, String name, String email, String phone, String pid, boolean canOrganize){
        Map<String, Object> data = Map.of(
                "Name", name,
                "Email", email,
                "Phone", phone,
                "Picture", pid,
                "CanOrganize", canOrganize
        );

        users.document(uid).set(data);
        return new GeneralUser(uid, name, email, phone, pid,this, canOrganize);
    }

    /**
     * Updates an existing user’s data in Firestore.
     *
     * @param user {@link User} object containing updated fields.
     */
    public void updateUser(User user) {
        users.document(user.getUid()).update(
                "Name", user.getName(),
                "Email", user.getEmail(),
                "Phone", user.getPhone(),
                "Picture", user.getPhotoID()
        );
    }

    /**
     * Deletes a user and all related data from Firestore.
     * Removes references from all events and subcollections the user participated in or organized.
     *
     * @param uid User ID to delete.
     */
    public Task<Void> deleteUser(String uid) {
        idlingResource.increment();

        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        List<Task<Void>> allTasks = new ArrayList<>();

        // 1. Delete from all subcollections
        for (String collection : collections) {
            Task<Void> t = users.document(uid).collection(collection).get()
                    .continueWithTask(task -> {

                        List<Task<Void>> innerDeletes = new ArrayList<>();

                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                String eid = doc.getId();
                                // delete user from event subcollection
                                innerDeletes.add(events.document(eid)
                                        .collection(collection)
                                        .document(uid)
                                        .delete());

                                // delete the subcollection doc from user
                                innerDeletes.add(doc.getReference().delete());
                            }
                        }
                        return Tasks.whenAllComplete(innerDeletes);
                    }).continueWith(task -> null); // normalize to Task<Void>

            allTasks.add(t);
        }

        // 2. Delete events user organized
        Task<Void> organizedTask = users.document(uid)
                .collection("Organize")
                .get()
                .continueWithTask(task -> {
                    List<Task<Void>> eventDeletes = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            eventDeletes.add(deleteEvent(doc.getId()));
                        }
                    }
                    return Tasks.whenAllComplete(eventDeletes);
                }).continueWith(task -> null);

        allTasks.add(organizedTask);

        // 3. Delete profile picture if exists
        Task<Void> deletePhoto = users.document(uid).get()
                .continueWithTask(task -> {
                    String url = task.getResult().getString("Picture");
                    if (url != null && !url.isEmpty()) {
                        StorageReference ref = storage.getReferenceFromUrl(url);
                        return ref.delete();
                    }
                    return Tasks.forResult(null);
                });

        allTasks.add(deletePhoto);

        // 4. Join all operations
        Tasks.whenAllComplete(allTasks)
                .continueWithTask(task -> {
                    // delete user document
                    return users.document(uid).delete();
                })
                .continueWithTask(task -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", uid);
                    // cloud function: delete auth user
                    return FirebaseFunctions.getInstance()
                            .getHttpsCallable("deleteUserAuth")
                            .call(data);
                })
                .addOnSuccessListener(r -> {
                    idlingResource.decrement();
                    tcs.setResult(null);
                })
                .addOnFailureListener(e -> {
                    idlingResource.decrement();
                    tcs.setException(e);
                });

        return tcs.getTask();
    }


    /**
     * Retrieves a {@link GeneralUser} by UID from Firestore.
     *
     * @param uid       User ID.
     * @param callback  Callback that receives the retrieved user object.
     */
    public void getUser(String uid, FirebaseCallback<GeneralUser> callback) {
        // idling resource for testing
        idlingResource.increment();

        users.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Map<String, Object> data = doc.getData();
                    assert data != null;
                    String name = (String) data.get("Name");
                    String email = (String) data.get("Email");
                    Boolean canOrganize = (Boolean) data.get("CanOrganize");
                    String phone = (String) data.get("Phone");
                    String pid = (String) data.get("Picture");
                    GeneralUser user = new GeneralUser(uid, name, email, phone, pid, this, Boolean.TRUE.equals(canOrganize));
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("User not found"));
                }
            }else{
                callback.onFailure(task.getException());
            }

            // idling resource for testing
            idlingResource.decrement();

        }).addOnFailureListener(error -> {
            callback.onFailure(error);

            // idling resource for testing
            idlingResource.decrement();
        });
    }

    public void revokeOrganizer(String uid){
        users.document(uid).update("CanOrganize", false);
    }

    public void grantOrganizer(String uid){
        users.document(uid).update("CanOrganize", true);
    }

    public void getAdmin(String uid, FirebaseCallback<AdminUser> callback){
        users.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Map<String, Object> data = doc.getData();
                    assert data != null;
                    String name = (String) data.get("Name");
                    String email = (String) data.get("Email");
                    String phone = (String) data.get("Phone");
                    String pid = (String) data.get("Picture");
                    AdminUser user = new AdminUser(uid, name, email, phone, pid, this);
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("User not found"));
                }
            }else{
                callback.onFailure(task.getException());
            }
        }).addOnFailureListener(callback::onFailure);
    }

    public void getAllEvents(FirebaseCallback<ArrayList<String>> callback){
        events.get().addOnCompleteListener(task -> {
            ArrayList<String> eventList = new ArrayList<>();
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    eventList.add(doc.getId());
                }
                callback.onSuccess(eventList);
            }else{
                callback.onFailure(task.getException());
            }
        });
    }


    /**
     * Creates and uploads a new event document to Firestore.
     *
     * @param oid          Organizer ID.
     * @param name         Event name.
     * @param datetime     Event timestamp.
     * @param location     Event location.
     * @param description  Event description.
     * @param eventSize    Event size limit.
     * @param maxEntrants  Maximum number of registrants.
     * @param photo_url    Event photo URL for reference into firebase storage. Null if no photo provided.
     * @param categories   List of event categories.
     * @return Instantiated {@link Event} object.
     */
    public Event createEvent(String oid, String name, Timestamp datetime, String location, String description, int eventSize,
                             int maxEntrants, String photo_url, ArrayList<String> categories){
        String eid = events.document().getId();
        Map<String, Object> data = Map.of(
                "Organizer", oid,
                "Name", name,
                "Date", datetime,
                "Location", location,
                "Description", description,
                "Event Size", eventSize,
                "Max Entrants", maxEntrants,
                "Picture", photo_url,
                "Categories", categories
        );
        users.document(oid).collection("Organize").document(eid).set(Map.of("Timestamp", datetime));
        events.document(eid).set(data);
        return new Event(eid, oid, name, datetime, location, description, photo_url, eventSize, maxEntrants, categories, this);
    }

    /**
     * Updates event data in Firestore.
     *
     * @param event The event object containing updated values.
     */
    public void updateEvent(Event event) {
        events.document(event.getEid()).update(
                "Name", event.getName(),
                "Date", event.getDate(),
                "Location", event.getLocation(),
                "Description", event.getDescription(),
                "Event Size", event.getEventSize()
        );
    }

    /**
     * Deletes an event from the database. Recursively goes through all users the event has been registered for and also deletes all of those references.
     * Waits for the asynchronous calls to complete before deleting the event at the end.
     *
     * @param eid  string of user id to search for and retrieve all attributes
     */
    public Task<Void> deleteEvent(String eid) {
        // idling resource for testing
        idlingResource.increment();

        List<Task<Void>> allTasks = new ArrayList<>();

        // 1. delete all event references in user subcollections
        for (String collection : collections) {
            Task<Void> t = events.document(eid).collection(collection).get().continueWithTask(task -> {
                        List<Task<Void>> innerDeletes = new ArrayList<>();
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                String uid = doc.getId();
                                innerDeletes.add(users.document(uid).collection(collection).document(eid).delete());
                                innerDeletes.add(doc.getReference().delete());
                            }
                        }
                        return Tasks.whenAllComplete(innerDeletes).continueWith(task1 -> null);
                    });
            allTasks.add(t);
        }
        // 2. Delete reference from organizer collection
        Task<Void> deleteOrganizer = events.document(eid).get().continueWithTask(task -> {
            String oid = task.getResult().getString("Organizer");
            if (oid != null) {
                return users.document(oid).collection("Organize").document(eid).delete();
            }
            return Tasks.forResult(null);
        });
        allTasks.add(deleteOrganizer);

        // Delete profile picture if it exists
        Task<Void> deletePhoto = events.document(eid).get().continueWithTask(task -> {
                    String photo_url = task.getResult().getString("Picture");
                    if (photo_url != null && !photo_url.isEmpty()) {
                        StorageReference ref = storage.getReferenceFromUrl(photo_url);
                        return ref.delete();
                    }
            return null;
        });

        allTasks.add(deletePhoto);
        // Delete event from database
        return Tasks.whenAllComplete(allTasks).continueWithTask(task -> {
            events.document(eid).delete();
            idlingResource.decrement();
            return null;
        });
    }

    /**
     * Converts a Firestore document snapshot into an {@link Event} object.
     *
     * @param doc The Firestore document snapshot.
     * @return An instantiated Event object.
     */
    public Event documentToEvent(DocumentSnapshot doc) {

        Long maxEntrantsLong = doc.getLong("Max Entrants");
        Integer maxEntrants = (maxEntrantsLong != null) ? maxEntrantsLong.intValue() : null;

        return new Event(
                doc.getId(),
                doc.getString("Organizer"),
                doc.getString("Name"),
                doc.getTimestamp("Date"),
                doc.getString("Location"),
                doc.getString("Description"),
                doc.getString("Picture") != null ? doc.getString("Picture") : "",
                Objects.requireNonNull(doc.getLong("Event Size")).intValue(),
                maxEntrants,
                doc.get("Categories") != null ? (ArrayList<String>) doc.get("Categories") : new ArrayList<>(),
                this
        );
    }

    /**
     * Retrieves an event from the database and instantiates an object for it.
     *
     * @param eid  string of user id to search for and retrieve all attributes
     * @param callback callback function to call when event is retrieved
     */
    public void getEvent(String eid, FirebaseCallback<Event> callback){
        // idling resource for testing
        idlingResource.increment();

        events.document(eid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    callback.onSuccess(documentToEvent(doc));
                } else {
                    callback.onFailure(new Exception("Event not found"));
                }
            }else {
                callback.onFailure(task.getException());
            }

            // idling resource for testing
            idlingResource.decrement();

        }).addOnFailureListener(error -> {
            callback.onFailure(error);

            // idling resource for testing
            idlingResource.decrement();
        });
    }

    /**
     * Retrieves a subcollection of an event from the database. Calls the provided callback function when event has been received.
     *
     * Usage: getUserSubcollection(uid, "collection name", new FirebaseCallback&lt;ArrayList&lt;String&gt;&gt;() {
     * <pre>{@code
     *      @Override
     *      public void onSuccess(ArrayList<String> result) {
     *          // define what to do with result
     *      }
     *
     *      @Override
     *       public void onFailure(Exception e) {
     *                 // define what to do on failure case
     *             }
     *         });
     *  }</pre>
     * @param uid  string of user id to search for and retrieve all attributes
     * @param subcollection  string of subcollection to retrieve
     * @param callback callback function to call when event is retrieved
     */
    public void getUserSubcollection(String uid, String subcollection, FirebaseCallback<ArrayList<String>> callback){
        // idling resource for testing
        idlingResource.increment();

        users.document(uid).collection(subcollection).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> ids = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    ids.add(doc.getId());
                }
                callback.onSuccess(ids);
            }else {
                callback.onFailure(task.getException());
            }

            // idling resource for testing
            idlingResource.decrement();

        }).addOnFailureListener(error -> {
            callback.onFailure(error);

            // idling resource for testing
            idlingResource.decrement();
        });
    }

    /**
     * Retrieves a subcollection of an event from the database. Calls the provided callback function when event has been received.
     *
     * <pre>{@code
     * Usage: getEventSubcollection(eid, "collection name", new FirebaseCallback&lt;ArrayList&lt;String&gt;&gt;() {
     *      @Override
     *      public void onSuccess(ArrayList<String> result) {
     *          // define what to do with result
     *      }
     *
     *      @Override
     *       public void onFailure(Exception e) {
     *                 // define what to do on failure case
     *             }
     *         });
     * }</pre>
     * @param eid  string of user id to search for and retrieve all attributes
     * @param subcollection  string of subcollection to retrieve
     * @param callback callback function to call when event is retrieved
     */
    public void getEventSubcollection(String eid, String subcollection, FirebaseCallback<ArrayList<String>> callback){
        // idling resource for testing
        idlingResource.increment();

        events.document(eid).collection(subcollection).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> ids = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    ids.add(doc.getId());
                }
                callback.onSuccess(ids);
            }else {
                callback.onFailure(task.getException());
            }

            // idling resource for testing
            idlingResource.decrement();

        }).addOnFailureListener(error -> {
            callback.onFailure(error);

            // idling resource for testing
            idlingResource.decrement();
        });
    }

    /**
     * Adds a user to the requested list for an event in the database.
     *
     * @param uid  User id
     * @param eid  Event id
     * @param subcollection  string of subcollection to retrieve
     */
    public Task<Void> userAddStatus(String uid, String eid, String subcollection){
        // add register time to user's event document and event's signup document
        Map<String, Object> data = Map.of(
                "Timestamp", Timestamp.now()
                );
        Task<Void> t1 = users.document(uid).collection(subcollection).document(eid).set(data);
        Task<Void> t2 = events.document(eid).collection(subcollection).document(uid).set(data);
        return Tasks.whenAll(t1, t2);
    }
    /**
     * Removes a user from the requested list for an event in the database.
     *
     * @param uid  User object containing all required attributes
     * @param eid  Event object containing all required attributes
     * @param subcollection  string of subcollection to retrieve
     */
    public Task<Void> userRemoveStatus(String uid, String eid, String subcollection) {
        Task<Void> t1 = users.document(uid).collection(subcollection).document(eid).delete();
        Task<Void> t2 = events.document(eid).collection(subcollection).document(uid).delete();
        return Tasks.whenAll(t1, t2);
    }


    /**
     * Retrieve a list of events from the database.
     *
     * @param lastVisible  last visible event
     * @param query  query to execute asynchronously
     * @param callback  callback function to call when event is retrieved
     */
    public void getQuery(DocumentSnapshot lastVisible, Query query, FirebaseCallback<ArrayList<DocumentSnapshot>> callback){
        if (lastVisible != null) query = query.startAfter(lastVisible);

        // idling resource for testing
        idlingResource.increment();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<DocumentSnapshot> response = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    response.add(doc);
                }
                callback.onSuccess(response);
            }else{
                callback.onFailure(task.getException());
            }
            callback.onComplete();

            // idling resource for testing
            idlingResource.decrement();

        }).addOnFailureListener(error -> {
            callback.onFailure(error);

            // idling resource for testing
            idlingResource.decrement();
        });
    }


    public void uploadBannerPic(Uri fileUri, FirebaseCallback<String> callback){
        String pid = UUID.randomUUID().toString(); // unique id for picture
        StorageReference imgRef = storageRef.child("banners/" + pid);

        imgRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(callback::onFailure);
    }

    public void uploadProfilePic(Uri fileUri, FirebaseCallback<String> callback){
        String pid = UUID.randomUUID().toString(); // unique id for picture
        StorageReference imgRef = storageRef.child("profiles/" + pid);

        imgRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(callback::onFailure);
    }

    public void uploadQR(Bitmap qr_bitmap, FirebaseCallback<String> callback){
        String pid = UUID.randomUUID().toString(); // unique id for picture
        StorageReference imgRef = storageRef.child("qr/" + pid);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qr_bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        imgRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(callback::onFailure);
    }

    // DOWNLOAD URL WILL CHANGE BECAUSE FIREBASE REGENS THE TOKEN
    public void editPic(Uri fileUri, String imageUrl, FirebaseCallback<String> callback){
        StorageReference imgRef = storage.getReferenceFromUrl(imageUrl);
        imgRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(callback::onFailure);
    }

    public void deletePic(String imageUrl, FirebaseCallback<Void> callback){
        StorageReference imgRef = storage.getReferenceFromUrl(imageUrl);
        imgRef.delete()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }
}
