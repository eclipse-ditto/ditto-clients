/*!
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

import typescript from 'rollup-plugin-typescript2';
import pkg from './package.json';

export default {
    input: 'dist/index.ts',
    output: [
        {
            file: pkg.main,
            format: 'cjs',
            sourcemap: true
        },
        {
            file: pkg.module,
            format: 'es',
            sourcemap: true
        },
        {
            file: pkg.browser,
            format: 'iife',
            name: 'EclipseDittoJavascriptClientApi',
            sourcemap: true
        }
    ],
    external: [
        ...Object.keys(pkg.dependencies || {})
    ],
    plugins: [
        typescript({
                       typescript: require('typescript'),
                       tsconfigDefaults: {
                           compilerOptions: {
                               // declarations aren't needed because compiling already added them
                               declaration: false,
                               declarationMap: false
                           }
                       }
                   }),
    ]
};
