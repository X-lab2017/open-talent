const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  //跨域处理
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        // target: 'http://47.116.118.218:8081',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '',
        },
      }
    },
  }
})
