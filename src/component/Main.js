import {View, Text} from 'react-native';
import React, {useState, useEffect, useRef} from 'react';

const configuration = {
  iceServers: [{url: 'stun:stun.l.google.com:19302'}],
};

const Main = () => {
  const [localStream, setlocalStream] = useState(null);
  const [remoteStream, setremoteStream] = useState(null);
  const [gettingCall, setgettingCall] = useState(false);
  const pc = useRef(); //RTCPeerConnection
  const connecting = useRef(false);

  useEffect(() => {
    const cRef = firestore().collection('meet').doc('chatId');
    const subscribe = cRef.onSnapshot(snapshot => {
      const data = snapshot.data();

      if (pc.current && !pc.current.remoteDescription && data && data.answer) {
        pc.current.setRemoteDescription(new RTCSessionDescription(data.answer));
      }

      if (data && data.offer && !connecting.current) {
        setgettingCall(true);
      }
    });

    const subscribeDelete = cRef.collection('callee').onSnapshot(snapshot => {
      snapshot.docChanges().forEach(change => {
        if (change.type == 'removed') {
          hangup();
        }
      });
    });

    return () => {
      subscribe();
      subscribeDelete();
    };
  }, []);

  const setupWebrtc = async () => {
    pc.current = new RTCPeerConnection(configuration);

    const stream = await Utils.getStream();

    if (stream) {
      setlocalStream(stream);
      pc.current.addStream(stream);
    }

    pc.current.onaddstream = event => {
      setremoteStream(event.stream);
    };
  };
  const create = async () => {
    console.log('calling');
    connecting.current = true;

    //setup webrtc
    await setupWebrtc();

    //document the call
    const cRef = firestore().collection('meet').doc('chatId');

    //exchange the ICE candidate b/w the caller and reciever
    collectionIceCandidate(cRef, 'caller', 'callee');

    if (pc.current) {
      //create the offer for the call
      //store the offer under the document
      const offer = await pc.current.createOffer();
      pc.current.setLocalDescription(offer);

      const cWithOffer = {
        offer: {
          type: offer.type,
          sdp: offer.sdp,
        },
      };

      cRef.set(cWithOffer);
    }
  };
  const join = async () => {
    console.log('join the call');
    connecting.current = true;
    setgettingCall(false);

    const cRef = firestore().collection('meet').doc('chatId');
    const offer = (await cRef.get()).data()?.offer;

    if (offer) {
      //setup webrtc
      await setupWebrtc();

      //exchange of ice candidate

      //check the parameters, its reversed. since the joining part is callee
      collectionIceCandidate(cRef, 'callee', 'caller');

      //create the canswer and update the document
      const answer = await pc.current.createAnswer();
      pc.current.setLocalDescription(answer);
      const cWithAnswer = {
        answer: {
          type: answer.type,
          sdp: answer.sdp,
        },
      };

      cRef.update(cWithAnswer);
    }
  };
  const hangup = async () => {
    setgettingCall(false);
    connecting.current = false;
    streamCleanUp();
    firestoreCleanUP();
    if (pc.current) {
      pc.current.close();
    }
  };

  //helper function
  const streamCleanUp = async () => {
    if (localStream) {
      localStream.getTracks().forEach(t => t.stop());
      localStream.release();
    }
    setlocalStream(null);
    setremoteStream(null);
  };

  const firestoreCleanUP = async () => {
    const cRef = firestore().collection('meet').doc('chatId');

    if (cRef) {
      const calleeCandidate = await cRef.collection('callee').get();
      calleeCandidate.forEach(async can => {
        await can.ref.delete();
      });

      const callerCandidate = await cRef.collection('caller').get();
      callerCandidate.forEach(async can => {
        await can.ref.delete();
      });

      cRef.delete();
    }
  };

  const collectionIceCandidate = async (cRef, localName, remoteName) => {
    const candidateCollection = cRef.collection(localName);

    if (pc.current) {
      // on ice candidate add it to firebase
      pc.current.onicecandidate = event => {
        if (event.candidate) {
          candidateCollection.add(event.candidate);
        }
      };
    }

    // get the ice candidate added to firestore and update the local pc
    cRef.collection(remoteName).onSnapshot(snapshot => {
      snapshot.docChanges().forEach(change => {
        if (change.type == 'added') {
          const candidate = new RTCIceCandidate(change.doc.data());
          pc.current?.addIceCandidate(candidate);
        }
      });
    });
  };

  // if call is incoming and not joined yet
  if (gettingCall) {
    return <GettingCall hangup={hangup} pickup={join} />;
  }

  // if you sent call and other not joined yet
  // if other joied
  if (localStream) {
    return (
      <Video
        hangup={hangup}
        localStream={localStream}
        remoteStream={remoteStream}
      />
    );
  }

  return (
    <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
      <Button title="Create call" onPress={create} />
    </View>
  );
  ////////////////////////////////////////////////////////////////////////////
};

export default Main;
