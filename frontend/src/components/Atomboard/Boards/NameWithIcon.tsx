import styles from './index.module.css';
import React from "react";

interface NameWithIconProps {
  icon: string;
  name: string;
  rounded?: boolean;
}

export const NameWithIcon = ({icon, name, rounded }: NameWithIconProps) => {
  return (
    <div className={styles.nameContainer}>
    <img src={icon} alt="" className={`${styles.icon} ${rounded ? styles.iconRounded : ''}`} />
    <span className={styles.ellipsis}>{name}</span>
  </div>
  )
}
