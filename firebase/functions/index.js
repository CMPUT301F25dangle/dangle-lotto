const functions = require("firebase-functions");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

// ✔ Required: direct Admin SDK to Auth emulator
if (process.env.FUNCTIONS_EMULATOR) {
  process.env.FIREBASE_AUTH_EMULATOR_HOST = "127.0.0.1:9099";
  console.log("🔥 Running Auth Admin SDK against emulator");
}

exports.deleteUserAuth = functions.https.onCall(async (data, context) => {
  console.log("Deleting user:", data.data);
  const uid = data.data.uid;

  try {
    await admin.auth().deleteUser(uid);
    console.log("✅ Deleted:", uid);
    return {success: true};
  } catch (err) {
    console.error("❌ Auth delete failed:", err);
    throw new functions.https.HttpsError("internal", err.message);
  }
});

exports.updateUserEmail = functions.https.onCall(async (data, context) => {
  const uid = data.data.uid; // User ID
  const newEmail = data.data.newEmail;

  try {
    await admin.auth().updateUser(uid, {email: newEmail});
    return {success: true, message: "Email updated successfully."};
  } catch (error) {
    console.error("Error updating user email:", error);
    throw new functions.https.HttpsError("internal",
        "Failed to update email.", error);
  }
});

exports.runEventDraws = onSchedule({
  schedule: "every 1 minutes",
  timeZone: "America/Edmonton",
}, async (context) => {
  const now = admin.firestore.Timestamp.now();
  const db = admin.firestore();
  console.log("Running scheduled event draws at", now.toDate().toISOString());

  const eventsQuery = await db.collection("events")
      .where("End Date", "<=", now)
      .get();

  for (const eventDoc of eventsQuery.docs) {
    const eventId = eventDoc.id;
    const event = eventDoc.data();

    // check if chosen subcollection is empty
    const drawSnap = await db.collection("events")
        .doc(eventId)
        .collection("Chosen")
        .limit(1)
        .get();

    if (!drawSnap.empty) {
      console.log(`Event ${eventId} already has chosen entries. Skipping draw.`);
      continue; // Skip to the next event
    }

    console.log(`Event ${eventId}: deadline passed and no chosen → running draw`);

    await runDraw(eventId, event, db);
  }
  return null;
});

async function runDraw(eventId, event, db) {
  // Load all registrants
  const registrantsSnap = await db.collection("events")
      .doc(eventId)
      .collection("Register")
      .get();

  // get max number of entrants to event
  const eventDoc = await db.collection("events").doc(eventId).get();
  const numChosen = event["Event Size"];

  if (registrantsSnap.empty) {
    console.log(`Event ${eventId}: No registrants → cannot draw`);
    return;
  }

  let registrants = registrantsSnap.docs.map((doc) => doc.id);

  // Shuffle registrants
  registrants = shuffle(registrants);

  // Select N winners
  const winners = registrants.slice(0, numChosen);

  console.log(`Event ${eventId}: Selected winners:`, winners);

  // Firestore batch for atomic writes
  const batch = db.batch();

  for (const winner of winners) {
    const chosenRef = db.collection("events")
        .doc(eventId)
        .collection("Chosen")
        .doc(winner);

    batch.set(chosenRef, {Timestamp: admin.firestore.FieldValue.serverTimestamp()});

    // add to user's chosen events
    const userChosenRef = db.collection("users")
        .doc(winner)
        .collection("Chosen")
        .doc(eventId);
    batch.set(userChosenRef, {Timestamp: admin.firestore.FieldValue.serverTimestamp()});

    // Remove from user's registered events
    const userRegisterRef = db.collection("users")
        .doc(winner)
        .collection("Register")
        .doc(eventId);
    batch.delete(userRegisterRef);

    // Remove from event's registered
    const eventRegisterRef = db.collection("events")
        .doc(eventId)
        .collection("Register")
        .doc(winner);
    batch.delete(eventRegisterRef);

    // Send notification to winner
    const userNotifRef = db.collection("users")
        .doc(winner)
        .collection("Notifications")
        .doc();
    batch.set(userNotifRef, {
      status: "Chosen",
      eid: eventId,
      nid: userNotifRef.id,
      receipt_time: admin.firestore.FieldValue.serverTimestamp(),
    });
  }
  // Commit batch
  await batch.commit();
  console.log(`Event ${eventId}: Draw complete and database updated.`);
}

function shuffle(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
  return array;
}

