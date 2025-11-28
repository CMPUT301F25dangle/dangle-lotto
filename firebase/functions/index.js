const functions = require("firebase-functions");
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
  // Ensure the user is authenticated and authorized to perform this action
  // if (!context.auth) {
  //   console.log("Unauthenticated request to update user email.");
  //   throw new functions.https.HttpsError("unauthenticated",
  //       "The function must be called while authenticated.");
  // }

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
