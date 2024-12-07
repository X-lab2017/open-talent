import { StatsItem, StatsItemProps } from './StatsItem.tsx';
import repository from './repository.svg';
import developer from './developer.svg';
import country from './country.svg';
import React from 'react'

import styles from './index.module.css';

export const Stats = () => {
  const stats = {
    stuCount: 6521,
    schoolCount: 692,
    commCount: 224,
    projectCount: 2312
  };

  const statsItems: StatsItemProps[] = [
    {
      icon: repository,
      text: '参与学生数',
      value: stats.stuCount,
    },
    {
      icon: developer,
      text: '参与高校数',
      value: stats.schoolCount,
    },
    {
      icon: country,
      text: '参与社区数',
      value: stats.commCount,
    },
    {
      icon: country,
      text: '参与项目数',
      value: stats.projectCount,
    },
  ];

  return (
      <div className={styles.container}>
        {statsItems.map((item, index) => (
            <StatsItem
                key={index}
                icon={item.icon}
                text={item.text}
                value={item.value}
            />
        ))}
      </div>
  );
};
