package com.example.dangle_lotto;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class FirebaseManager {
    private final FirebaseFirestore db;
    private final CollectionReference users;
    private final CollectionReference events;
    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
        events = db.collection("events");
    }
    /**
     * Adds a new user to the database and instantiates a user object with all required attributes.
     * <p>
     * Pass null into phone and pid if user has decided not to provide that information.
     *
     * @param uid  user id. is a string provided by firebase auth that can uniquely identify a user
     * @param name  Name of the user
     * @param email  Email of the user
     * @param phone  Phone number of the user - set null if not provided
     * @param pid  Photo id for user profile picture - set null if not provided
     * @param canOrganize  Boolean value indicating whether the user can organize events
     * @return Instantiated GeneralUser object with all required attributes
     */
    public User createNewUser(String uid, String name, String email, String phone, String pid, boolean canOrganize){
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
     * Updates a user's information in the database.
     * <p>
     * Is the update method for all users in general. canOrganize does NOT get updated here for
     * GeneralUser and will be handled separately in admin related functions
     *
     * @param user  User object containing all required attributes
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
     * Deletes a user from the database.
     *
     * @param uid  string of user id to delete from database
     */
    public void deleteUser(String uid) {
        users.document(uid).delete();
    }

    /**
     * Retrieves a user from the database and instantiates an object for them
     *
     * @param uid  string of user id to search for and retrieve all attributes
     * @return Instantiated GeneralUser object with all required attributes
     */
    public void getUser(String uid, FirestoreCallback<User> callback) {
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
        }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a new event to the database and instantiates an object for it.
     *
     * @param oid  Organizer id of the event
     * @param name  Name of the event
     * @param datetime  Timestamp of the event
     * @param location  Location of the event
     * @param description  Description of the event
     * @param eventSize  Size of the event
     * @param pid  Photo id for event banner
     *
     * @return Instantiated Event object with all required attributes
     */
    public Event createEvent(String oid, String name, Timestamp datetime, String location, String description, int eventSize, String pid){
        String eid = events.document().getId();
        Map<String, Object> data = Map.of(
                "Organizer", oid,
                "Name", name,
                "Date", datetime,
                "Location", location,
                "Description", description,
                "Event Size", eventSize,
                "Picture", pid
        );

        events.document(eid).set(data);
        return new Event(eid, oid, name, datetime, location, description, pid, eventSize, this);
    }

    /**
     * Updates an event's information in the database.
     *
     * @param event  string of user id to search for and retrieve all attributes
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
     * Deletes an event from the database.
     *
     * @param eid  string of user id to search for and retrieve all attributes
     */
    public void deleteEvent(String eid) {
        events.document(eid).delete();
    }

    public Event documentToEvent(DocumentSnapshot doc) {
        return new Event(
                doc.getId(),
                doc.getString("Organizer"),
                doc.getString("Name"),
                doc.getTimestamp("Date"),
                doc.getString("Location"),
                doc.getString("Description"),
                doc.getString("Picture"),
                Objects.requireNonNull(doc.getLong("Event Size")).intValue(),
                this
        );
    }

    /**
     * Retrieves an event from the database and instantiates an object for it.
     *
     * @param eid  string of user id to search for and retrieve all attributes
     * @param callback callback function to call when event is retrieved
     */
    public void getEvent(String eid, FirestoreCallback<Event> callback){
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
        }).addOnFailureListener(callback::onFailure);
    }

    public void getUserSubcollection(String id, String subcollection, FirestoreCallback<ArrayList<String>> callback){
        users.document(id).collection(subcollection).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> ids = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    ids.add(doc.getId());
                }
                callback.onSuccess(ids);
            }else {
                callback.onFailure(task.getException());
            }
        }).addOnFailureListener(callback::onFailure);
    }

    public void getEventSubcollection(String id, String subcollection, FirestoreCallback<ArrayList<String>> callback){
        events.document(id).collection(subcollection).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> ids = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    ids.add(doc.getId());
                }
                callback.onSuccess(ids);
            }else {
                callback.onFailure(task.getException());
            }
        }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a user to the sign-up list for an event in the database.
     * <p>
     * Only adds if user is not yet in the list, otherwise throws an exception.
     * ONLY USE THIS FUNCTION WHEN ADDING A NEW USER TO AN EVENT
     *
     * @param user  User object containing all required attributes
     * @param event  Event object containing all required attributes
     */
    public void userSignUp(User user, Event event) {
        Map<String, Object> data = Map.of(
                "SignUpTime", Timestamp.now()
        );
        event.addSignUp(user.getUid());
        users.document(user.getUid()).collection("SignUps").document(event.getEid()).set(data);
        events.document(event.getEid()).collection("SignUps").document(user.getUid()).set(data);
    }
    /**
     * Deletes a user from the sign-up list for an event in the database.
     * <p>
     * Only deletes if user is already in the list, otherwise throws an exception.
     * ONLY USE THIS FUNCTION WHEN DELETING A USER FROM EVENT.
     *
     * @param user  User object containing all required attributes
     * @param event  Event object containing all required attributes
     */
    public void userCancelSignUp(User user, Event event) {
        event.cancelSignUp(user.getUid());
        users.document(user.getUid()).collection("SignUps").document(event.getEid()).delete();
        events.document(event.getEid()).collection("SignUps").document(user.getUid()).delete();
    }

    /**
     * Adds a user to the registered list for an event in the database.
     * <p>
     * ONLY USE THIS FUNCTION WHEN REGISTERING A NEW USER FOR AN EVENT
     *
     * @param user  User object containing all required attributes
     * @param event  Event object containing all required attributes
     */
    public void userRegister(User user, Event event){
        // add register time to user's event document and event's signup document
        Map<String, Object> data = Map.of(
                "RegisterTime", Timestamp.now()
                );
        event.addRegistered(user.getUid());
        users.document(user.getUid()).collection("Registered").document(event.getEid()).set(data);
        events.document(event.getEid()).collection("Registrants").document(user.getUid()).set(data);
    }
    /**
     * Removes a user from the registered list for an event in the database.
     * <p>
     * ONLY USE THIS FUNCTION WHEN UNREGISTERING A NEW USER FOR AN EVENT
     *
     * @param user  User object containing all required attributes
     * @param event  Event object containing all required attributes
     */
    public void userUnregister(User user, Event event) {
        event.deleteRegistered(user.getUid());
        users.document(user.getUid()).collection("Registered").document(event.getEid()).delete();
        events.document(event.getEid()).collection("Registrants").document(user.getUid()).delete();
    }

    // implement for chosen and cancelled and stuff
//    public void userChooseEvent(User user, Event event);
//
//    public void userCancelChosenEvent(User user, Event event);
//
//    public ArrayList<String> getChosenEvents(String uid);
//
//    public ArrayList<String> getEventChosenUsers(String eid);
//
//    public void userCancelEvent(User user, Event event);
//
//    public void userReinstateEvent(User user, Event event);


    // Querying
    public void getEventsQuery(DocumentSnapshot lastVisible, int numEvents, FirestoreCallback<ArrayList<DocumentSnapshot>> callback){
        Query query = events.orderBy("Date", Query.Direction.DESCENDING).limit(numEvents);

        if (lastVisible != null) query = query.startAfter(lastVisible);
        query.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<DocumentSnapshot> events = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            events.add(doc);
                        }
                        callback.onSuccess(events);
                    }else{
                        callback.onFailure(task.getException());
                    }
                }).addOnFailureListener(callback::onFailure);
    }


}
