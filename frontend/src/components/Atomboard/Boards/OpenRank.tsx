import newTag from './new.svg';
import increaseTag from './increase.svg';
import decreaseTag from './decrease.svg';
import React from "react";

import styles from './index.module.css';

interface OpenRankProps {
  delta: number | '-';
  openrank: number;
}

export const OpenRank = ({ delta, openrank }: OpenRankProps) => {
  if (delta === '-') {
    return (
      <div className={styles.openrankContainer}>
        <span className={styles.openrank}>{openrank.toFixed(2)}</span>
        <img className={styles.tag} src={newTag} alt='new' />
      </div>
    );
  }
  return (
    <div className={styles.openrankContainer}>
      <span>{openrank.toFixed(2)}</span>
      <img className={styles.tag} src={delta > 0 ? increaseTag : decreaseTag} alt='increase/decrease' />
      <span>{Math.abs(delta).toFixed(2)}</span>
    </div>
  );
}