import styles from './index.module.css'
import  { useState } from 'react';
import Vector from '../../../assets/Vector.png'
import {EchartsModule} from './echats'
import {GlobalBoard} from './globalBoard'
import {ChinaBoard} from './chinaBoard'

export const CompanyPop = () => {

  const [selectedOption, setSelectedOption] = useState<string>('1'); 
  const [selectedCom, setSelectedCom] = useState<string>('1'); 
  const handleChangeOption = (option: string) => {
    setSelectedOption(option);
  };
  const handleChangeCom = () => {
    if(selectedCom === "1"){
        setSelectedCom("0");
    }else{
        setSelectedCom("1"); 
    }
  };

  const renderCom = () => {
    if (selectedCom === '1') {
      return (
        <div>
            <GlobalBoard /> 
        </div>
      );
    } else if (selectedCom === '0') {
      // 返回另一个组件
      return <EchartsModule/>;
    }
    // 如果选项不是 '全球' 或 '中国'，可以添加其他选项的处理逻辑
    return null; 
  };

  const renderComponent = () => {
    if (selectedOption === '1') {
      return (
        <div style={{overflow:'hidden'}}>
            {selectedCom === '1' ? <div className={styles.links} onClick={()=>{handleChangeCom()}}>近五年变化趋势<img style={{marginLeft:'4px'}} src={Vector}/></div>:<div className={styles.links} onClick={()=>{handleChangeCom()}}>开源企业排行榜<img style={{marginLeft:'4px'}} src={Vector}/></div>}
            {renderCom()}
        </div>
      );
    } else if (selectedOption === '0') {
      // 返回另一个组件
      return <ChinaBoard />;
    }
    // 如果选项不是 '全球' 或 '中国'，可以添加其他选项的处理逻辑
    return null; 
  };
  return(
    <>
    <h2 className={styles.popupTitle}>{selectedCom ==='1'? '开源企业排行榜':'开源企业5年变化'}</h2>
        <div style={{position:'relative'}} className={styles.countryPop}>
            <div className={styles.switch}>
                <div className={styles.option} onClick={() => handleChangeOption('1')} style={selectedOption === '1' ? { background:'rgba(189, 225, 255, 0.3)' ,borderRight: '1px solid #CCE7FF',borderRadius:'6px'} : {}}>全球</div>
                <div className={styles.option} onClick={() => handleChangeOption('0')} style={selectedOption === '0' ? { background:'rgba(189, 225, 255, 0.3)' ,borderLeft: '1px solid #CCE7FF' ,borderRadius:'6px'} : {}}>中国</div>
            </div>
            {renderComponent()}
        </div>
    </>
  )
}
