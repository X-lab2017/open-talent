
import { IndexNumber } from './IndexNumber.tsx';
import { createColumnHelper, flexRender, getCoreRowModel, useReactTable, ColumnDef } from '@tanstack/react-table';
import { OpenRank } from './OpenRank.tsx';
import schoolList from './school_list.json';
import styles from './index.module.css';
import React from "react";

type School = {
  id: string;
  name: string;
  openRank: number;
  openRankDelta: number;
  stuCount: number;
  stuDelta: number;
  avropenRank: number;
  avropenRankDelta: number;
}

interface CountryTableProps<T> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  columns: ColumnDef<T, any>[];
  data: T[];
}


const columnHelper = createColumnHelper();


function CountryTable<T>({ columns, data }: CountryTableProps<T>) {

  const table = useReactTable({
    columns,
    data,
    getCoreRowModel: getCoreRowModel(),
  });

  return (
    <div>
      <table style={{
        tableLayout: 'fixed',
        width: '100%',
        borderCollapse: 'collapse',
      }}>
        <thead>
          {table.getHeaderGroups().map(headerGroup => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <th
                  key={header.id}
                  style={{
                    width: header.getSize(),
                    padding: '10px 0px',
                    textAlign: 'left',
                    fontSize: '16px',
                    fontWeight: 700,
                  }}
                >
                  {header.isPlaceholder
                    ? null
                    : flexRender(
                      header.column.columnDef.header,
                      header.getContext()
                    )}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        {/* 分割线 */}
        <div className={styles.divider } />
        <tbody>
          {table.getRowModel().rows.map(row => (
              <tr key={row.id} className={styles.tr}>
                {row.getVisibleCells().map(cell => (
                  <td key={cell.id}
                    style={{
                      width: cell.column.getSize(),
                      padding: '5px 0px 5px 0px',
                    }}
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                ))}
              </tr>
            )
          )}
        </tbody>
      </table>
    </div >
  )
}
export const CountryBoard = () => {

  const generateColumns = () => [
    columnHelper.accessor((_row, index) => index + 1, {
      id: 'index',
      cell: info => <span style={{paddingLeft: 8}}><IndexNumber value={info.getValue()} /></span>,
      header: () => <span style={{paddingLeft: 8}}>#</span>,
      size:30 ,
    }),
    columnHelper.accessor(row => [row.name], {
      id: 'name',
      cell: info => (
        <div className={styles.flagContainer}>
          <span>{info.getValue()[0]}</span>
        </div>
      ),
      header: () => '高校名称',
      size:  100 ,
    }),
    columnHelper.accessor(row => [row.openRankDelta, row.openRank], {
      id: 'openRank',
      cell: info => <OpenRank delta={info.getValue()[0]} openrank={Number(info.getValue()[1])} />,

      header: () => 'openRank',
      size:  130,
    }),
    columnHelper.accessor(row => [row.stuDelta, row.stuCount], {
      id: 'stu',
      cell: info => <OpenRank delta={info.getValue()[0]} openrank={Number(info.getValue()[1])} />,
      header: () => '参与学生数',
      size:  120,
    }),
    columnHelper.accessor(row => [row.avropenRankDelta, row.avropenRank], {
      id: 'avrOpenRank',
      cell: info => <OpenRank delta={info.getValue()[0]} openrank={Number(info.getValue()[1])} />,
      header: () => '人均OpenRank',
      size:  120,
    }),
  ];

  const data = schoolList;

  return (
    <div>
      <CountryTable columns={generateColumns()} data={data.slice(0, 10)} />
      <div className={ styles.countryBoardFooterDiv } >
        {/* <span className={ styles.countryBoardFooterSpan }>
          * 主流协作平台注册账户去重后测算
        </span> */}
      </div>
    </div>
  );
}
