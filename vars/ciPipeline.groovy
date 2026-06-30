def call() {
    pipeline {
        agent any

        stages {
            stage('Setup & Configuration') {
                steps {
                    script {
                        echo "Loading pipeline configurations..."
                        if (!fileExists('pipeline-config.yaml')) {
                            error "Missing pipeline-config.yaml from application root!"
                        }
                        
                        // Parse YAML properties directly into global environment strings
                        def config = readYaml file: 'pipeline-config.yaml'
                        
                        env.RUN_DOCKER  = (config.docker != null && config.docker.enabled != null) ? config.docker.enabled.toString() : 'false'
                        env.DOCKER_IMG  = (config.docker != null && config.docker.image != null) ? config.docker.image.toString() : ''
                        env.DOCKER_TAG  = (config.docker != null && config.docker.tag != null) ? config.docker.tag.toString() : 'latest'
                        env.PUSH_DOCKER = (config.docker != null && config.docker.push != null) ? config.docker.push.toString() : 'false'
                        env.RUN_CLEAN   = config.cleanup != null ? config.cleanup.toString() : 'true'
                    }
                }
            }

            stage('Auto-Detect Tech Stack') {
                steps {
                    script {
                        env.APP_STACK = detectStack()
                        echo "Detected Application Framework: ${env.APP_STACK}"
                    }
                }
            }

            stage('Application Build') {
                steps {
                    script {
                        executeBuild(env.APP_STACK)
                    }
                }
            }

            stage('Container Engine') {
                when { expression { return env.RUN_DOCKER.toBoolean() } }
                steps {
                    script {
                        runDocker(
                            env.RUN_DOCKER.toBoolean(),
                            env.DOCKER_IMG,
                            env.DOCKER_TAG,
                            env.PUSH_DOCKER.toBoolean(),
                            'docker-hub-credentials'
                        )
                    }
                }
            }
        }

        post {
            always {
                script {
                    if (env.RUN_CLEAN.toBoolean()) {
                        echo "Performing workspace cleanup tasks..."
                        if (env.RUN_DOCKER.toBoolean()) {
                            sh "docker rmi ${env.DOCKER_IMG}:${env.DOCKER_TAG} --force || true"
                        }
                        cleanWs()
                    }
                }
            }
        }
    }
}