const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.newDelivery = functions.database.ref('/deliveries/{uid}').onWrite(event => {
    const snapshot = event.data;

    if(snapshot.previous.val()) {
        return;
    }

    const payload = {
        notification: {
            title: 'Delivery created',
            body: 'A delivery has been created'
        }
    };

    return admin.database().ref('receivers').once('value').then(allTokens => {
        if (allTokens.val()) {
            const tokens = Object.keys(allTokens.val());

            return admin.messaging().sendToDevice(tokens, payload).then(response => {
                const tokensToRemove = [];
                response.results.forEach((result, index) => {
                    const error = result.error;

                    if(error) {
                        console.error('Failure sending notifacations to ', tokens[index], error);

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
