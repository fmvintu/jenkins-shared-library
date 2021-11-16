def call(Map pipelineParams) {
    pipeline {
        agent any

        tools {
            maven 'M3'
        }

        environment {
            POM_ARTIFACT = readMavenPom().getProperties().getProperty('artifactId')  
            MULE_VERSION = '4.3.0'
            DEPLOY_CREDS = credentials('CH_CLIENT_CREDENTIALS')

            ENVIRONMENT = pipelineParams.env
            WORKER = pipelineParams.worker
            API_ID = pipelineParams.apiId
            API_CLIENT_ID = pipelineParams.environmentClientId
            API_CLIENT_ID = pipelineParams.environmentClientSecret
        }

        stages {
            stage('Build') {
                steps {
                    sh "mvn -B -U -e -V clean -DskipTests package"
                }
            }

            stage('Test') {
                steps {
                    mvn test
                }
            }

            stage('Publish artifact') {
                steps {
                    mvn deploy
                }
            }

            stage("Deploy to Environment") {
                environment {
                    ENVIRONMENT = 'dev'
                    APP_NAME = 'test-api'
                }

                steps {
                        sh 'mvn -U -V -e -B -DskipTests deploy -DmuleDeploy -Dapp.client_id="${CH_CLIENT_CREDENTIALS_USR}" -Dapp.client_secret="${CH_CLIENT_CREDENTIALS_PSW}" -Ddeployment.env=dev -Dencrypt.key=secure12345 -Dap.api.id=17413469 -Dap.client_id=c435d784c1154e30898eded3b8b4a50a -Dap.client_secret=d1cAd05E13844985a7Ad4E821B3406e0'
                }
            }
        }
    }
}