import { useEffect, useState } from 'react';
import { useData } from '../../useData';
import * as echarts from 'echarts';
import styles from './index.module.css'

type Repository = {
  prov: string;
  total: number;
  ratio:string;

}
 const EchartsModule = () => {
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const data = useData<Repository>('https://oss.open-digger.cn/openatom/prov_details.json');

  useEffect(() => {
    if (!data) return;
    setIsLoading(false);

    const chartDom = document.getElementById('main');
    if (!chartDom) return;

    const myChart = echarts.init(chartDom, null, { renderer: 'svg' });
    
    const generateSeriesList = () => {
      return data.map(item => ({
        value:item.total,
        name:item.prov,
      }));
    };
    const option = {
      color:['rgb(175,206,232,0.3)'],
      legend: {
        show:false,
        },
      tooltip: {
        trigger: 'item',
        formatter: '{b} : {c} ({d}%)',
        backgroundColor: 'rgb(175,206,232,0.8)',
        textStyle:{
          color:'#fff'
        }
      },
      series: [
        {
          name: 'Access From',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['56%', '50%'],
          avoidLabelOverlap: false,
          data: generateSeriesList(),
          label: {
            fontSize: 12,
            color: '#fff'
          },
          itemStyle: {
            opacity: 0.7,
            borderWidth: 1,
            borderColor: '#7F98B0',
            emphasis:{
              color:'rgb(175,206,232,0.6)',
            },
          }
        },
      ]
    };

    myChart.setOption(option);

    return () => {
      myChart.dispose();
    };
  }, [data]);

  if (isLoading) return <div>Loading...</div>;

  return (
    <>
      <div style={{display:'flex' ,height:'100%',alignItems:'center',gap:'40px'}}>
        <div id='main' style={{ width: '540px', height: '600px' }}></div>
        <div>
            <div style={{fontSize:'16px',color:'#fff',lineHeight:'24px',fontWeight:600}}>各行政区</div>
            <div className={styles.scroll} style={{width:'225px',overflow:'auto',height:'480px',marginTop:'16px',paddingRight:'10px'}}>
            {data.map((item)=>{
                return(
                    <div key={item.prov}  style={{display:"flex",marginBottom:'16px',fontSize:'12px',lineHeight:'16px',color:'#fff',fontWeight:400,justifyContent:'space-between'}}>
                        <div>{item.prov}</div>
                        <div>{item.ratio}</div>
                    </div>
                )
            })}
            </div>
        </div>
      </div>
    </>
  );
};

export default EchartsModule;

