const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.deleteUserAuth = functions.https.onCall(async (data, context) => {
  const uid = data.uid;

  try {
    await admin.auth().deleteUser(uid);
    return {success: true};
  } catch (err) {
    console.error(err);
    throw new functions.https.HttpsError("unknown", "Failed to delete user");
  }
});
