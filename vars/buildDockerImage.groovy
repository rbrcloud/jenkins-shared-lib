def call(Map config = [:]) {

    // Define variables for rbrcloud organization
    def registry = "ghcr.io"
    def orgName = "rbrcloud"

    def imageName = "${registry}/${orgName}/${config.serviceName}".toLowerCase()
    def imageTag = "${imageName}:${config.tag}"
    def latestTag = "${imageName}:latest"

    def dockerConfigDir = "${WORKSPACE}/.docker-${config.serviceName}"

    // Authenticate and push to the registry
    withCredentials([usernamePassword(credentialsId: "ghcr-token", usernameVariable: "GH_USERNAME", passwordVariable: "GH_TOKEN")]) {

        // Build the Docker image for rbrcloud organization
        echo "Building Docker image ${imageTag} for service ${config.serviceName}"
        sh """
            docker build \
            --build-arg GITHUB_USERNAME=${GH_USERNAME} \
            --build-arg GITHUB_TOKEN=${GH_TOKEN} \
            -t ${imageTag} .

            mkdir -p ${dockerConfigDir}
            export DOCKER_CONFIG=${dockerConfigDir}

            echo "Logging in to ${registry} with user ${GH_USERNAME}"
            echo ${GH_TOKEN} | docker login ${registry} -u ${GH_USERNAME} --password-stdin

            echo "Pushing Docker image ${imageTag} to ${registry}"
            docker push ${imageTag}

            if [ "${env.BRANCH_NAME}" = "main" ] || [ "${env.BRANCH_NAME}" = "master" ]; then
                echo "Branch name is ${env.BRANCH_NAME}, tagging as latest..."
                docker tag ${imageTag} ${latestTag}
                docker push ${latestTag}
            fi
        """
        sh "rm -rf ${dockerConfigDir}"
        echo "Successfully pushed ${imageTag} to ${registry}"
    }
}
