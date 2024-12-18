import { useEffect } from "react";
import * as echarts from 'echarts';
import geoData from './geo.json';
import chinaJson from './china.json';
import styles from './index.module.css'
import React from 'react'

interface DataItem {
    name: string;
    value: number;
}

type GeoCoord = Record<string, number[]>;


export const MapBoard = () => {
    const data = geoData.data;
    const geoCoordMap = geoData.geoCoordMap;

    useEffect(() => {
        echarts.registerMap('china', chinaJson);

        const chartDom = document.getElementById('main');
        if (!chartDom) return;

        const myChart = echarts.init(chartDom);

        const convertData = function (data: DataItem[]): { name: string; value: any[] }[] {
            const res: { name: string; value: any[] }[] = [];
            for (let i = 0; i < data.length; i++) {
                const geoCoord = geoCoordMap[data[i].name];
                if (geoCoord) {
                    res.push({
                        name: data[i].name,
                        value: geoCoord.concat(data[i].value),
                    });
                }
            }
            return res;
        };

        const option = {
            title: {
                // text: "全国数据请求量",
                // subtext: "Show Data",
                left: "center",
                top: "top",
                textStyle: {
                    color: "#fff",
                },
            },
            // tooltip:{},
            tooltip: {
                trigger: 'item',
                formatter: function (params){
                    var res = `
                        <div style="width: 130px">
                        <div style="margin-left: 5px; margin-bottom: 10px">${params.seriesName}</div>
                        <div style="display: flex; justify-content: center; margin-left: 5px;" >

                            <div
                                style="display: inline-block;
                                width: 10px;
                                height: 10px;
                                margin-top: 5px;
                                margin-right: 10px;
                                border-radius: 50%;
                                background-color: ${params.color}"
                            ></div>
                            <span>${params.name}</span>
                            <span style="margin-left: auto;">${params.value[2]}</span>
                        </div>
                        </div>

                    `
                    return res
                }
            },
            legend: {
                left: "left",
                data: ["强", "中", "弱"],
                textStyle: {
                    color: "#ccc",
                },
            },
            // backgroundColor: {
            //     type: "linear",
            //     x: 0,
            //     y: 0,
            //     x2: 1,
            //     y2: 1,
            //     colorStops: [
            //         { offset: 0, color: "#0f2c70" },
            //         { offset: 1, color: "#091732" },
            //     ],
            //     globalCoord: false,
            // },
            geo: {
                map: "china",
                show: true,
                roam: false,
                silent:true,
                label: { emphasis: { show: false } },
                itemStyle: {
                    normal: {
                        areaColor: "#091632",
                        borderColor: "#1773c3",
                        shadowColor: "#1773c3",
                        shadowBlur: 20,
                    },
                },
            },
            series: [
                {
                    type: "map",
                    map: "china",
                    geoIndex: 1,
                    aspectScale: 0.75,
                    showLegendSymbol: true,
                    label: {
                        normal: { show: false },
                        emphasis: { show: false, textStyle: { color: "#fff" } },
                    },
                    silent:true,
                    roam: false,
                    itemStyle: {
                        normal: {
                            areaColor: "#031525",
                            borderColor: "#3B5077",
                            borderWidth: 1,
                        },
                        emphasis: { areaColor: "#0f2c70" },
                    },
                    data: [{ name: "北京", value: 21300 }],
                },
                {
                    name: "城市",
                    type: "scatter",
                    coordinateSystem: "geo",
                    data: convertData(data),
                    symbolSize: (val) => val[2] / 8,
                    label: {
                        normal: { formatter: "{b}", position: "right", show: false },
                        emphasis: { show: true },
                    },
                    itemStyle: { normal: { color: "#ddb926" } },

                },
                {
                    name: "前5",
                    type: "effectScatter",
                    coordinateSystem: "geo",
                    // data: convertData(data.sort((a, b) => b.value - a.value).slice(0, 5)),
                    data: convertData(data.sort((a, b) => b.value - a.value).slice(0, 5)),
                    symbolSize: (val) => val[2] / 8,
                    showEffectOn: "render",
                    rippleEffect: { brushType: "stroke" },
                    hoverAnimation: true,
                    label: {
                        normal: { formatter:"{b}", position: "right", show: true },
                    },
                    itemStyle: {
                        normal: {
                            color: "#f4e925",
                            shadowBlur: 10,
                            shadowColor: "#333",
                        },
                    },
                    zlevel: 1,
                },
            ],
        };

        myChart.setOption(option);

        return () => {
            myChart.dispose();
        };
    }, []);


    return (

            <div id="main" className={styles.main}></div>

    )
};
