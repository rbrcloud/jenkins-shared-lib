def call(Map config = [:]) {

    // Define variables for rbrcloud organization
    def orgName = "rbrcloud"
    def registry = "ghcr.io"

    def imageName = "${registry}/${orgName}/${config.serviceName}".toLowerCase()
    def imageTag = "${imageName}:${config.tag}"

    // Build the Docker image for rbrcloud organization
    echo "Building Docker image ${imageTag} for service ${config.serviceName}"
    sh "docker build -t ${imageTag} ."

    // Authenticate and push to the registry
    withCredentials([usernamePassword(credentialsId: "ghcr-token", usernameVariable: "GH_USERNAME", passwordVariable: "GH_TOKEN")]) {
        echo "Logging in to ${registry} with user ${GH_USERNAME}"
        sh "echo ${GH_TOKEN} | docker login ${registry} -u ${GH_USERNAME} --password-stdin"

        echo "Pushing Docker image ${imageTag} to ${registry}"
        sh "docker push ${imageTag}"

        echo "Successfully pushed ${imageTag} to ${registry}"
    }
}