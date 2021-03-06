// Import Firebase SDK for Google Cloud Functions
const functions = require('firebase-functions');

// Import and initialize Firebase Admin SDK
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

//Sends a notification to user when a new delivery is created.
exports.newDelivery = functions.database.ref('/receiver_deliveries/{receiver_uid}/{delivery_uid}').onWrite(event => {
    const snapshot = event.data;

    if (snapshot.child('state').val() === "Delivered") {
        return;
    }

    if (!snapshot.exists()) {
        return;
    }

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

exports.parcelDelivered = functions.database.ref('/receiver_deliveries/{receiver_uid}/{delivery_uid}/').onWrite(event => {
    const snapshot = event.data;

    if (!snapshot.exists()) {
        return;
    }

    if (snapshot.child('state').val() === "Created") {
        return;
    }

    const payload = {
        notification: {
            title: 'Parcel delivered',
            body: 'Your parcel has been succesfully delivered'
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
