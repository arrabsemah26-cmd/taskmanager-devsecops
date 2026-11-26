pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
        PATH = "${JAVA_HOME}/bin:/usr/local/src/maven/bin:${env.PATH}"
        IMAGE_NAME = "taskmanager"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn -B clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest .'
            }
        }

        stage('Trivy Scan') {
            steps {
                // Report: all HIGH and CRITICAL findings that have a fix available
                sh 'trivy image --scanners vuln --skip-db-update --skip-java-db-update --timeout 15m --severity HIGH,CRITICAL --ignore-unfixed --format table -o trivy-report.txt ${IMAGE_NAME}:${BUILD_NUMBER}'
                // Gate: fail the pipeline if a fixable CRITICAL vulnerability exists
                sh 'trivy image --scanners vuln --skip-db-update --skip-java-db-update --timeout 15m --severity CRITICAL --ignore-unfixed --exit-code 1 ${IMAGE_NAME}:${BUILD_NUMBER}'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
                }
            }
        }
    }
}
