const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const db = admin.firestore();
// const now = admin.firestore.Timestamp.now();

	// send postID to all followers
	exports.sendPostID = functions.firestore.document('users/{userID}/catalog/{postID}')
	.onWrite((change, context) =>
	{
		const userID = context.params.userID;
		const postID = context.params.postID;

		// Create a reference to the followers collection
		const followersRef = db.collection(`users/${userID}/followers`);

		const catalogRef = db.doc(`users/${userID}/data/catalog`);
		
		if (!change.after.data()) {

			const snapshot = followersRef.get().then(snapshot => {
					snapshot.forEach(doc => {
						const id = doc.id;

						// delete document from followers timelines
						const userFeed = db.doc(`users/${id}/feed/${postID}`)
						.delete();

						// console.log(doc.id);
					});
				});

			// decrement catalog counter
			catalogRef.update({
				catalog : admin.firestore.FieldValue.increment(-1)
			});

		} else {

				const snapshot = followersRef.get().then(snapshot => {
					snapshot.forEach(doc => {
						const id = doc.id;
						// add to timeline
						const userFeed = db.doc(`users/${id}/feed/${postID}`)
						.set({
							timestamp : admin.firestore.FieldValue.serverTimestamp()
						});

						// console.log(doc.id);
					});
				});

				catalogRef.get().then((docSnapshot) => {
					 if (docSnapshot.exists) {
						catalogRef.update({
							catalog : admin.firestore.FieldValue.increment(1)
						});
					 } else {
					 	catalogRef.set({
							catalog : admin.firestore.FieldValue.increment(1)
						});
					 }
				});
		}

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

		
		return null;
	});

	// comment counter for posts
	exports.commentCount = functions.firestore.document('posts/{postID}/comments/{commentID}')
	.onWrite((change, context) => {
		const commentID = context.params.commentID;
		const postID = context.params.postID;

		const commentRef = db.doc(`posts/${postID}`);
		if (!change.after.data()) 
		{
			commentRef.update({
				comments : admin.firestore.FieldValue.increment(-1)
			});
			
			// console.log('A comment has been deleted :' , commentID);
		
		} else {
			commentRef.update({
				comments : admin.firestore.FieldValue.increment(1)
			});

			// console.log('A comment has been added :' , commentID);
		}
		
		return null;
	});

	// like counter for posts
	exports.likeCount = functions.firestore.document('posts/{postID}/likes/{userID}')
	.onWrite((change, context) => {
		const userID = context.params.userID;
		const postID = context.params.postID;

		const postRef = db.doc(`posts/${postID}`);
		if (!change.after.data()) 
		{
			postRef.update({
				likes : admin.firestore.FieldValue.increment(-1)
			});
			
			// console.log('User unliked :' , userID);
		} else {
			postRef.update({
				likes : admin.firestore.FieldValue.increment(1)
			});
			// console.log('User liked :' , userID);
		}
		return null;
	});

	// followers counter for posts
	exports.followerCount = functions.firestore.document('users/{userID}/followers/{followerID}')
	.onWrite((change, context) => {
		const userID = context.params.userID;
		const followerID = context.params.followerID;

		const followersRef = db.doc(`users/${userID}/data/followers`);
		if (!change.after.data()) 
		{
			followersRef.update({
				followers : admin.firestore.FieldValue.increment(-1)
			});
			
			// console.log('New unfollower :' , followerID);
			
		} else {
			followersRef.get().then((docSnapshot) => {
				 if (docSnapshot.exists) {
					followersRef.update({
						followers : admin.firestore.FieldValue.increment(1)
					});
				 } else {
				 	followersRef.set({
						followers : admin.firestore.FieldValue.increment(1)
					});
				 }
			});
			
			
			// console.log('New follower :' , followerID);
			
		}
		return null;
	});

	// add posts to timeline
	exports.addFeedPosts = functions.firestore.document('users/{userID}/suggestions/{followingID}')
	.onWrite((change, context) => {
		const followingID = context.params.followingID;
		const userID = context.params.userID;

		// snap users timeline posts
		const feedRef = db.collection(`users/${userID}/feed`).get().then(snap => {
			size = snap.size
			// console.log('Posts :' , size);
			
			if (size < 15) {
				// console.log('Size is < 15');

				// get all users following
				const followingRef = db.collection(`users/${userID}/following`).get().then(snap => {
					
					// each user
					snap.forEach(doc => {
					const id = doc.id;

						// get most recent posts (2) from users catalog
						const catalogRef = db.collection(`users/${id}/catalog`).orderBy('timestamp', 'desc').limit(5);
						
						catalogRef.get().then(snapshot => {
							
							// for each post
							snapshot.forEach(doc => {
								const postID = doc.id;
								const timestamp = doc.data().timestamp;

								// set post id to user timeline
								const userFeed = db.doc(`users/${userID}/feed/${postID}`)
								.set({
									timestamp : `${timestamp}`
								});

								// console.log('Added: ', postID);
							});
						});
					});
				});
			}
		});
		return null;
	});