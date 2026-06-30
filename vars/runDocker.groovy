def call(boolean isEnabled, String imageName, String imageTag, boolean shouldPush, String credsId) {
    if (!isEnabled) {
        echo "Docker engine is disabled. Skipping step."
        return
    }

    if (!fileExists('Dockerfile')) {
        error "Dockerfile is missing from root directory! Cannot build image."
    }

    def fullImageName = "${imageName}:${imageTag}"
    echo "Building Docker image: ${fullImageName}"
    sh "docker build -t ${fullImageName} ."

    if (shouldPush) {
        echo "Logging into Docker Registry and pushing image..."
        withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
            sh "echo '${PASS}' | docker login -u '${USER}' --password-stdin"
            sh "docker push ${fullImageName}"
            sh "docker logout"
        }
    }
}