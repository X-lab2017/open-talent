
import { BaseBoard } from '../../BaseBoard';
import { IndexNumber } from '../../IndexNumber';
import { TextAlign } from '../../TextAlign';

import { useData } from '../../useData';
import styles from './index.module.css'
import { createColumnHelper } from '@tanstack/react-table';
import { TrendsAndChanges } from '../../TrendsAndChanges'
type Repository = {
  id: string;
  order: number;
  prov: string;
  delta: number | '-';
  total: number | '-';
  ratio: number | '-';
  est: number | '-';
}

const columnHelper = createColumnHelper<Repository>();

const generateColumns = () => [
  columnHelper.accessor((row) => [row.order], {
    id: 'index',
    cell: info => <IndexNumber value={info.getValue()[0]} />,
    header: () => '#',
    size: 30,
  }),
  columnHelper.accessor(row => [row.prov], {
    id: 'prov',
    header: () => <div className={styles.headerFont}>行政区划</div>,
    size: 100,
  }),
  columnHelper.accessor(row => [row.ratio], {
    id: 'ratio',
    header: () => <div className={styles.headerFont}><TextAlign value={'全国占比'} /></div>,
    size: 60,
    cell: info => <TextAlign value={info.getValue()[0]} />
  }),
  columnHelper.accessor(row => [row.est], {
    id: 'est',
    header: () => <div className={styles.headerFont}><TextAlign value={'总量（万）'} /></div>,
    size: 80,
    cell: info => <TextAlign value={info.getValue()[0]} />
  }),
  columnHelper.accessor(row => [row.delta], {
    id: 'delta',
    header: () => <div className={styles.headerFont}><TextAlign value={'同比变化'} /></div>,
    size: 60,
    cell: info => <TrendsAndChanges detail={info.getValue()[0]}/>
  }),
];

const ChinaBoard = () => {

  const data = useData<Repository>('https://oss.open-digger.cn/openatom/prov_details.json');
  const dataLeft = data?.slice(0, 17)?.map((item, index) => ({ ...item, order: index + 1 })) || [];
  const dataRight = data?.slice(17, 34)?.map((item, index) => ({ ...item, order: index + 18 })) || [];
  return (
    <div className={styles.countryPop}>
      <BaseBoard dividerWidth='380px' columns={generateColumns()} data={dataLeft} />
      <BaseBoard dividerWidth='380px' columns={generateColumns()} data={dataRight} />
    </div>
  )
}

export default ChinaBoard;
