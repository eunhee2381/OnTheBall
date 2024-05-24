const admin = require('firebase-admin');

// Firebase Admin SDK 초기화
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// 사용자 이메일로부터 UID 조회
function getUidByEmail(email) {
  return admin.auth().getUserByEmail(email)
    .then(userRecord => {
      return userRecord.uid;  // UID 반환
    })
    .catch(error => {
      console.error('Error fetching user data:', error);
      throw error;
    });
}

// uid를 이용하여 토큰 값 가져오기
//해당 이메일이 있는 컬렉션에서 토큰 값을 가져옴

function getTokenByUid(uid) {
  return db.collection('User').doc(uid).get()
    .then(doc => {
      if (doc.exists) {
        return doc.data().token;  // 'token' 필드의 값을 반환
      } else {
        throw new Error('No user found with UID ' + uid);
      }
    });
}

/**
    메세지를 사용자의 messages 서브 컬렉션에 저장하는 함수
*/
function saveMessageToCurrentUser(title, message) {
  const auth = firebase.auth();
  const db = firebase.firestore();

  // 현재 로그인된 사용자의 이메일을 가져옵니다.
  const currentUserEmail = auth.currentUser?.email;
  if (!currentUserEmail) {
    return Promise.reject('로그인된 사용자가 없습니다.');
  }

  // 이메일을 사용하여 User 컬렉션에서 해당 문서를 찾습니다.
  return db.collection('User').where('email', '==', currentUserEmail).get()
    .then(snapshot => {
      if (snapshot.empty) {
        return Promise.reject('해당 이메일로 등록된 사용자를 찾을 수 없습니다.');
      }

      // 문서가 존재하면 해당 사용자의 messages 서브 컬렉션에 접근합니다.
      const userDoc = snapshot.docs[0];
      const userMessagesRef = userDoc.ref.collection('messages');

      // messages 컬렉션에 새로운 메시지를 추가합니다.
      return userMessagesRef.add({
        title: title,
        message: message,
        timestamp: firebase.firestore.FieldValue.serverTimestamp()
      });
    })
    .catch(error => {
      console.error('메세지 저장 중 오류가 발생했습니다:', error);
      return Promise.reject(error);
    });
}



/**
    위의 함수들과 메세지를 통함하여 만든 함수
   ******** 프론트 분들은 이것만 사용하시면 됩니다 ********

    // email -> 토큰 값을 가져올 계정의 이메일 (알람 받을 계정)
    // tl -> 알람 제목(title)
    // ms -> 알람 내용(message)

    // 사용 예
    //sendNotification('user@example.com');

    //알람을 저장하는 함수도 통합
*/

function sendNotification(email, tl, ms) {

  getUidByEmail(email)
    .then(uid => {
      // 토큰을 가져와서 메시지를 보내고, 메시지를 데이터베이스에 저장
      return Promise.all([
        getTokenByUid(uid),
        saveMessageToCurrentUser(tl, ms)
      ])
      .then(([token]) => {
        const message = {
          notification: {
            title: notificationTitle,
            body: notificationBody
          },
          token: token
        };

        return admin.messaging().send(message);
      });
    })
    .then(response => {
      console.log('Successfully sent message:', response);
    })
    .catch(error => {
      console.error('Error sending message or saving to database:', error);
    });
}

