const nodemailer = require('nodemailer');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const db = admin.firestore();
// const now = admin.firestore.Timestamp.now();
const APP_NAME = 'Shopline';
var transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'shoplineuganda@gmail.com',
        pass: 'Shopline12?'
    }
});

var d = new Date();
var date = d.getDate() + '-' + (d.getMonth() + 1) + '-' + d.getFullYear(); // date format (dd-MM-yyyy)
// console.log('Date: ' , date);

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

		} else if (change.after.data()) {
				// get username
				const ruserID = change.after.data().ruserID; 
				const userRef = db.doc(`users/${userID}`).get().then(doc => {
					username = doc.data().username;
					
					// send notification to subscribers (Followers)
					var topic = userID; // set topic 
					var message = {
						notification: {
							title: `${APP_NAME}`,
							body: `${username} posted a new product.`,
						},
						data: {
							postID: postID
						},
						topic: topic
					};
					admin.messaging().send(message);

					/*.then(response => {
						console.log('Successful');
					});*/
				});
				// send post to followers
				const snapshot = followersRef.get().then(snapshot => {
					snapshot.forEach(doc => {
						const id = doc.id;
						const userFeed = db.doc(`users/${id}/feed/${postID}`);
						// check if repostID is null 
						if (ruserID != null) {
							// add to follower feed
							userFeed.set({
								timestamp : admin.firestore.FieldValue.serverTimestamp(),
								ruserID : ruserID
							});
						} else {
							userFeed.set({
								timestamp : admin.firestore.FieldValue.serverTimestamp()
							});
						}
						// console.log(doc.id);
					});
				});
				// add increment to user catalog
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
			// get userID from post
			const postRef = db.doc(`posts/${postID}`).get().then(doc => {
				PostUserID = doc.data().userID;
				const userID = change.after.data().userID; // get value from comment snapshot
				// push to notification ref
				if (PostUserID != userID) {
					const userRef = db.collection(`users/${PostUserID}/notifications`);
					userRef.add({
						timestamp : admin.firestore.FieldValue.serverTimestamp(),
						postID : postID,
						state: 2, // COMMENT
						userID: userID
					});	

					// comments analytics
					const userAnalyticsRef = db.doc(`analytics/${PostUserID}/comments/${date}`);
					// setting analytics
					userAnalyticsRef.set({
						timestamp : admin.firestore.FieldValue.serverTimestamp(),
						count: admin.firestore.FieldValue.increment(1),
					}, { merge: true });
				}
				// console.log('Succesful');
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
			// push to notification ref
			postRef.get().then(doc => {
				PostUserID = doc.data().userID;
				if (PostUserID != userID) {
					const userRef = db.collection(`users/${PostUserID}/notifications`);
					userRef.add({
						timestamp : admin.firestore.FieldValue.serverTimestamp(),
						postID : postID,
						state: 1,// LIKE
						userID: userID
					});	

				// likes analytics
				const userAnalyticsRef = db.doc(`analytics/${PostUserID}/likes/${date}`);
					// setting analytics
					userAnalyticsRef.set({
						timestamp : admin.firestore.FieldValue.serverTimestamp(),
						count: admin.firestore.FieldValue.increment(1),
					}, { merge: true });
				}
				/*// send notification to subscribers (Followers)
					var topic = PostUserID + '_notifications'; 
					console.log(topic)
					var message = {
						notification: {
							title: `${APP_NAME}`,
							body: `${customerName}, placed an new order.`,
							click_action: 'Orders'
						},
						
						topic: topic
					};
					admin.messaging().sendToTopic(message);*/
				// console.log('Succesful');	
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

		const userAnalyticsRef = db.doc(`analytics/${userID}/followers/${date}`);

		if (!change.after.data()) 
		{
			followersRef.update({
				followers : admin.firestore.FieldValue.increment(-1)
			});
			
			// console.log('New unfollower :' , followerID);

			// decrement analytics
			userAnalyticsRef.update({
				count: admin.firestore.FieldValue.increment(-1),
			});
			
		} else {
			
			// setting analytics
			userAnalyticsRef.set({
				timestamp : admin.firestore.FieldValue.serverTimestamp(),
				count: admin.firestore.FieldValue.increment(1),
			}, { merge: true });

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

				 // push to notification reference
				const userRef = db.doc(`users/${userID}/notifications/${followerID}`);
					userRef.set({
						timestamp : admin.firestore.FieldValue.serverTimestamp(),
						state: 3, // FOLLOW
						userID: followerID
				});	
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
									timestamp : timestamp
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

	// send emails of order to buyer and seller
	exports.sendEmail = functions.firestore.document('users/{userID}/orders_customer/{orderId}')
    .onCreate((snap, context) => {
    	const userID = context.params.userID;
    	const postID = snap.data().postID;
    	const customerID = snap.data().userID;
    	// get post product name
    	const postRef = db.doc(`posts/${postID}`).get().then(doc => {
    		product = doc.data().product;
    		// get customer's name and email
    		const customerRef = db.doc(`users/${customerID}`).get().then(doc => {
    			customerName = doc.data().username;
    			customerEmail = doc.data().email;

    			// send notification to subscribers (Followers)
					var topic = userID + '_notifications'; 
					// console.log(topic)
					var message = {
						notification: {
							title: `${APP_NAME}`,
							body: `${customerName}, placed an new order.`,
							
						},
						data: {
							click_action: 'Orders'
						},
						topic: topic
					};
					admin.messaging().send(message);

    			// get buyer's name and email
		    	const userRef = db.doc(`users/${userID}`);
		    	return userRef.get().then(doc => {
		    		email = doc.data().email;
		    		username = doc.data().username;
			    	// create email
					const mailSeller = {
					    from: `${APP_NAME} <noreply@firebase.com>`,
					    to: email,
					    subject: `${username}, You have a New Order!`,
					    html: `<h1>${APP_NAME}</h1>
						    
						    <p>Hi ${username}, You have a new order from a customer. Here are the details.</p>
						    
						    <h2>Order Details</h2>
						    <p> <b>Product: </b>${product} </p>
						    <p> <b>Quantity: </b>${snap.data().quantity} </p>
						    <br>
						    <h2>Customer Details</h2>
						    <p> <b>Username: </b>${customerName} </p>
						    <p> <b>Email: </b>${customerEmail} </p>
						    <p> <b>Deliever to: </b>${snap.data().location} </p>
						    <br>
						    <p> Thank you for using <b>${APP_NAME}.</b></p>`
					};
				
					const mailBuyer = {
					    from: `${APP_NAME} <noreply@firebase.com>`,
					    to: customerEmail,
					    subject: `${customerName}, You have placed an Order!`,
					    html: `<h1>${APP_NAME}</h1>
						    
						    <p>Hi ${customerName}, You have placed a new order. Here are the details.</p>
						    
						    <h2>Order Details</h2>
						    <p> <b>Product: </b>${product} </p>
						    <p> <b>Quantity: </b>${snap.data().quantity} </p>
						    <p> <b>Deliever to: </b>${snap.data().location} </p>
						    <p> <b>Seller's Email: </b>${email} </p>
						    <br>
						    <p> Thank you for using <b>${APP_NAME}.</b></p>`
					};
					// send to seller
					return transporter.sendMail(mailSeller, (error, data) => {
					    if (error) {
					        console.log(error)
					        return
					    }
					    // console.log("Sent!")
					    // send email to buyer
						return transporter.sendMail(mailBuyer);
					});
					
		    	});
    		});
    	});
		// push to notification reference
		const userRef = db.collection(`users/${userID}/notifications`);
			userRef.add({
				timestamp : admin.firestore.FieldValue.serverTimestamp(),
				state: 4, // ORDER
				userID: customerID
		});	

		return null;
	});

    // check notifications
	exports.Notifications = functions.firestore.document('users/{userID}/notifications/{nid}')
    .onCreate((snap, context) => {
    	const userID = context.params.userID;
    	// add to notification counter
    	const notiCountRef = db.doc(`users/${userID}/data/notifications`);
    	notiCountRef.set({
    		notifications : 1
    	});

    	const notificationRef = db.collection(`users/${userID}/notifications`).get().then(snap => {
	    	// check size
	    	size = snap.size
	    	if (size >= 30) {
				// create query to get last 5 (oldest dates)
				const notificationRef = db.collection(`users/${userID}/notifications`).orderBy('timestamp').limit(10);
				notificationRef.get().then(snapshot => {
					snapshot.forEach(doc => {
						doc.ref.delete();
					});
				});
	    	}
    	});
    	
    	return null;
    });

    // re-posts
    exports.Reposts = functions.firestore.document('posts/{postID}/reposts/{userID}')
    .onWrite((change, context) => {
    	const userID = context.params.userID;
    	const postID = context.params.postID;
		const postRef = db.doc(`posts/${postID}`);
    	
    	if (!change.after.data()) 
		{
			postRef.update({
				reposts : admin.firestore.FieldValue.increment(-1)
			});
			
			// console.log('User unliked :' , userID);
		} else {

			// increment value
			postRef.update({
				reposts : admin.firestore.FieldValue.increment(1)
			});
			// push to notification reference
			postRef.get().then(doc => {
				PostUserID = doc.data().userID;
				const userRef = db.collection(`users/${PostUserID}/notifications`);
				userRef.add({
					timestamp : admin.firestore.FieldValue.serverTimestamp(),
					postID : postID,
					state: 5, // RE-POST
					userID: userID
				});
				
				// repost analytics
				const userAnalyticsRef = db.doc(`analytics/${PostUserID}/reposts/${date}`);
					// setting analytics
					userAnalyticsRef.set({
						timestamp : admin.firestore.FieldValue.serverTimestamp(),
						count: admin.firestore.FieldValue.increment(1),
					}, { merge: true });	
			});
		}
		
		return null;
    });
    
						
