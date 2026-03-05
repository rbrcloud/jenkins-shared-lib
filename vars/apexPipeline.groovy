def call(Map config = [:]) {
    pipeline {
        agent any

        tools {
            jdk 'sdkman-java'
            maven 'maven_3.9.12'
        }

        environment {
            DOCKER_REGISTRY = "ghcr.io"
            SERVICE_NAME = "${config.serviceName}"
            IMAGE_TAG = "v-${BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
        }

        stages {
            stage("Initialize") {
                steps {
                    echo "Starting build for ${SERVICE_NAME} with image tag ${IMAGE_TAG}"
                    checkout scm
                }
            }

            stage("Build and Test") {
                steps {
                    sh "./mvnw clean package"
                }
            }

            stage("Static Analysis") {
                steps {
                    echo "Running SonarQube analysis for ${SERVICE_NAME}"
                }
            }

            stage("Build Docker Image & Push") {
                steps {
                    buildDockerImage(serviceName: env.SERVICE_NAME, tag: env.IMAGE_TAG)
                }
            }

            stage("Trigger API tests") {
                when {
                    changeRequest()
                }
                steps {
                    echo "Triggering API tests for ${SERVICE_NAME} with image tag ${IMAGE_TAG}"
                    runApiTests(
                            service: env.SERVICE_NAME,
                            imageTag: env.IMAGE_TAG,
                    )
                }
            }
        }
    }
    post {
        always {
            echo "Cleaning up workspace for ${SERVICE_NAME}"
            cleanWs()
        }
        failure {
            echo "Build failed for ${SERVICE_NAME}. Please check the logs for details."
        }
    }
}