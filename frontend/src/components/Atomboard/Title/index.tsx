import title from './title-1892x1032.png';
import laser from './laser-1892x1032.png';
import React from "react";

import styles from './index.module.css';

export const Title = () => {

  return (
    <>
      <img src={title} alt="" className={styles.title} />
      <img src={laser} alt="" className={styles.laser} />
    </>
  )
}
