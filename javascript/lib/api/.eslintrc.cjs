module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module'
  },
  plugins: [
    '@typescript-eslint'
  ],
  extends: [
    'eslint:recommended'
  ],
  rules: {
    // Disable base rules in favor of TypeScript versions
    'no-unused-vars': 'off',
    'no-undef': 'off', // TypeScript handles undefined variables

    // Basic TypeScript rules
    '@typescript-eslint/no-unused-vars': ['error', {
      argsIgnorePattern: '^_',
      varsIgnorePattern: '^_'
    }],
    '@typescript-eslint/no-explicit-any': 'warn',
    '@typescript-eslint/no-inferrable-types': ['error', { ignoreParameters: true }],

    // Style rules
    'indent': 'off',
    '@typescript-eslint/indent': ['error', 4],
    'quotes': 'off',
    '@typescript-eslint/quotes': ['error', 'single'],
    'semi': 'off',
    '@typescript-eslint/semi': ['error', 'always'],
    'max-len': ['error', { code: 140 }],
    'no-trailing-spaces': 'error',
    'eol-last': 'error',

    // General rules
    'no-console': ['error', { allow: ['warn', 'error', 'log', 'debug'] }],
    'no-debugger': 'error',
    'prefer-const': 'error',
    'curly': 'error'
  },
  env: {
    node: true,
    browser: true,
    es6: true,
    jest: true
  },
  ignorePatterns: [
    'dist/',
    'node_modules/',
    '*.js',
    '*.d.ts'
  ]
};