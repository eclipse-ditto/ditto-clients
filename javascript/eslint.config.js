/*!
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import eslint from '@eslint/js';
import { defineConfig } from 'eslint/config';
import tseslint from 'typescript-eslint';
import yetAnotherLicenseHeader from 'eslint-plugin-yet-another-license-header';

const licenseHeaderPattern = /^\/\*!?\n \* Copyright \(c\) \d{4} Contributors to the Eclipse Foundation\n \*\n \* See the NOTICE file\(s\) distributed with this work for additional\n \* information regarding copyright ownership\.\n \*\n \* This program and the accompanying materials are made available under the\n \* terms of the Eclipse Public License 2\.0 which is available at\n \* http:\/\/www\.eclipse\.org\/legal\/epl-2\.0\n \*\n \* SPDX-License-Identifier: EPL-2\.0\n \*\/$/;

export default defineConfig(
  eslint.configs.recommended,
  tseslint.configs.recommended,
  {
    plugins: {
      'yet-another-license-header': yetAnotherLicenseHeader
    },
    files: ['lib/*/src/**/*.ts'],
    languageOptions: {
      parserOptions: {
        projectService: true
      }
    },
    rules: {
      'yet-another-license-header/header': ['error', {
        allowedHeaderPatterns: [licenseHeaderPattern],
        headerFile: '../legal/headers/license-header.txt'
      }],
      // Formatting rules (matching TSLint)
      'indent': ['error', 2, {
        SwitchCase: 1,
        FunctionDeclaration: { parameters: 'first' },
        FunctionExpression: { parameters: 'first' },
        CallExpression: { arguments: 'first' },
        ignoredNodes: [
          'FunctionDeclaration > .params',
          'FunctionExpression > .params',
          'CallExpression > .arguments',
          'ArrowFunctionExpression > .params',
          'NewExpression > .arguments'
        ]
      }],
      'quotes': ['error', 'single', { avoidEscape: true, allowTemplateLiterals: true }],
      'semi': ['error', 'always'],
      'comma-dangle': ['error', 'never'],
      'eol-last': ['error', 'always'],
      'no-trailing-spaces': 'error',
      'no-multiple-empty-lines': ['error', { max: 2 }],
      'max-len': ['error', { code: 140 }],
      'arrow-parens': ['error', 'as-needed'],
      'curly': 'error',

      // Best practices
      'eqeqeq': ['error', 'smart'],
      'no-eval': 'error',
      'no-caller': 'error',
      'no-bitwise': 'off',
      'no-debugger': 'error',
      'no-var': 'error',
      'prefer-const': 'error',
      'radix': 'error',
      'guard-for-in': 'error',
      'no-new-wrappers': 'error',
      'no-throw-literal': 'error',

      // Console restrictions (allow log, warn, error)
      'no-console': ['error', { allow: ['log', 'warn', 'error'] }],

      // TypeScript-specific
      'no-shadow': 'off',
      "@typescript-eslint/no-empty-object-type": "error",
      '@typescript-eslint/no-inferrable-types': ['error', { ignoreParameters: true }],
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', caughtErrors: 'none' }],
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-non-null-assertion': 'off',
      '@typescript-eslint/no-wrapper-object-types': 'off',
      '@typescript-eslint/no-require-imports': 'off'
    }
  },
  {
    // Tests - no projectService
    files: ['lib/*/tests/**/*.ts'],
    plugins: {
      'yet-another-license-header': yetAnotherLicenseHeader
    },
    rules: {
      'yet-another-license-header/header': ['error', {
        allowedHeaderPatterns: [licenseHeaderPattern],
        headerFile: '../legal/headers/license-header.txt'
      }],
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', caughtErrors: 'none', args: 'none' }],
      '@typescript-eslint/no-require-imports': 'off'
    }
  },
  {
    ignores: ['**/dist/**', '**/node_modules/**', '**/*.js', '**/*.d.ts']
  }
);
