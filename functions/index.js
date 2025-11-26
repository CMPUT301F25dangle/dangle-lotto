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
