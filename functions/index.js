/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({maxInstances: 10});

const functions = require("firebase-functions");
const { onValueCreated } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");
admin.initializeApp();

// eslint-disable-next-line max-len
exports.sendInvitationNotification = onValueCreated(
    {
        ref: "/invitations/{targetUserId}/{invitationId}",
        region: "europe-west1",
        instance: "idk-a-school-project-or-smth-default-rtdb"
    },
    async (event) => {
        // 3. We now get everything from the single 'event' object
        const targetUserId = event.params.targetUserId;
        const invitationData = event.data.val();

        if (!invitationData) return null;

        const senderId = invitationData.senderId;

        try {
            // Reconstruct senderName from senderId by fetching it from the users node
            const senderSnapshot = await admin.database().ref(`/users/${senderId}`).once("value");
            const senderData = senderSnapshot.val();
            const senderName = (senderData && senderData.displayName) ? senderData.displayName : "Someone";

            // eslint-disable-next-line max-len
            const tokenSnapshot = await admin.database().ref(`/users/${targetUserId}/fcmToken`).once("value");
            const fcmToken = tokenSnapshot.val();

            if (!fcmToken) {
                console.log("No FCM token found for user: ", targetUserId);
                // We keep the invitation even if notification fails so user can see it manually
                return null;
            }

            const payload = {
                data: {
                    title: `New Invite from ${senderName}`,
                    body: `${invitationData.message} Room Code: ${invitationData.roomCode}`,
                    roomCode: String(invitationData.roomCode),
                    invitationId: String(event.params.invitationId),
                    senderId: String(senderId)
                },
                token: fcmToken,
            };

            const response = await admin.messaging().send(payload);
            console.log("Successfully sent invitation with room code:", response);

            return null;
        } catch (e) {
            console.error("Error sending notification:", e);
            return null;
        }
    }
);
