import { formatNumberWithCommas } from '../utils/format.ts';
import React from "react";

import styles from './index.module.css';

export interface StatsItemProps {
  icon: string;
  text: string;
  value: number;
}

export const StatsItem = ({icon, text, value}: StatsItemProps) => {
  return (
    <div className={styles.statsItem}>
      <img src={icon} alt="" className={styles.statsIcon} />
      <div className={styles.statsItemContent}>
        <span className={styles.statsItemText}>{text}</span>
        <span className={styles.statsItemNumber}>{formatNumberWithCommas(value)}</span>
      </div>
    </div>
  );
}
