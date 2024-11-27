<script>
import NavMenu from "@/components/NavMenu.vue";


export default {
  components: {NavMenu},
  methods: {
    drawOrBarChart() {
      let orBarChart = this.$echarts.init(document.getElementById("orBarChart"))

      orBarChart.setOption({
        color: ['#007AFF', '#FF9000'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        grid:{ // 让图表占满容器
          top:"23%",
          left:"5%",
          right:"13%",
          bottom:"13%"
        },
        xAxis: [
          {
            type: 'category',
            axisTick: {
              alignWithLabel: true
            },
            data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            // show: false
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '活跃度',
            min: 0,
            max: 200,
            position: 'left',
            axisLine: {
              lineStyle: {
                color: '#007AFF'
              }
            },
            axisLabel: {
              formatter: '{value}'
            }
          },
          {
            type: 'value',
            name: 'OpenRank',
            min: 0,
            max: 200,
            position: 'right',
            offset: 80,
            axisLine: {
              lineStyle: {
                color: '#FF9000'
              }
            },
            axisLabel: {
              formatter: '{value}'
            }
          }
        ],
        series: [
          {
            name: '活跃度',
            type: 'bar',
            data: [2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3],
            itemStyle: {
              barBorderRadius: [10, 10, 0, 0]
            }
          },
          {
            name: 'OpenRank',
            type: 'bar',
            yAxisIndex: 1,
            data: [2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6, 182.2, 48.7, 18.8, 6.0, 2.3],
            itemStyle: {
              barBorderRadius: [10, 10, 0, 0]
            }
          }
        ]
      })
    },
    drawSourceBarChart() {
      let sourceBarChart = this.$echarts.init(document.getElementById("sourceBarChart"))
      sourceBarChart.setOption({
        color:'#007AFF',
        dataset: {
          source: [
            [ 'rank', 'source'],
            [20, 'Open-Digger6'],
            [40, 'Open-Digger5'],
            [60, 'Open-Digger4'],
            [80, 'Open-Digger3'],
            [ 80, 'Open-Digger2'],
            [ 100, 'Open-Digger1'],

          ]
        },
        grid:{ // 让图表占满容器
          top:"8%",
          left:"15%",
          right:"3%",
          bottom:"0%"
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
          }
        },

        xAxis: {
          type: 'value',
          show: false
        },
        yAxis: {
          type: 'category',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }

        },

        series: [
          {
            type: 'bar',
            encode: {
              x: 'rank',
              y: 'source'
            },
            showBackground: true,
            backgroundStyle: {
              color: 'rgba(220, 220, 220, 0.8)',
              barBorderRadius: [15, 15, 15, 15]
            },
            itemStyle: {
              barBorderRadius: [15, 15, 15, 15]
            }
          }
        ]
      })
    },
    drawPieChart() {
      let pieChart = this.$echarts.init(document.getElementById("pieChart"))
      pieChart.setOption({
        color: this.tableColor,
        series: [
          {
            name: '访问来源',
            type: 'pie',
            radius: ['60%', '90%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '20',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: this.tableData.map((item, index) => ({
                  value: parseFloat(item.pct.replace('%', '') / 100),
                  name: item.label,
                })
            ),
            itemStyle: {
              normal: {
                borderColor:'#fff',
                borderWidth: 3
              }
            },
          }
        ]
      })
    }
  },
  mounted() {
    this.drawOrBarChart()
    this.drawSourceBarChart()
    this.drawPieChart()
  },
  data () {
    return {
      tableColor: ['#14B8A6', '#3B82F6', '#6366F1', '#EC4899', '#F59E0B', '#FACC15'],
      tableData: [
        {
          color: 1,
          content: 'Label1',
          label: '代码',
          pct: '42.9%'
        },
        {
          color: 2,
          content: 'Label2',
          label: '评审',
          pct: '28.6%'
        },
        {
          color: 3,
          content: 'Label3',
          label: '协作',
          pct: '28.5%'
        },
      ]
    }
  }
}

</script>

<template>
  <div class="member-profile">
    <NavMenu></NavMenu>



    <el-main>
      <el-row :gutter="20" style="width: 100%;">
        <el-col :span="6">
          <el-card class="card">
            <div class="row1">
              <img src="../assets/repository.png" class="detail-img">
              <div class="title">总仓库数量</div>
              <div class="number">82</div>

              <div class="detail-container">
                <img src="../assets/up.png" style="height: 25px;">
                <span class="detail-number-up">1.3%</span>
                <span class="detail-text">Up from past week</span>
              </div>

            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="card">
            <div class="row1">
              <img src="../assets/star.png" class="detail-img">
              <div class="title">Star数量</div>
              <div class="number">27</div>

              <div class="detail-container">
                <img src="../assets/up.png" style="height: 25px;">
                <span class="detail-number-up">1.3%</span>
                <span class="detail-text">Up from past week</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="card">
            <div class="row1">
              <img src="../assets/openrank.png" class="detail-img">
              <div class="title">OpenRank</div>
              <div class="number">12.18</div>

              <div class="detail-container">
                <img src="../assets/down.png" style="height: 25px;">
                <span class="detail-number-down">4.3%</span>
                <span class="detail-text">Down from yesterday</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="card">
            <div class="row1">
              <img src="../assets/activity.png" class="detail-img">
              <div class="title">活跃度</div>
              <div class="number">24.16</div>

              <div class="detail-container">
                <img src="../assets/up.png" style="height: 25px;">
                <span class="detail-number-up">1.3%</span>
                <span class="detail-text">Up from past week</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="width: 100%;">
        <el-col :span="16">
          <el-card class="card" >
            <div class="title">活跃度和OpenRank趋势</div>
            <div id="orBarChart" style="height: 165px"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="card">
            <div class="row2">
              <div class="title">编程语言</div>
              <div style="padding: 5px 0">
                <el-tag>HTML</el-tag>
                <el-tag>TypeScript</el-tag>
                <el-tag>JavaScript</el-tag>
                <el-tag>CSS</el-tag>
                <el-tag>Python</el-tag>
                <el-tag>Java</el-tag>
              </div>
<!--              <el-divider></el-divider>-->
              <div class="title">仓库标签</div>
              <div style="padding: 5px 0">
                <el-tag>Data</el-tag>
                <el-tag>Rearch</el-tag>
                <el-tag>AI</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="width: 100%;">
        <el-col :span="12">
          <el-card class="card">
            <div style="height: 235px">
              <div class="title">贡献能力</div>
              <div style="height: 190px">
                <div id="pieChart" style="height: 100%; width: 50%; display: inline-block;"></div>
                <div style="height: 100%; width: 50%; display: inline-block;">
                  <el-table :data="tableData" style="width: 100%">
                    <el-table-column prop="colorDot" label="" width="50">
                      <template slot-scope="scope">
                        <span :style="{ backgroundColor: tableColor[scope.row.color - 1], width: '10px', height: '10px', display: 'inline-block', borderRadius: '50%', marginRight: '8px' }"></span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="content" label="贡献内容" width="150">
                    </el-table-column>
                    <el-table-column prop="label" label="标签" width="90">
                    </el-table-column>
                    <el-table-column prop="pct" label="%">
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="card">
            <div>
              <div class="title">贡献来源</div>
              <div class="row3" id="sourceBarChart"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>


</template>

<style scoped>
.member-profile{
  background-color: #F8F8F8
}

.card{
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 20px;

}

.title{
  font-size: 18px;
  font-weight: bold;
  color: #636566;
}

.number{
  font-size: 32px;
  font-weight: bold;
  padding-top: 15px;
}

.detail-number-down {
  color: #00B69B;
  padding-left: 8px;
  padding-right: 8px;
}

.detail-number-up {
  color: #F93D66;
  padding-left: 8px;
  padding-right: 8px;
}

.detail-text {
  color: #636566;
  word-spacing: 2px;
}
.detail-container {
  display: flex;
  align-items: center;
  padding-top: 15px;
  font-size: 15px;
}

.main{
  padding: 0 10px;
}

.el-col-6 {
  padding-top: 10px;
  padding-bottom: 10px;
}

.el-col-16 {
  padding-top: 10px;
  padding-bottom: 10px;
}

.el-col-8 {
  padding-top: 10px;
  padding-bottom: 10px;
}

.el-col-12 {
  padding-top: 10px;
  padding-bottom: 10px;
}

.row1{
  height: 120px;
  position: relative;
  padding: 0 10px;
}

.row2{
  height: 190px;
}

.row3{
  height: 215px;
}

.detail-img{
  position: absolute;
  height: 50%;
  top: 3%;
  right: 3%;
}

.el-tag {
  border-radius: 10px;
  margin: 5px 10px;
  padding: 0 30px;
  background-color: #FFFFFF;
  border-color: #979797;
  color: #000000;
  font-weight: bold;
  width: 135px;
  text-align: center;
}

.el-table .el-table__cell {
  padding: 0 0;
}
</style>