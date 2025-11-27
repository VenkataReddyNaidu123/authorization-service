pipeline {
    agent any

    environment {
        APP_VERSION = "0.1.0"
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                // explicit checkout of the commit that triggered the build
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // use Windows batch commands
                bat 'mvn -B -DskipTests clean package'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    // archive JUnit XML reports (JUnit plugin required)
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        success {
            echo "SUCCESS: ${env.BUILD_NUMBER}"
        }
        failure {
            echo "FAILED: ${env.BUILD_NUMBER}"
        }
        always {
            echo "Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} finished at ${new Date()}"
        }
    }
}
