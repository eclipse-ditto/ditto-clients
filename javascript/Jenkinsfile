/*
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

/**
 * Requires plugins:
 * 1. NodeJS ( Manage Jenkins - Global Tool Configuration - NodeJS installations - (NodeJS 10.16.3, Install automatically))
 * 2. Git Plugin (Manage Jenkins - Configure System - Git plugin (user.name = Eclipse Ditto committers; user.email = ditto-dev@eclipse.org))
 */
pipeline {
    agent any

    tools {
        nodejs "node"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout([
                            $class: 'GitSCM'
                    ])
                }
            }
        }

        stage('Install dependencies') {
            steps {
                dir('javascript') {
                    // uses npm ci to install fixed versions from package-lock.json
                    sh 'npm ci'
                }
            }
        }
        stage('Lint') {
            steps {
                dir('javascript') {
                    sh 'npm run lint'
                }
            }
        }
        stage('Build') {
            steps {
                dir('javascript') {
                    sh 'npm run build'
                }
            }
        }
        stage('Test') {
            steps {
                dir('javascript') {
                    sh 'npm test'
                    junit allowEmptyResults: true, testResults: '**/target/test-report.xml'
                }
            }
        }
    }
}
