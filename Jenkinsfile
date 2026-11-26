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

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=taskmanager -Dsonar.projectName=taskmanager'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Dependency-Check') {
            steps {
                timeout(time: 60, unit: 'MINUTES') {
                    withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_KEY')]) {
                        // Policy: fail the build if a dependency has a CVE with CVSS >= 9 (CRITICAL)
                        sh 'mvn -B org.owasp:dependency-check-maven:12.1.1:check -DnvdApiKey=$NVD_KEY -DdataDirectory=/var/lib/jenkins/dependency-check-data -DautoUpdate=true -DfailBuildOnCVSS=9 -Dformat=HTML -DossindexAnalyzerEnabled=false'
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/dependency-check-report.html', allowEmptyArchive: true
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
                sh 'trivy image --scanners vuln --skip-db-update --skip-java-db-update --timeout 15m --severity HIGH,CRITICAL --ignore-unfixed --format table -o trivy-report.txt ${IMAGE_NAME}:${BUILD_NUMBER}'
                sh 'trivy image --scanners vuln --skip-db-update --skip-java-db-update --timeout 15m --severity CRITICAL --ignore-unfixed --exit-code 1 ${IMAGE_NAME}:${BUILD_NUMBER}'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
                }
            }
        }

        stage('Publish to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_TOKEN')]) {
                    sh '''
                        echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin
                        docker tag ${IMAGE_NAME}:${BUILD_NUMBER} $DH_USER/${IMAGE_NAME}:${BUILD_NUMBER}
                        docker tag ${IMAGE_NAME}:${BUILD_NUMBER} $DH_USER/${IMAGE_NAME}:latest
                        docker push $DH_USER/${IMAGE_NAME}:${BUILD_NUMBER}
                        docker push $DH_USER/${IMAGE_NAME}:latest
                        docker logout
                    '''
                }
            }
        }
        stage('Deploy to Minikube') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_TOKEN')]) {
                    sh '''
                        kubectl apply -f k8s/service.yaml
                        sed "s|image: .*|image: $DH_USER/${IMAGE_NAME}:${BUILD_NUMBER}|" k8s/deployment.yaml | kubectl apply -f -
                        kubectl rollout status deployment/taskmanager --timeout=300s
                        kubectl get pods
                    '''
                }
            }
        }
    }
}
