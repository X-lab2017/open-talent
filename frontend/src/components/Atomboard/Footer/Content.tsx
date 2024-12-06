import XLabLogo from './lab-logo.png';
import OpenDiggerLogo from './open-digger-logo-1892x1032.png';
import React from "react";

import { useState, useEffect } from 'react';
import moment from 'moment';

import styles from './index.module.css';


const VerticalDivider = () => {
  return (
    <div className={styles.verticalDivider} />
  );
}

export const Content = () => {
  const [updatedAt, setUpdatedAt] = useState('YYYY.MM');

  useEffect(() => {
    const fetchData = async () => {
      const response = await fetch('https://oss.open-digger.cn/openatom/stats.json');
      const data = await response.json();
      setUpdatedAt(moment(data.updatedAt).format('YYYY.MM'));
    }
    fetchData();
  }, [])

  return (
    <div className={`${styles.contentContainer}`}>
      <div className={styles.footerItem}>
        <span className={styles.label}>出品方：</span>
        {
          <img src={XLabLogo} alt="" className={styles.logo} />
        }
        <span>X-lab实验室</span>
      </div>
      <VerticalDivider />
      <div className={styles.footerItem}>
        <span className={styles.label}>数据来源：</span>
        {
          <img src={OpenDiggerLogo} alt="" className={styles.logo} />
        }
      </div>
      <VerticalDivider />
      <div>
        <span className={styles.label}>更新时间：</span>
        <span>{updatedAt}</span>
      </div>
    </div>
  );
}
