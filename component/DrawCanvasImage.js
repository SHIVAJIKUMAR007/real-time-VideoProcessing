import React, {useEffect, useRef} from 'react';
import Canvas, {Image as CanvasImage} from 'react-native-canvas';

const DrawCanvasImage = ({imageUrl}) => {
  let canvas = useRef(null);
  let canvasContext = useRef(null);

  async function drawImage(imgUrl) {
    console.log('in drawimage ');
    //if (!canvas.current || !canvasContext.current) return;

    if (canvas.current) {
      const img = new CanvasImage(canvas.current, 300, 400);
      // let idx = Math.floor(Math.random() * 9);
      // let imageStr = images[idx];
      // console.log(idx, imageStr, 'in drawimage 222222222222222');
      img.src =
        'https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg';
      //  imgUrl;
      img.addEventListener('load', () => {
        console.log('image is loaded');
        canvasContext.current.drawImage(img, 0, 0, 200, 400);
      });
    }
  }

  useEffect(() => {
    if (imageUrl) drawImage(imageUrl);
    setInterval(() => {
      if (canvas) {
        let stream = canvas.captureStream(24);
        console.log(stream);
      }
    }, 1000);
  }, [imageUrl]);
  const handleCanvas = can => {
    if (can) {
      can.width = 300;
      can.height = 400;
      const ctx = can.getContext('2d');

      canvas.current = can;
      canvasContext.current = ctx;
    }
  };
  return <Canvas ref={handleCanvas} style={{width: 400, height: 400}} />;
};

export default DrawCanvasImage;
