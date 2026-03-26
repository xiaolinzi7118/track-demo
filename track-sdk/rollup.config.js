import resolve from '@rollup/plugin-node-resolve'
import commonjs from '@rollup/plugin-commonjs'
import babel from '@rollup/plugin-babel'
import terser from '@rollup/plugin-terser'

export default {
  input: 'src/index.js',
  output: [
    {
      file: 'dist/track-sdk.js',
      format: 'umd',
      name: 'TrackSDK',
      sourcemap: true
    },
    {
      file: 'dist/track-sdk.min.js',
      format: 'umd',
      name: 'TrackSDK',
      plugins: [terser()],
      sourcemap: true
    }
  ],
  plugins: [
    resolve(),
    commonjs(),
    babel({
      babelHelpers: 'bundled',
      exclude: 'node_modules/**'
    })
  ]
}
