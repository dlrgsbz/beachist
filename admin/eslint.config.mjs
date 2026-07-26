import js from '@eslint/js'
import tsParser from '@typescript-eslint/parser'
import tsPlugin from '@typescript-eslint/eslint-plugin'
import eslintReact from '@eslint-react/eslint-plugin'
import globals from 'globals'

export default [
  {
    ignores: ['build/**', 'dist/**', 'node_modules/**', 'public/**', 'coverage/**', '.react-router/**', '**/*.config.js', '**/*.config.mjs'],
  },
  js.configs.recommended,
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaFeatures: { jsx: true },
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.jest,
      },
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
      '@eslint-react': eslintReact,
    },
    settings: {
      react: { version: 'detect' },
    },
    rules: {
      ...tsPlugin.configs.recommended.rules,
      ...eslintReact.configs.recommended.rules,
      'no-undef': 'off',
      '@eslint-react/rules-of-hooks': 'error',
      '@eslint-react/exhaustive-deps': 'warn',
    },
  },
]
