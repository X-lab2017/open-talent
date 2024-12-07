import cellsLeft from './cells-left-1892x1032.svg';
import cellsRight from './cells-right-1892x1032.svg';
import styles from './index.module.css';
import React from "react";
export const Background = () => {

  return (
    <div className={styles.bg}>
      {
      <>
        <img src={cellsLeft} alt="" className={styles.cellsLeft} />
        <img src={cellsRight} alt="" className={styles.cellsRight} />
      </>
      }
    </div>
  )
}