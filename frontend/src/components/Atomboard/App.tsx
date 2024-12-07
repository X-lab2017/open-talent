import { Background } from './Background/index.tsx'
import { Border } from './Border/index.tsx'
import { Footer } from './Footer/index.tsx'
import { Title } from './Title/index.tsx'
import { Stats } from './Stats/index.tsx'
import { Boards } from './Boards/index.tsx'
import {MapBoard} from "./Map/index.tsx"
import { useEffect } from 'react'
import React from 'react'
import backIcon from './backIcon.svg'
import './App.css'

function Atomboard() {
  useEffect(() => {
    const handleResize = () => {
      const designSize = { width: 1892, height: 1032 };
      const scaleX = window.innerWidth / designSize.width;
      const scaleY = window.innerHeight / designSize.height;

      const body = document.body;
      body.style.transform = `scale(${scaleX}, ${scaleY})`;
      body.style.transformOrigin = 'top left';  // 以左上角为缩放原点
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);


  return (
      <div>
        <Background />
        <Border />
        <Footer />
        <Title />
        <MapBoard />
        <Stats />
        <Boards />
      </div>

  )
}

export default Atomboard
