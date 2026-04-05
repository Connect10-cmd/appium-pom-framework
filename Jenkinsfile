pipeline {
    agent any

    parameters {
        string(name: 'PLATFORM',    defaultValue: 'android',               description: 'android or ios')
        string(name: 'SUITE_FILE',  defaultValue: 'testng-suite.xml',      description: 'TestNG suite XML filename')
        string(name: 'DEVICE',      defaultValue: 'Pixel_6_API_33',        description: 'Device name or UDID')
    }

    environment {
        APPIUM_HOME = '/usr/local/lib/node_modules/appium'
        JAVA_HOME   = '/usr/lib/jvm/java-17-openjdk-amd64'
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Start Appium Server') {
            steps {
                sh 'nohup appium &'
                sh 'sleep 5'
            }
        }

        stage('Run Tests') {
            steps {
                sh """
                    mvn test \\
                        -Dplatform=${params.PLATFORM} \\
                        -DsuiteFile=src/test/resources/${params.SUITE_FILE} \\
                        -Dmaven.test.failure.ignore=true
                """
            }
        }

        stage('Generate Allure Report') {
            steps {
                sh 'mvn allure:report'
            }
            post {
                always {
                    allure(
                        includeProperties: false,
                        jdk: '',
                        results: [[path: 'target/allure-results']]
                    )
                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'target/allure-report',
                        reportFiles          : 'index.html',
                        reportName           : 'Allure Report'
                    ])
                }
            }
        }
    }

    post {
        always { cleanWs() }
        failure {
            mail(
                to      : 'qa-team@yourcompany.com',
                subject : "FAILED: Appium Tests — ${params.PLATFORM} — Build #${BUILD_NUMBER}",
                body    : "Check the Allure report: ${BUILD_URL}allure"
            )
        }
    }
}
