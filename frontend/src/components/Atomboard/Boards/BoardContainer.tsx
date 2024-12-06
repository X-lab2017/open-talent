import earthAnimation from './earth-animation.gif';
import React from "react";

import { ReactNode } from 'react';

import styles from './index.module.css';

interface BoardContainerProps {
  children?: ReactNode;
  top: string;
  left: string;
  title: string;
  style?: React.CSSProperties;
  className?: string;
}

export const BoardContainer = ({ children, top, left, title, style={}, className='' }: BoardContainerProps) => {

  return (
    <div
      className={`${styles.boardContainer} ${styles[className]}`}
      style={{ top, left, ...style }}
    >
      <div className={styles.boardTitle}>
        <img src={earthAnimation} alt="" className={styles.boardTitleIcon}/>
        <span className={styles.boardTitleText}>{title}</span>
      </div>
      {children}
    </div>
  )
  
}
