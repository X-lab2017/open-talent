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
  },
  configureWebpack: {
    module: {
      rules: [
        {
          test: /\.(js|jsx|ts|tsx)$/,
          exclude: /node_modules/,
          use: {
            loader: 'babel-loader',
            options: {
              presets: [
                '@babel/preset-env',
                '@babel/preset-flow',
                ['@babel/preset-react',
                {
                  runtime: 'automatic', // 加上这行配置
                },
                ]
              ]
            }
          }
        }
      ]
    }
  }
})
