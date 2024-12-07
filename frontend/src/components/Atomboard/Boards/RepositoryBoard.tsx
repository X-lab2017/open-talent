import { BaseBoard } from './BaseBoard.tsx';
import { IndexNumber } from './IndexNumber.tsx';
import { NameWithIcon } from './NameWithIcon.tsx';
import { OpenRank } from './OpenRank.tsx';
import { useData } from './useData.ts';
import React from "react";
import { createColumnHelper } from '@tanstack/react-table';

type Repository = {
  id: string;
  name: string;
  platform: string;
  openrank: number;
  delta: number | '-';
  logo: string;
}

const columnHelper = createColumnHelper();

const generateColumns = () => [
  columnHelper.accessor((_row, index) => index + 1, {
    id: 'index',
    cell: info => <IndexNumber value={info.getValue()} />,
    header: () => '#',
    size:50,
  }),
  columnHelper.accessor(row => [row.logo, row.name], {
    id: 'project',
    cell: info => <NameWithIcon icon={info.getValue()[0]} name={info.getValue()[1]} />,
    header: () => '项目',
    size:140,
  }),
  columnHelper.accessor(row => [row.delta, row.openrank], {
    id: 'openrank',
    cell: info => <OpenRank delta={info.getValue()[0]} openrank={Number(info.getValue()[1])} />,
    header: () => 'OpenRank 月度环比',
    size:140,
  }),
];

export const RepositoryBoard = () => {
  const data = useData('https://oss.open-digger.cn/openatom/project_list_china.json');

  return (
    <div style={{cursor:'pointer'}}>
      <BaseBoard  columns={generateColumns()} data={data.slice(0,12)} />
    </div>
  )
}
