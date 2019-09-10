module.exports = function (config) {
  config.set({
    autoWatch: true,
    concurrency: Infinity,
    frameworks: ['jasmine', 'karma-typescript'],
    files: ['src/**/*.ts', 'tests/**/*.ts'],
    plugins: ['karma-jasmine', 'karma-typescript', 'karma-chrome-launcher', 'karma-firefox-launcher', 'karma-edge-launcher'],
    browsers: ['ChromeHeadless', 'Firefox', 'Edge', 'Chrome'],
    preprocessors: {
      '**/*.ts': ['karma-typescript']
    },
    reporters: ['progress', 'karma-typescript'],
    coverageReporter: {
      includeAllSources: true,
      dir: 'coverage/',
      reporters: [
        { type: "html", subdir: "html" },
        { type: 'text-summary' }
      ]
    },
    karmaTypescriptConfig: {
      compilerOptions: {
        target: 'es6'
      }
  }})
};
