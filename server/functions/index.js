// // Start writing Firebase Functions
// // https://firebase.google.com/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// })

//Import the firebase SDK for Google Cloud Functions.
const functions = require('firebase-functions');
//Import and initialize the Firebase Admin SDK.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);



//Sends a notification to all users when a new message is posted.
exports.sendNotifications = functions.database.ref('/User messages/{userID}/{messageID}').onWrite(event => {
    const snapshot = event.data;

    if (!snapshot.exists()) return;
    if (snapshot.child('removed').val()) return;

        console.log("Hola4");


notificationData = {};
notificationData["commerce_name"] = snapshot.child("commerce_name").val();
notificationData["commerce_uid"] = snapshot.child("commerce_uid").val();
//notificationData["message_uid"] = snapshot.child("message_uid").val();
notificationData["removed"] = snapshot.child("removed").val().toString();
notificationData["title"] = snapshot.child("title").val();
notificationData["description"] = snapshot.child("description").val();
notificationData["used"] = snapshot.child("used").val().toString();
notificationData["description"] = snapshot.child("description").val();

     const payload = {
      data: notificationData
    };



        console.log("Hola5");


    const getToken = admin.database().ref(`/Users/${event.params.userID}/token`).once('value');

    return Promise.all([getToken]).then(results => {

    console.log("Hola6");

        const token = results[0];

        console.log(`Token is: ${token.val()}`);

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
                        if(error.code === 'messaging/invalid-registration-token' ||
                        error.code === 'messaging/registration-token-not-registered'){
                            tokensToRemove.push(allTokens.ref.child(tokens[index]).remove());
                        }
                    }
                });
                return Promise.all(tokensToRemove);
            });
        }
    });
});
