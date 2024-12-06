import ChinaBoard  from "./chinaBoard";
import EchartsModule from './echarts'
import Vector from '../../../assets/Vector.png'
import  { useState } from 'react';
export const CountryPop = () => {
    const [selectedOption, setSelectedOption] = useState<string>('1');
    const handleChange = () => {
        if(selectedOption === '1'){
            setSelectedOption('2');
        }else{
            setSelectedOption('1');
        }
    }
    return(
    <>
        <div style={{position:'absolute',top:'26px',left:'286px',fontSize:'14px',color:'#cae4fd',cursor:'pointer',height:'20px',zIndex:'10'}} onClick={()=>handleChange()}>
            <span>{selectedOption==='1'?'比例分布图':'排行榜'}</span>
            <img src={Vector} style={{marginLeft:'4px'}}/>
        </div>
        {selectedOption==='1' && <ChinaBoard /> }
        {selectedOption==='2' && <EchartsModule />}
    </>
    )
}