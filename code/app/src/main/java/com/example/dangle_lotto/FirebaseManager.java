package com.example.dangle_lotto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

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
    private final FirebaseFunctions functions;

    private final String[] collections = new String[]{"Register", "Chosen", "SignUps", "Cancelled", "Organize", "Notifications"};
    private final FirebaseIdlingResource idlingResource = new FirebaseIdlingResource();
    private static FirebaseManager instance;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        users = db.collection("users");
        events = db.collection("events");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        functions = FirebaseFunctions.getInstance();
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
                        FirebaseUser user = mAuth.getCurrentUser();
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
     * @param username Username of the user
     * @param phone  Phone number of the user - set null if not provided
     * @param photo_id  Photo id for user profile picture - set null if not provided
     * @param canOrganize  Boolean value indicating whether the user can organize events
     * @param callback  Callback function to call when user is created
     */
    public void signUp(String email, String password, String name, String username, String phone, String photo_id, boolean canOrganize, FirebaseCallback<String> callback) {
        // idling resource for testing
        // Note: you don't need String did as a parameter as it is automatically set to null by createNewUser()
        idlingResource.increment();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null){
                            String uid = user.getUid();
                            this.createNewUser(uid, name, username, email, phone, photo_id, canOrganize);
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

    public void getNotificationsForUser(String uid, FirebaseCallback<List<DocumentSnapshot>> callback) {
        users.document(uid).collection("Notifications")
                .orderBy("receiptTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> callback.onSuccess(query.getDocuments()))
                .addOnFailureListener(callback::onFailure);
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
     * @param uid         Firebase Auth user ID.
     * @param name        User name.
     * @param username    Username of the user
     * @param email       User email.
     * @param phone       User phone number (nullable).
     * @param pid         Profile photo ID (nullable).
     * @param canOrganize Whether the user can organize events.
     */
    public void createNewUser(String uid, String name, String username, String email, String phone, String pid, boolean canOrganize){
        Map<String, Object> data = new HashMap<>();
        data.put("UID", uid);
        data.put("Username", username);
        data.put("Name", name);
        data.put("Email", email);
        data.put("Phone", phone);
        data.put("Picture", pid);
        data.put("DeviceId", null);
        data.put("CanOrganize", canOrganize);
        data.put("Location", null);
        data.put("notiStatus", true);
        data.put("isAdmin", false); // no admin creation interface in app but can add later

        users.document(uid).set(data);
    }

    /**
     * Returns a unique device ID for the current Android device.
     * <p>
     * This ID is used to identify the device for auto-login purposes.
     *
     * @param context Context of the calling component.
     * @return A string representing the device's unique Android ID.
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    /**
     * Sets or updates the DeviceId field for a given user in Firestore.
     * <p>
     * Used to bind a device to a user for device-based auto-login.
     *
     * @param uid      UID of the user to update.
     * @param deviceId The device ID to associate with this user.
     * @param callback Callback to handle success, failure, and completion events.
     */
    public void setDeviceIdForUser(String uid, String deviceId, FirebaseCallback<Void> callback) {
        Log.d("setDeviceId", "Setting DeviceId to: " + deviceId + " for UID: " + uid);

        users.document(uid)
                .update("DeviceId", deviceId)
                .addOnSuccessListener(aVoid -> {
                    Log.d("setDeviceId", "DeviceId successfully updated");
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("setDeviceId", "Failed to update DeviceId", e);
                    if (callback != null) callback.onFailure(e);
                })
                .addOnCompleteListener(task -> {
                    if (callback != null) callback.onComplete();
                });
    }

    /**
     * Sets or updates the notification status for a given user in Firestore.
     * <p>
     * Used to enable or disable notifications for the user.
     *
     * @param uid       UID of the user to update.
     * @param enabled   True if notifications should be enabled, false otherwise.
     * @param callback  Callback to handle success, failure, and completion events.
     */
    public void updateUserNotificationStatus(String uid, boolean enabled, FirebaseCallback<Void> callback) {
        Log.d("updateNotiStatus", "Setting notifications to: " + enabled + " for UID: " + uid);

        users.document(uid)
                .update("notiStatus", enabled)
                .addOnSuccessListener(aVoid -> {
                    Log.d("updateNotiStatus", "Notification status successfully updated");
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("updateNotiStatus", "Failed to update notification status", e);
                    if (callback != null) callback.onFailure(e);
                })
                .addOnCompleteListener(task -> {
                    if (callback != null) callback.onComplete();
                });
    }


    /**
     * Retrieves the UID of a user associated with a given device ID.
     * <p>
     * Used for device-based auto-login to identify the user linked to the current device.
     *
     * @param deviceId The device ID to search for in Firestore.
     * @param callback Callback to handle success (returns UID) or failure.
     */
    public void getUserByDeviceId(String deviceId, FirebaseCallback<String> callback) {
        users.whereEqualTo("DeviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String uid = query.getDocuments().get(0).getId();
                        callback.onSuccess(uid);
                    } else {
                        callback.onFailure(new Exception("No user linked to this device"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }



    /**
     * Updates an existing user’s data in Firestore.
     *
     * @param userObj {@link User} object containing updated fields.
     * @param name    New name.
     * @param username New username.
     * @param newEmail New email.
     * @param phone    New phone number.
     * @param photo_id New profile photo ID.
     * @param password New password.
     */
    public void updateUser(User userObj, String name, String username, String newEmail,
                           String phone, String photo_id, String password,
                            FirebaseCallback<Boolean> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), password);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    if (!Objects.equals(newEmail, user.getEmail())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("uid", user.getUid());
                        data.put("newEmail", newEmail);
                        functions.getHttpsCallable("updateUserEmail").call(data)
                                .addOnSuccessListener(s -> {
                                    userObj.setEmail(newEmail);
                                    users.document(userObj.getUid())
                                            .update("Email", userObj.getEmail());
                                }).addOnFailureListener(e -> {
                        });
                    }
                    if (!Objects.equals(photo_id, userObj.getPhotoID())) {
                        userObj.setPhotoID(photo_id);
                    }
                    if (!Objects.equals(phone, userObj.getPhone())) {
                        userObj.setPhone(phone);
                    }
                    if (!Objects.equals(name, userObj.getName())) {
                        userObj.setName(name);
                    }
                    if (!Objects.equals(username, userObj.getUsername())) {
                        userObj.setUsername(username);
                    }

                    users.document(userObj.getUid()).update(
                            "Name", userObj.getName(),
                            "Username", userObj.getUsername(),
                            "Phone", userObj.getPhone(),
                            "Picture", userObj.getPhotoID()
                    );

                    callback.onSuccess(true);
                }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Makes the specified user an admin by setting isAdmin = true.
     *
     * @param uid      UID of the user to update.
     * @param callback Callback for success/failure.
     */
    public void makeUserAdmin(String uid, FirebaseCallback<Boolean> callback) {
        // idling resource for testing
        idlingResource.increment();

        users.document(uid)
                .update("isAdmin", true)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess(true);

                    // idling resource for testing
                    idlingResource.decrement();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

    /**
     * Updates a user's location in Firestore.
     *
     * @param uid User ID
     * @param location Location to update to
     */
    public void updateUserLocation(String uid, GeoPoint location) {
        users.document(uid).update("Location", location)
                .addOnSuccessListener(unused -> {
                })
                .addOnFailureListener(e -> {
                });
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
                    return functions
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
     * Retrieves a {@link User} by UID from Firestore.
     * May be a {@link GeneralUser} or {@link AdminUser}
     *
     * @param uid       User ID.
     * @param callback  Callback that receives the retrieved user object.
     */
    public void getUser(String uid, FirebaseCallback<User> callback) {
        // idling resource for testing
        idlingResource.increment();

        users.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    User user = UserFactory.getUser(doc, this);;
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
                    String username = (String) data.get("Username");
                    String email = (String) data.get("Email");
                    String phone = (String) data.get("Phone");
                    String pid = (String) data.get("Picture");
                    AdminUser user = new AdminUser(uid, name, username, email, phone, pid, this);
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
     * @param start_date   Event start date.
     * @param end_date     Event end date.
     * @param event_date   Event timestamp.
     * @param location     Event location.
     * @param location_required  Whether the location is required to sign up for event.
     * @param description  Event description.
     * @param eventSize    Event size limit.
     * @param maxEntrants  Maximum number of registrants.
     * @param photo_url    Event photo URL for reference into firebase storage. Null if no photo provided.
     * @param categories   List of event categories.
     * @return Instantiated {@link Event} object.
     */
    public Event createEvent(
            String oid, String name, Timestamp start_date, Timestamp end_date,
            Timestamp event_date, String location, Boolean location_required,
            String description, int eventSize, int maxEntrants,
            String photo_url, String qr_url, ArrayList<String> categories
    ) {
        String eid = events.document().getId();
        Map<String, Object> data = new HashMap<>();
        data.put("Organizer", oid);
        data.put("Name", name);
        data.put("Start Date", start_date);
        data.put("End Date", end_date);
        data.put("Event Date", event_date);
        data.put("Location", location);
        data.put("Location Required", location_required);
        data.put("Description", description);
        data.put("Event Size", eventSize);
        data.put("Max Entrants", maxEntrants);
        data.put("Picture", photo_url);
        data.put("QR", qr_url);
        data.put("Categories", categories);

        // idling resource for testing
        idlingResource.increment();

        Timestamp current = Timestamp.now();

        Task<Void> t1 = users.document(oid)
                .collection("Organize")
                .document(eid)
                .set(Map.of("Timestamp", current));

        Task<Void> t2 = events.document(eid).set(data);

        // Wait for both async tasks to finish
        Tasks.whenAllComplete(t1, t2).addOnCompleteListener(task -> {
            // idling resource for testing
            idlingResource.decrement();
        });

        return new Event(eid, oid, name, start_date, end_date, event_date, location, location_required,
                description, photo_url, qr_url, eventSize, maxEntrants, categories, this);
    }

    /**
     * Updates event data in Firestore.
     *
     * @param event The event object containing updated values.
     */
    public void updateEvent(Event event) {
        events.document(event.getEid()).update(
                "Name", event.getName(),
                "Start Date", event.getStartDate(),
                "End Date", event.getEndDate(),
                "Event Date", event.getEventDate(),
                "Organizer", event.getOrganizerID(),
                "Location", event.getLocation(),
                "Location Required", event.isLocationRequired(),
                "Description", event.getDescription(),
                "Event Size", event.getEventSize(),
                "Max Entrants", event.getMaxEntrants(),
                "Picture", event.getPhotoID(),
                "QR", event.getQR()
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

        // Safe integer extraction
        Integer eventSize = null;
        Long eventSizeLong = doc.getLong("Event Size");
        if (eventSizeLong != null) {
            eventSize = eventSizeLong.intValue();
        }

        Integer maxEntrants = null;
        Long maxEntrantsLong = doc.getLong("Max Entrants");
        if (maxEntrantsLong != null) {
            maxEntrants = maxEntrantsLong.intValue();
        }

        // Safe categories list
        ArrayList<String> categories = new ArrayList<>();
        Object catObj = doc.get("Categories");
        if (catObj instanceof ArrayList) {
            categories = (ArrayList<String>) catObj;
        }

        return new Event(
                doc.getId(),
                doc.getString("Organizer"),
                doc.getString("Name"),
                doc.getTimestamp("Start Date"),
                doc.getTimestamp("End Date"),
                doc.getTimestamp("Event Date"),
                doc.getString("Location"),
                doc.getBoolean("Location Required"),
                doc.getString("Description"),

                // Default empty strings instead of null
                doc.getString("Picture") != null ? doc.getString("Picture") : "",
                doc.getString("QR") != null ? doc.getString("QR") : "",

                // Use default value if missing
                eventSize != null ? eventSize : 0,   // or null, your choice
                maxEntrants,

                categories,
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
     * Creates a notification document in the database.
     *
     * @param uid  User id
     * @param eid  Event id
     * @param message message to send to user
     * @param isFromAdmin whether the notification is from an admin
     */
    public void createNotification(String eid, String uid, String message, Boolean isFromAdmin){
        String nid = users.document(uid).collection("Notifications").document().getId();
        Notification newnNoti = new Notification(eid, uid, nid, Timestamp.now(), message, isFromAdmin);
        users.document(uid).collection("Notifications").document(nid).set(newnNoti);
    }

    /**
     * Converts a Firestore document snapshot into a {@link Notification} object.
     *
     * @param nDoc The Firestore document snapshot.
     * @return An instantiated Notification object.
     */
    public Notification notiDocToNoti(DocumentSnapshot nDoc){
        return nDoc.toObject(Notification.class);
    }

    public void deleteNotification(String uid, String nid) {
        users.document(uid).collection("Notifications").document(nid).delete();
    }

    /**
     * Retrieves a subcollection of an event from the database. Calls the provided callback function when event has been received.
     * <p>
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
    public Task<Void> userAddStatus(String uid, String eid, String subcollection) {
        // idling resource for testing
        idlingResource.increment();

        // data for both writes
        Map<String, Object> data = Map.of(
                "Timestamp", Timestamp.now()
        );

        Task<Void> t1 = users.document(uid).collection(subcollection).document(eid).set(data);
        Task<Void> t2 = events.document(eid).collection(subcollection).document(uid).set(data);
        Task<Void> combined = Tasks.whenAll(t1, t2);

        // idling resource for testing
        combined.addOnCompleteListener(task -> {
            idlingResource.decrement();
        });

        return combined;
    }

    /**
     * Removes a user from the requested list for an event in the database.
     *
     * @param uid  User object containing all required attributes
     * @param eid  Event object containing all required attributes
     * @param subcollection  string of subcollection to retrieve
     */
    public Task<Void> userRemoveStatus(String uid, String eid, String subcollection) {
        // idling resource for testing
        idlingResource.increment();

        Task<Void> t1 = users.document(uid).collection(subcollection).document(eid).delete();
        Task<Void> t2 = events.document(eid).collection(subcollection).document(uid).delete();
        Task<Void> combined = Tasks.whenAll(t1, t2);

        // idling resource for testing
        combined.addOnCompleteListener(task -> {
            idlingResource.decrement();
        });

        return combined;
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

    /**
     * Uploads a banner picture to Firebase Storage.
     *
     * @param fileUri Uri of the file to upload
     * @param callback callback function to call when event is retrieved
     */
    public void uploadBannerPic(Uri fileUri, FirebaseCallback<String> callback){
        // idling resource for testing
        idlingResource.increment();

        String pid = UUID.randomUUID().toString(); // unique id for picture
        StorageReference imgRef = storageRef.child("banners/" + pid);

        imgRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imgRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()));

                    // idling resource for testing
                    idlingResource.decrement();
                })
                .addOnFailureListener((error) -> {
                    callback.onFailure(error);

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

    /**
     * Uploads a profile picture to Firebase Storage.
     *
     * @param fileUri Uri of the file to upload
     * @param callback callback function to call when event is retrieved
     */
    public void uploadProfilePic(Uri fileUri, FirebaseCallback<String> callback){
        // idling resource for testing
        idlingResource.increment();

        String pid = UUID.randomUUID().toString(); // unique id for picture
        StorageReference imgRef = storageRef.child("profiles/" + pid);

        imgRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imgRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()));
                })
                .addOnFailureListener(error -> {
                    callback.onFailure(error);

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

    /**
     * Uploads a QR code to Firebase Storage.
     *
     * @param qr_bitmap bitmap of the QR code
     * @param callback callback function to call when event is retrieved
     */
    public void uploadQR(Bitmap qr_bitmap, FirebaseCallback<String> callback){
        // idling resource for testing
        idlingResource.increment();

        String pid = UUID.randomUUID().toString(); // unique id for picture
        StorageReference imgRef = storageRef.child("qr/" + pid);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qr_bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        imgRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    imgRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                callback.onSuccess(uri.toString());

                                // idling resource for testing
                                idlingResource.decrement();
                            })
                            .addOnFailureListener(error -> {
                                callback.onFailure(error);

                                // idling resource for testing
                                idlingResource.decrement();
                            });
                })
                .addOnFailureListener(error -> {
                    callback.onFailure(error);

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

    /**
     * Uploads a replacement picture to Firebase Storage.
     * <p>
     * DOWNLOAD URL WILL CHANGE BECAUSE FIREBASE REGENS THE TOKEN
     *
     * @param fileUri Uri of the file to upload
     * @param imageUrl String URL of the image to replace
     * @param callback callback function to call when event is retrieved
     */
    public void editPic(Uri fileUri, String imageUrl, FirebaseCallback<String> callback){
        StorageReference imgRef = storage.getReferenceFromUrl(imageUrl);
        imgRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes a picture from Firebase Storage.
     * <p>
     * STILL NEED TO DECREMENT IDLING RESOURCE FOR TESTING
     *
     * @param imageUrl String URL of the image to delete
     * @param callback callback function to call when event is retrieved
     */
    public void deletePic(String imageUrl, FirebaseCallback<Void> callback){
        idlingResource.increment();

        StorageReference imgRef = storage.getReferenceFromUrl(imageUrl);
        imgRef.delete()
                .addOnSuccessListener(Void -> {
                    callback.onSuccess(Void);

                    // idling resource for testing
                    idlingResource.decrement();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);

                    // idling resource for testing
                    idlingResource.decrement();
                });
    }

}
