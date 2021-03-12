const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const db = admin.firestore();
// const now = admin.firestore.Timestamp.now();

	exports.sendPostID = functions.firestore.document('users/{userID}/catalog/{postID}')
	.onCreate((data, context) =>
	{
		const userID = context.params.userID;
		const postID = context.params.postID;

		/*

		console.log('user:' , userID + ' created post ',postID);

		var data = {
		      status: 'status'  //Here, if necessary, you could use the value of st1, i.e. the new status
		    };

		let followers = s

		const db = admin.firestore().doc(`users/${userID}/followers/${postID}`)
		.update({
			status: `${userID}`
		});

		*/

		// Create a reference to the followers collection
		const followersRef = db.collection(`users/${userID}/followers`);
			const snapshot = followersRef.get().then(snapshot => {
				snapshot.forEach(doc => {
					const id = doc.id;

					const userTimeline = db.doc(`users/${id}/timeline/${postID}`)
					.set({
						timestamp : admin.firestore.FieldValue.serverTimestamp()
					});

					// console.log(doc.id);
				});
			});
		
		return null;
	});

	exports.commentCount = functions.firestore.document('posts/{postID}/comments/{commentID}')
	.onWrite((change, context) => {
		const commentID = context.params.commentID;
		const postID = context.params.postID;

		if (!change.after.data()) 
		{
			const userRef = db.doc(`posts/${postID}`)
			.update({
				comments : admin.firestore.FieldValue.increment(-1)
			});
			
			// console.log('A comment has been deleted :' , commentID);
			return null;
		
		} else {
			const userRef = db.doc(`posts/${postID}`)
			.update({
				comments : admin.firestore.FieldValue.increment(1)
			});

			// console.log('A comment has been added :' , commentID);
			return null;
		}
		

		return null;
	});
