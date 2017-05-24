// Import Firebase SDK for Google Cloud Functions
const functions = require('firebase-functions');

// Import and initialize Firebase Admin SDK
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

//Sends a notification to user when a new delivery is created.
exports.newDelivery = functions.database.ref('/receiver_deliveries/{receiver_uid}/{delivery_uid}').onWrite(event => {
    const snapshot = event.data;

    if (!snapshot.exists()) {
        return;
    }

    // notificationData = {};
    // notificationData["commerce_name"] = snapshot.child("commerce_name").val();
    // notificationData["commerce_uid"] = snapshot.child("commerce_uid").val();
    // notificationData["removed"] = snapshot.child("removed").val().toString();
    // notificationData["title"] = snapshot.child("title").val();
    // notificationData["description"] = snapshot.child("description").val();
    // notificationData["used"] = snapshot.child("used").val().toString();
    // notificationData["description"] = snapshot.child("description").val();

    const payload = {
        notification: {
            title: 'Delivery created',
            body: 'A delivery has been created'
        }
    };

    const getToken = admin.database().ref(`/receivers/${event.params.receiver_uid}/token`).once('value');

    return Promise.all([getToken]).then(results => {
        const token = results[0];
        if(token.val()){
            const tokens = token.val();
            //Send notifications to all tokens.
            return admin.messaging().sendToDevice(tokens, payload).then(response =>{
                //For each message check if there was an error.
                const tokensToRemove = [];
                response.results.forEach((result, index) => {
                    const error = result.error;
                    if(error){
                        console.error('Failure sending notification to', tokens[index],error);
                        //Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                        error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(allTokens.ref.child(tokens[index]).remove());
                        }
                    }
                });
                return Promise.all(tokensToRemove);
            });
        }
    });
});
