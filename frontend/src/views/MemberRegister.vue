<template>
  <div>
    <NavMenu></NavMenu>

    <div class="d1">
      <h3>成员注册</h3>
      <el-form :model="form" :rules="rules" ref="form" label-position="left">
        <el-row :gutter="180">
          <el-col :span="8">
            <div class="input-label">姓名</div>
            <el-form-item prop="name">
              <el-input v-model="form.name" placeholder="名字"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">国家</div>
            <el-form-item prop="nationality">
              <el-select v-model="form.nationality" filterable placeholder="选择国家">
                <el-option
                    v-for="item in countrys"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">学校</div>
            <el-form-item prop="organizationId">
              <el-select v-model="form.organizationId" filterable placeholder="学校">
                <el-option
                    v-for="item in orgs"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="180">
          <!-- 第二行，三列 -->
          <el-col :span="8">
            <div class="input-label">GitHub账号</div>
            <el-form-item prop="githubAccount">
              <el-input v-model="form.githubAccount" placeholder="账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">Gitee账号</div>
            <el-form-item prop="giteeAccount">
              <el-input v-model="form.giteeAccount" placeholder="账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">AtomGit账号</div>
            <el-form-item prop="atomgitAccount">
              <el-input v-model="form.atomgitAccount" placeholder="账号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="180">
          <!-- 第三行，两列（剩下一列空白） -->
          <el-col :span="8">
            <div class="input-label">联系邮箱</div>
            <el-form-item>
              <el-input v-model="form.contactEmail" placeholder="邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">通讯地址</div>
            <el-form-item>
              <el-input v-model="form.contactAddress" placeholder="地址"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 可以选择添加其他表单项或者留空 -->
          </el-col>
        </el-row>
      </el-form>

      <div style="margin:0 0 40px 0">
        <el-button type="primary" v-on:click="goToOrgRegister" >返回</el-button>
        <el-button type="primary" plain v-on:click="submitForm">完成注册</el-button>
      </div>

      <h3>批量注册</h3>
      <div style="margin:20px 0">
<!--        <el-button icon="el-icon-upload2" type="primary" plain>批量导入</el-button>-->
        <el-button
            icon="el-icon-download"
            type="primary"
            plain v-on:click="templateDownload"
            style="margin: 10px 0;"
        >模板下载</el-button>

        <el-upload
            class="upload-demo"
            action="/member/register/upload"
            :http-request="uploadFile"
            :limit="1"
        >
          <el-button
              icon="el-icon-upload2"
              type="primary"
              plain
              style="margin: 10px 0;"
          >批量导入</el-button>
        </el-upload>
      </div>

      <el-footer>
        <div class="footer-content">
          <img
              src="/images/lab-logo.png"
              alt="Xlab Logo"
              class="footer-logo"
          />
          <p>官方支持</p >
          <p>Copyright © 2024 X-lab</p >
        </div>
      </el-footer>
    </div>
  </div>


</template>

<script>
import NavMenu from "@/components/NavMenu.vue";

export default {
  components: {NavMenu},
  data() {
    var validatePass = (rule, value, callback) => {
      console.log("到达",value)
      if (this.form.githubAccount === '' && this.form.giteeAccount === '' && this.form.atomgitAccount === ''){
        callback(new Error("GitHub，Gitee，AtomGit账号至少填写一项"))
      } else {
        callback()
      }
    }
    return {
      countrys: [
        {value:'Angola',label:'安哥拉'},
        {value:'Afghanistan',label:'阿富汗'},
        {value:'Albania',label:'阿尔巴尼亚'},
        {value:'Algeria',label:'阿尔及利亚'},
        {value:'Andorra',label:'安道尔共和国'},
        {value:'Anguilla',label:'安圭拉岛'},
        {value:'Antigua and Barbuda',label:'安提瓜和巴布达'},
        {value:'Argentina',label:'阿根廷'},
        {value:'Armenia',label:'亚美尼亚'},
        {value:'Ascension',label:'阿森松'},
        {value:'Australia',label:'澳大利亚'},
        {value:'Austria',label:'奥地利'},
        {value:'Azerbaijan',label:'阿塞拜疆'},
        {value:'Bahamas',label:'巴哈马'},
        {value:'Bahrain',label:'巴林'},
        {value:'Bangladesh',label:'孟加拉国'},
        {value:'Barbados',label:'巴巴多斯'},
        {value:'Belarus',label:'白俄罗斯'},
        {value:'Belgium',label:'比利时'},
        {value:'Belize',label:'伯利兹'},
        {value:'Benin',label:'贝宁'},
        {value:'Bermuda Is',label:'百慕大群岛'},
        {value:'Bolivia',label:'玻利维亚'},
        {value:'Botswana',label:'博茨瓦纳'},
        {value:'Brazil',label:'巴西'},
        {value:'Brunei',label:'文莱'},
        {value:'Bulgaria',label:'保加利亚'},
        {value:'Burkina Faso',label:'布基纳法索'},
        {value:'Burma',label:'缅甸'},
        {value:'Burundi',label:'布隆迪'},
        {value:'Cameroon',label:'喀麦隆'},
        {value:'Canada',label:'加拿大'},
        {value:'Cayman Is',label:'开曼群岛'},
        {value:'Central African Republic',label:'中非共和国'},
        {value:'Chad',label:'乍得'},
        {value:'Chile',label:'智利'},
        {value:'China',label:'中国'},
        {value:'Colombia',label:'哥伦比亚'},
        {value:'Congo',label:'刚果'},
        {value:'Cook Is',label:'库克群岛'},
        {value:'Costa Rica',label:'哥斯达黎加'},
        {value:'Cuba',label:'古巴'},
        {value:'Cyprus',label:'塞浦路斯'},
        {value:'Czech Republic',label:'捷克'},
        {value:'Denmark',label:'丹麦'},
        {value:'Djibouti',label:'吉布提'},
        {value:'Dominica Rep',label:'多米尼加共和国'},
        {value:'Ecuador',label:'厄瓜多尔'},
        {value:'Egypt',label:'埃及'},
        {value:'EI Salvador',label:'萨尔瓦多'},
        {value:'Estonia',label:'爱沙尼亚'},
        {value:'Ethiopia',label:'埃塞俄比亚'},
        {value:'Fiji',label:'斐济'},
        {value:'Finland',label:'芬兰'},
        {value:'France',label:'法国'},
        {value:'French Guiana',label:'法属圭亚那'},
        {value:'French Polynesia',label:'法属玻利尼西亚'},
        {value:'Gabon',label:'加蓬'},
        {value:'Gambia',label:'冈比亚'},
        {value:'Georgia',label:'格鲁吉亚'},
        {value:'Germany',label:'德国'},
        {value:'Ghana',label:'加纳'},
        {value:'Gibraltar',label:'直布罗陀'},
        {value:'Greece',label:'希腊'},
        {value:'Grenada',label:'格林纳达'},
        {value:'Guam',label:'关岛'},
        {value:'Guatemala',label:'危地马拉'},
        {value:'Guinea',label:'几内亚'},
        {value:'Guyana',label:'圭亚那'},
        {value:'Haiti',label:'海地'},
        {value:'Honduras',label:'洪都拉斯'},
        {value:'Hungary',label:'匈牙利'},
        {value:'Iceland',label:'冰岛'},
        {value:'India',label:'印度'},
        {value:'Indonesia',label:'印度尼西亚'},
        {value:'Iran',label:'伊朗'},
        {value:'Iraq',label:'伊拉克'},
        {value:'Ireland',label:'爱尔兰'},
        {value:'Israel',label:'以色列'},
        {value:'Italy',label:'意大利'},
        {value:'Ivory Coast',label:'科特迪瓦'},
        {value:'Jamaica',label:'牙买加'},
        {value:'Japan',label:'日本'},
        {value:'Jordan',label:'约旦'},
        {value:'Kampuchea (Cambodia )',label:'柬埔寨'},
        {value:'Kazakstan',label:'哈萨克斯坦'},
        {value:'Kenya',label:'肯尼亚'},
        {value:'Korea',label:'韩国'},
        {value:'Kuwait',label:'科威特'},
        {value:'Kyrgyzstan',label:'吉尔吉斯坦'},
        {value:'Laos',label:'老挝'},
        {value:'Latvia',label:'拉脱维亚'},
        {value:'Lebanon',label:'黎巴嫩'},
        {value:'Lesotho',label:'莱索托'},
        {value:'Liberia',label:'利比里亚'},
        {value:'Libya',label:'利比亚'},
        {value:'Liechtenstein',label:'列支敦士登'},
        {value:'Lithuania',label:'立陶宛'},
        {value:'Luxembourg',label:'卢森堡'},
        {value:'Madagascar',label:'马达加斯加'},
        {value:'Malawi',label:'马拉维'},
        {value:'Malaysia',label:'马来西亚'},
        {value:'Maldives',label:'马尔代夫'},
        {value:'Mali',label:'马里'},
        {value:'Malta',label:'马耳他'},
        {value:'Mariana Is',label:'马里亚那群岛'},
        {value:'Martinique',label:'马提尼克'},
        {value:'Mauritius',label:'毛里求斯'},
        {value:'Mexico',label:'墨西哥'},
        {value:'Moldova',label:'摩尔多瓦'},
        {value:'Monaco',label:'摩纳哥'},
        {value:'Mongolia',label:'蒙古'},
        {value:'Montserrat Is',label:'蒙特塞拉特岛'},
        {value:'Morocco',label:'摩洛哥'},
        {value:'Mozambique',label:'莫桑比克'},
        {value:'Namibia',label:'纳米比亚'},
        {value:'Nauru',label:'瑙鲁'},
        {value:'Nepal',label:'尼泊尔'},
        {value:'Netheriands Antilles',label:'荷属安的列斯'},
        {value:'Netherlands',label:'荷兰'},
        {value:'New Zealand',label:'新西兰'},
        {value:'Nicaragua',label:'尼加拉瓜'},
        {value:'Niger',label:'尼日尔'},
        {value:'Nigeria',label:'尼日利亚'},
        {value:'North Korea',label:'朝鲜'},
        {value:'Norway',label:'挪威'},
        {value:'Oman',label:'阿曼'},
        {value:'Pakistan',label:'巴基斯坦'},
        {value:'Panama',label:'巴拿马'},
        {value:'Papua New Cuinea',label:'巴布亚新几内亚'},
        {value:'Paraguay',label:'巴拉圭'},
        {value:'Peru',label:'秘鲁'},
        {value:'Philippines',label:'菲律宾'},
        {value:'Poland',label:'波兰'},
        {value:'Portugal',label:'葡萄牙'},
        {value:'Puerto Rico',label:'波多黎各'},
        {value:'Qatar',label:'卡塔尔'},
        {value:'Reunion',label:'留尼旺'},
        {value:'Romania',label:'罗马尼亚'},
        {value:'Russia',label:'俄罗斯'},
        {value:'Saint Lueia',label:'圣卢西亚'},
        {value:'Saint Vincent',label:'圣文森特岛'},
        {value:'Samoa Eastern',label:'东萨摩亚(美)'},
        {value:'Samoa Western',label:'西萨摩亚'},
        {value:'San Marino',label:'圣马力诺'},
        {value:'Sao Tome and Principe',label:'圣多美和普林西比'},
        {value:'Saudi Arabia',label:'沙特阿拉伯'},
        {value:'Senegal',label:'塞内加尔'},
        {value:'Seychelles',label:'塞舌尔'},
        {value:'Sierra Leone',label:'塞拉利昂'},
        {value:'Singapore',label:'新加坡'},
        {value:'Slovakia',label:'斯洛伐克'},
        {value:'Slovenia',label:'斯洛文尼亚'},
        {value:'Solomon Is',label:'所罗门群岛'},
        {value:'Somali',label:'索马里'},
        {value:'South Africa',label:'南非'},
        {value:'Spain',label:'西班牙'},
        {value:'SriLanka',label:'斯里兰卡'},
        {value:'St.Lucia',label:'圣卢西亚'},
        {value:'St.Vincent',label:'圣文森特'},
        {value:'Sudan',label:'苏丹'},
        {value:'Suriname',label:'苏里南'},
        {value:'Swaziland',label:'斯威士兰'},
        {value:'Sweden',label:'瑞典'},
        {value:'Switzerland',label:'瑞士'},
        {value:'Syria',label:'叙利亚'},
        {value:'Tajikstan',label:'塔吉克斯坦'},
        {value:'Tanzania',label:'坦桑尼亚'},
        {value:'Thailand',label:'泰国'},
        {value:'Togo',label:'多哥'},
        {value:'Tonga',label:'汤加'},
        {value:'Trinidad and Tobago',label:'特立尼达和多巴哥'},
        {value:'Tunisia',label:'突尼斯'},
        {value:'Turkey',label:'土耳其'},
        {value:'Turkmenistan',label:'土库曼斯坦'},
        {value:'Uganda',label:'乌干达'},
        {value:'Ukraine',label:'乌克兰'},
        {value:'United Arab Emirates',label:'阿拉伯联合酋长国'},
        {value:'United Kiongdom',label:'英国'},
        {value:'United States of America',label:'美国'},
        {value:'Uruguay',label:'乌拉圭'},
        {value:'Uzbekistan',label:'乌兹别克斯坦'},
        {value:'Venezuela',label:'委内瑞拉'},
        {value:'Vietnam',label:'越南'},
        {value:'Yemen',label:'也门'},
        {value:'Yugoslavia',label:'南斯拉夫'},
        {value:'Zimbabwe',label:'津巴布韦'},
        {value:'Zaire',label:'扎伊尔'},
        {value:'Zambia',label:'赞比亚'}
      ],
      orgs: [],
      form: {
        name: '',
        githubAccount: '',
        contactEmail: '',
        nationality: '',
        giteeAccount: '',
        contactAddress: '',
        organizationId: '',
        atomgitAccount: '',
      },
      rules: {
        name: [
          { required : true, message: '请输入姓名', trigger: 'blur' }
        ],
        nationality: [
          { required : true, message: '请选择国家', trigger: 'blur' }
        ],
        organizationId: [
          { required : true, message: '请输入学校', trigger: 'blur' }
        ],
        githubAccount: [
          { validator: validatePass, trigger: 'blur' }
        ],
        giteeAccount: [
          { validator: validatePass, trigger: 'blur' }
        ],
        atomgitAccount: [
          { validator: validatePass, trigger: 'blur' }
        ],
      },
    };
  },
  methods: {
    async fetchOrgs(){
      try {
        const response = await this.$http.get('/member/getOrgName');  // 替换为实际的 API 端点
        let res = response.data.data
        for (let key in res) {
          this.orgs.push(
              {value:key, label:res[key]}
          )
        }
      } catch (error) {
        console.error('获取组织名称失败:', error);
      }
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          this.$http.post("/member/register/single",this.form)
              .then(response => {
                console.log(response.data)
                this.$message.success('注册成功！');
              })
              .catch(error => {
                console.error(error)
                this.$message.error('注册失败');
              })
        } else {
          return false;
        }
      })
    },
    goToOrgRegister() {
      this.$router.push({ name : 'OrgRegister' })
    },
    templateDownload() {
      this.$http.get('/member/register/download',{responseType: 'blob'}).then(response => {
        const blob = new Blob([response.data]);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'template.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
      }).catch(error => {
          console.error("下载文件时出错:", error);
      })},
    uploadFile({ file }) {
      const formData = new FormData();
      formData.append('file', file);

      return this.$http.post('/member/register/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then((response) => {
        this.$message.success('注册成功');
      }).catch(error => {
        console.error('上传失败', error);
        this.$message.error('注册失败');
      });
    }
  },
  mounted() {
    this.fetchOrgs()
  }
};
</script>

<style scoped>
.el-select{
  display: block;
}
.input-label{
  text-align: left; /* 显式设置文本靠左，虽然这是默认行为 */
  margin-top: 15px; /* 上边距 */
  margin-bottom: 8px; /* 下边距 */
  margin-left: 2px; /* 左边距 */
  font-size: 14px;
}
.d1{
  margin: 0px 10%;
}
.el-row{
  margin-bottom: 20px;
}
.el-footer {
  text-align: center;
  padding: 30px 0;
  background: #fff;
  border-top: 1px solid #eaeaea;
}
.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.footer-logo {
  width: 50px;
  height: 50px;
  margin-bottom: 10px;
}
</style>
