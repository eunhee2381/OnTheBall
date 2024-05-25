const admin = require('firebase-admin');
const firebase = require('firebase/app');
require('firebase/auth');
require('firebase/firestore');

// Firebase Admin SDK를 초기화합니다.
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

/**
 * 주어진 이메일로부터 해당하는 사용자의 토큰 값을 직접 조회합니다.
 */
function getTokenByEmail(email) {
  return db.collection('User').where('email', '==', email).get()
    .then(snapshot => {
      if (snapshot.empty) {
        throw new Error('입력한 이메일로 등록된 사용자가 없습니다: ' + email);
      }
      const doc = snapshot.docs[0];
      return doc.data().token;  // 해당 문서에서 토큰 필드의 값을 반환합니다.
    });
}

/**
 * 이메일 주소를 기반으로 사용자를 찾고 해당 사용자의 'messages' 컬렉션에 메시지를 저장합니다.
 *
 * @param {string} userEmail - 메시지를 저장할 사용자의 이메일 주소
 * @param {string} title - 메시지의 제목
 * @param {string} message - 메시지의 본문 내용
 */
function saveMessageByEmail(userEmail, title, message) {
  // User 컬렉션에서 주어진 이메일과 일치하는 문서를 조회합니다.
  db.collection('User').where('email', '==', userEmail).get()
    .then(snapshot => {
      if (snapshot.empty) {
        console.log('해당 이메일로 등록된 사용자를 찾을 수 없습니다:', userEmail);
        return;
      }

      // 사용자 문서가 존재하면 해당 문서의 ID를 가져와서 'messages' 서브 컬렉션에 접근합니다.
      const userDoc = snapshot.docs[0];
      const messagesRef = userDoc.ref.collection('messages');

      // 'messages' 컬렉션에 새로운 메시지를 추가합니다.
      return messagesRef.add({
        title: title,
        message: message,
        timestamp: admin.firestore.FieldValue.serverTimestamp() // 메시지가 저장된 시간을 기록합니다.
      });
    })
    .then(() => {
      console.log('메시지가 성공적으로 저장되었습니다.');
    })
    .catch(error => {
      console.error('메시지 저장 중 오류가 발생했습니다:', error);
    });
}




/**
    위의 함수들을 통합하여 만든 함수입니다.
    프론트엔드 개발자는 이 함수만 사용하시면 됩니다.

    // email -> 알림을 받을 계정의 이메일
    // tl -> 알림 제목
    // ms -> 알림 내용

    // 사용 예
    //sendNotification('user@example.com');

    // 알람을 저장하는 기능도 포함되어 있습니다.
*/

function sendNotification(userEmail, title, message) {
  getTokenByEmail(userEmail)
    .then(token => {
      const notificationMessage = {
        notification: {
          title: title,
          body: message
        },
        data: {
          customKey1: 'customValue1',
          customKey2: 'customValue2'
        },
        token: token
      };

      // 메시지 전송과 메시지 저장을 동시에 실행합니다.
      return Promise.all([
        admin.messaging().send(notificationMessage),
        saveMessageByEmail(userEmail, title, message)
      ]);
    })
    .then(([sendResponse, saveResponse]) => {
      console.log('성공적으로 메시지가 전송되고 사용자 메시지에 저장되었습니다:', sendResponse);
    })
    .catch(error => {
      console.error('메시지 전송 또는 저장 중 오류가 발생했습니다:', error);
    });
}
