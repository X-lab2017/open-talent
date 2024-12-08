import { BaseBoard } from './BaseBoard.tsx';
import { IndexNumber } from './IndexNumber.tsx';
import studentList from './student_list.json';
import React from "react";

import { createColumnHelper } from '@tanstack/react-table';
import styles from "./index.module.css";

type Student ={
  id: string;
  name: string;
  openRank: number;
  school: string;
  comm: string;
  month: number;
}

const columnHelper = createColumnHelper();

const generateColumns = () => [
  columnHelper.accessor((_row, index) => index + 1, {
    id: 'index',
    cell: info => <IndexNumber value={info.getValue()} />,
    header: () => '#',
    size:22,
  }),
  columnHelper.accessor(row => [row.name], {
    id: 'name',
    cell: info => (
        <div className={styles.flagContainer}>
          <span>{info.getValue()[0]}</span>
        </div>
    ),
    header: () => '学生姓名',
    size:55,
  }),
  columnHelper.accessor(row => [row.openRank], {
    id: 'openRank',
    cell: info => (
        <div className={styles.flagContainer}>
          <span>{info.getValue()[0]}</span>
        </div>
    ),
    header: () => 'OpenRank',
    size:72,
  }),
  columnHelper.accessor(row => [row.school], {
    id: 'school',
    cell: info => (
        <div className={styles.flagContainer}>
          <span>{info.getValue()[0]}</span>
        </div>
    ),
    header: () => '学校',
    size:85,
  }),
  columnHelper.accessor(row => [row.comm], {
    id: 'comm',
    cell: info => (
        <div className={styles.flagContainer}>
          <span>{info.getValue()[0]}</span>
        </div>
    ),
    header: () => '参与社区',
    size:110,
  }),
  columnHelper.accessor(row => [row.month], {
    id: 'month',
    cell: info => (
        <div className={styles.flagContainer}>
          <span>{info.getValue()[0]}</span>
        </div>
    ),
    header: () => '活跃月数',
    size:52,
  })
];

export const DeveloperBoard = () => {
  const data = studentList;

  return (
    <div style={{cursor:'pointer'}}>
      <BaseBoard columns={generateColumns()} data={data.slice(0, 12)} />
    </div>
  )
}
