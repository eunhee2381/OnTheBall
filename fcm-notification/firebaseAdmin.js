const admin = require('firebase-admin');
const express = require('express');
const app = express();
const port = 3000;

app.use(express.json());
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

async function getTokenByEmail(email) {
  const snapshot = await db.collection('User').where('email', '==', email).get();
  if (snapshot.empty) {
    throw new Error('입력한 이메일로 등록된 사용자가 없습니다: ' + email);
  }
  const doc = snapshot.docs[0];
  return doc.data().token;
}

async function sendNotification(email, title, body) {
    const token = await getTokenByEmail(email);
    if (!token) return;

    const message = {
        notification: {
            title: title,
            body: body
        },
        data: {
            userName: userEmail, // 사용자 이메일을 예시로 사용
            productName: productName
        },
        token: token
    };

    try {
        await admin.messaging().send(message);
        console.log('알림이 성공적으로 전송되었습니다:', email);
    } catch (error) {
        console.error('알림 전송 실패:', error);
    }
}

app.post('/api/sendRentalRequest', async (req, res) => {
  const { productName, userEmail } = req.body;
  const title = "대여 요청";
  const message = `${userEmail}님이 ${productName}을 대여 요청하였습니다.`;

  sendNotification(userEmail, title, message).then(() => {
    res.status(200).send('알림이 성공적으로 전송되었습니다');
  }).catch(error => {
    res.status(500).send('알림 전송 실패');
  });
});

app.listen(port, () => {
  console.log(`서버가 ${port}포트에서 실행 중입니다.`);
});
