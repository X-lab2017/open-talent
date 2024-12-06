import border from './border-1892x1032.svg';
import { Content } from './Content.tsx';
import styles from './index.module.css';
import React from "react";

export const Footer = () => {
  return (
    <>
      <img src={border} alt="" className={styles.border} />
      <Content />
    </>
  )
}
