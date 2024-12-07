module.exports = {
  // presets: [
  //   '@vue/cli-plugin-babel/preset',
  // ],
  presets: [
      "@babel/preset-env",
    "@babel/preset-flow",
    ["@babel/preset-react",{runtime: 'automatic'}]
  ],
  plugins: ["@babel/plugin-transform-react-jsx"]
}
