const admin = require('firebase-admin');

// Firebase Admin SDK 초기화
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// 메시지 데이터
const message = {
  notification: {
    title: '기자재 예약 알림',
    body: '기자재 예약이 정상적으로 완료되었습니다.'
  },
  data: {
    customKey1: 'customValue1',
    customKey2: 'customValue2'
  },
  token: '토큰 입력해야함'
};

// 메시지 전송 함수
admin.messaging().send(message)
  .then(response => {
    console.log('Successfully sent message:', response);
  })
  .catch(error => {
    console.error('Error sending message:', error);
  });
