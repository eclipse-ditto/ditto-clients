module.exports = function (config) {
  config.set({
    autoWatch: true,
    concurrency: Infinity,
    frameworks: ['jasmine', 'karma-typescript'],
    files: ['src/**/*.ts', 'tests/**/*.ts'],
    plugins: ['karma-jasmine', 'karma-typescript', 'karma-chrome-launcher', 'karma-junit-reporter'],
    browsers: ['ChromeHeadless'],
    preprocessors: {
      '**/*.ts': ['karma-typescript'],
    },
    reporters: ['progress', 'karma-typescript', 'junit'],
    coverageReporter: {
      includeAllSources: true,
      dir: 'coverage/',
      reporters: [
        { type: "html", subdir: "html" },
        { type: 'text-summary' }
      ]
    },
    junitReporter: {
      useBrowserName: false,
      outputDir: 'target/',
      outputFile: 'test-report.xml'
    },
    karmaTypescriptConfig: {
      compilerOptions: {
        target: 'es6'
      }
  }})
};
