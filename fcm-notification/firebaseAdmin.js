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
            email: email, // 사용자 이메일을 예시로 사용
            body: body
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
  const { email, title, message } = req.body;

  if (!email || !title || !message) {
    return res.status(400).send('필요한 정보가 누락되었습니다.');
  }

  try {
    const token = await getTokenByEmail(email);
    if (!token) {
      throw new Error('사용자 토큰을 찾을 수 없습니다.');
    }

    await sendNotification(email, title, message);
    res.status(200).send('알림이 성공적으로 전송되었습니다');
  } catch (error) {
    console.error('알림 전송 실패:', error);
    res.status(500).send('알림 전송 실패: ' + error.message);
  }
});

app.listen(port, () => {
  console.log(`서버가 ${port}포트에서 실행 중입니다.`);
});
