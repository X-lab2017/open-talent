import { flexRender, getCoreRowModel, useReactTable, ColumnDef } from '@tanstack/react-table';
import React from "react";

import styles from './index.module.css';

interface BaseBoardProps<T> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  columns: ColumnDef<T, any>[];
  data: T[];
  dividerWidth?: string;
}

export function BaseBoard<T> ({columns, data, dividerWidth = '400px' }: BaseBoardProps<T>) {
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
                    padding: '10px 0px' ,
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
        <div className={styles.divider } style={{ width: dividerWidth }}/>
        <tbody>
          {table.getRowModel().rows.map(row => (
            <tr key={row.id} className={styles.tr}>
              {row.getVisibleCells().map(cell => (
                <td key={cell.id}
                  style={{
                    width: cell.column.getSize(),
                    padding:'5px 0px 5px 0px',
                  }}
                >
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div >
  )
}
