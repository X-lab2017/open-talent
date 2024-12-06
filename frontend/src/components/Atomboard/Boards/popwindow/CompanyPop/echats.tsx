import { useEffect, useState } from 'react';
import { useData } from '../../useData';
import * as echarts from 'echarts';

type Repository = {
  key: string;
  value: number[];
}

export const EchartsModule = () => {
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const data = useData<Repository>('https://oss.open-digger.cn/openatom/company_trend.json');

  useEffect(() => {
    if (!data) return;
    setIsLoading(false);

    const chartDom = document.getElementById('main');
    if (!chartDom) return;

    const myChart = echarts.init(chartDom, null, { renderer: 'svg' });
    const names = data.map(item => item.key);
    const currentYear = new Date().getFullYear(); // 获取当前年份
    const years = Array.from({ length: 5 }, (_, index) => (currentYear - index).toString()).reverse();
    

    const generateSeriesList = () => {
      return data.map(item => ({
        name: item.key,
        type: 'line',
        smooth: true,
        emphasis: { focus: 'series' },
        endLabel: { show: true, formatter: '{a}', fontSize: 12, color: '#ffffff' },
        lineStyle: { width: 2 },
        data: item.value,
      }));
    };
    
    const option = {
      legend: {
        data: names,
        textStyle: {
          fontSize: 12,
          color: '#ffffff',
          fontWeight: 400
        },
      },
      textStyle: {
        color: '#ffffff',
        fontSize: '12px',
        textShadowColor: 'transparent'
      },
      tooltip: {
        trigger: 'item'
      },
      xAxis: {
        type: 'category',
        splitLine: { show: false },
        axisLabel: { margin: 20, fontSize: 12 },
        boundaryGap: false,
        data: years,
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#ffffff', margin: 20, fontSize: 12, fontWeight: 400 },
        inverse: true,
        interval: 1,
        min: 1,
        max: names.length,
        splitLine: {
          show: false
        }
      },
      series: generateSeriesList()
    };

    myChart.setOption(option);

    return () => {
      myChart.dispose();
    };
  }, [data]);

  if (isLoading) return <div>Loading...</div>;

  return (
    <div id='main' style={{ width: '600px', height: '600px', marginTop: '40px' }}></div>
  );
};
