import { BaseBoard } from '../../BaseBoard';
import { IndexNumber } from '../../IndexNumber';
import { NameWithIcon } from '../../NameWithIcon';
import { OpenRank } from '../../OpenRank';
import { useData } from '../../useData';

import { TextAlign } from '../../TextAlign';

import { createColumnHelper } from '@tanstack/react-table';

import numeral from 'numeral';
type Repository = {
  id: string;
  name: string;
  platform: string;
  openrank: number;
  delta: number | '-';
  logo: string;
  repo_count:number;
  participant_count:number;
  flag_code:string
}

const columnHelper = createColumnHelper<Repository>();

const generateColumns = () => [
  columnHelper.accessor((_row, index) => index + 1, {
    id: 'index',
    cell: info => <IndexNumber value={info.getValue()} />,
    header: () => '#',
    size:40,
  }),
  columnHelper.accessor(row => [row.logo, row.name], {
    id: 'deceloper',
    cell: info => <NameWithIcon icon={info.getValue()[0]} name={info.getValue()[1]} />,
    header: () => '企业',
    size:180,
  }),
  columnHelper.accessor(row => [row.delta, row.openrank], {
    id: 'openrank',
    cell: info => <OpenRank delta={info.getValue()[0]} openrank={Number(info.getValue()[1])} />,
    header: () => 'OpenRank',
    size:140,
  }),
  columnHelper.accessor(row => [row.repo_count], {
    id: 'repository',
    cell: info =>  <TextAlign value={numeral(info.getValue()[0]).format('0,0')} />,
    header: () => <TextAlign value={'仓库个数'} />,
    size:70,
  }),
  columnHelper.accessor(row => [row.participant_count], {
    id: 'peoplenumber',
    cell: info =>  <TextAlign value={numeral(info.getValue()[0]).format('0,0')} />,
    header: () => <TextAlign value={'参与人数'} />,
    size: 50,
  }),
  columnHelper.accessor(row => [row.flag_code], {
    id: 'country',
    cell: () => <TextAlign value={'🇨🇳'} textAlign='right' />,
    header: () => <TextAlign value={'国家'} textAlign='right' />,
    size: 40,
  }),
];



export const ChinaBoard = () => {
  const data = useData<Repository>('https://oss.open-digger.cn/openatom/company_list_china.json');

  return <BaseBoard dividerWidth='860px'   columns={generateColumns()} data={data.slice(0, 20)} />
}
