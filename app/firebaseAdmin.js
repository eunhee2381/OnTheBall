const admin = require('firebase-admin');
const express = require('express');
const app = express();

app.use(express.json());

const serviceAccount = require('C:\\boogie\\app\\boogie-1af95-firebase-adminsdk-vlwjd-ea92d6a6ae.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// 메시지 전송 함수
async function sendMessage(userEmail, productName) {
  const message = {
    notification: {
      title: 'New Product Alert',
      body: `Hey ${userEmail}, check out our new product: ${productName}!`
    },
    topic: 'adminUsers'
  };

  try {
    await admin.messaging().send(message);
    console.log('Message sent successfully');
  } catch (error) {
    console.error('Error sending message:', error);
  }
}

app.listen(3000, () => {
  console.log('Server is running on port 3000');
});
