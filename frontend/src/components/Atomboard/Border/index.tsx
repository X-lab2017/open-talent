import border from './border-1892x1032.svg';
import arrowAnimation from './arrow-animation.gif'
import React from "react";


import styles from './index.module.css';

export const Border = () => {
  return (
    <>
      <img src={border} alt="" className={styles.border} />
      <img src={arrowAnimation} alt="" className={styles.arrowAnimationLeft} />
      <img src={arrowAnimation} alt="" className={styles.arrowAnimationRight} />
    </>
  )
}
