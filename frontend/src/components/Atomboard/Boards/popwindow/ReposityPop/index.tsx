import { BaseBoard } from '../../BaseBoard';
import { IndexNumber } from '../../IndexNumber';
import { NameWithIcon } from '../../NameWithIcon';
import { OpenRank } from '../../OpenRank';
import { useData } from '../../useData';
// import { TextAlign } from '../../TextAlign';
import styles from './index.module.css'
import { createColumnHelper } from '@tanstack/react-table';
import { useState } from 'react';


type Repository = {
  id: string;
  name: string;
  platform: string;
  openrank: number;
  delta: number | '-';
  logo: string;
  participant_count: number;
}

const columnHelper = createColumnHelper<Repository>();

const generateColumns = () => [
  columnHelper.accessor((_row, index) => index + 1, {
    id: 'index',
    cell: info => <IndexNumber value={info.getValue()} />,
    header: () => '#',
    size: 40,
  }),
  columnHelper.accessor(row => [row.logo, row.name], {
    id: 'company',
    cell: info => <NameWithIcon icon={info.getValue()[0]} name={info.getValue()[1]} />,
    header: () => '项目',
    size: 180,
  }),
  columnHelper.accessor(row => [row.delta, row.openrank], {
    id: 'openrank',
    cell: info => <OpenRank delta={info.getValue()[0]} openrank={Number(info.getValue()[1])} />,
    header: () => 'OpenRank',
    size: 140,
  }),
];

const ReposityPop = () => {

  const globalData = useData<Repository>('https://oss.open-digger.cn/openatom/repo_list.json');
  const chinaData = useData<Repository>('https://oss.open-digger.cn/openatom/project_list_china.json');

  const [selectedOption, setSelectedOption] = useState<string>('1');
  const handleChangeOption = (option: string) => {
    setSelectedOption(option);
  };


  const renderComponent = () => {
    let renderData: Repository[] = [];
    if (selectedOption === '1') {
      renderData = chinaData.slice(0, 20);
    } else if (selectedOption === '0') {
      renderData = globalData.slice(0, 20);
    }
    // 如果选项不是 '全球' 或 '中国'，可以添加其他选项的处理逻辑
    return <BaseBoard dividerWidth='860px' columns={generateColumns()} data={renderData} />;
  };
  return (<div className={styles.reposityPop}>
    <div className={styles.switch}>
      <div className={styles.option} onClick={() => handleChangeOption('1')} style={selectedOption === '1' ? { background: 'rgba(189, 225, 255, 0.3)', borderRight: '1px solid #CCE7FF', borderRadius: '6px' } : {}}>中国</div>
      <div className={styles.option} onClick={() => handleChangeOption('0')} style={selectedOption === '0' ? { background: 'rgba(189, 225, 255, 0.3)', borderLeft: '1px solid #CCE7FF', borderRadius: '6px' } : {}}>全球</div>
    </div>
    {renderComponent()}
  </div>)
}

export default ReposityPop;
