def call(Map params = [:]) {

    def service = params.service ?: error("Service name is required")
    def imageTag = params.imageTag ?: error("Image tag is required")

    echo "Running API tests for service ${service} with image tag ${imageTag}"
    build job: 'Apex API Automation Runner',
            parameters: [
                    string(name: 'TARGET_SERVICE', value: service),
                    string(name: 'IMAGE_TAG', value: imageTag),
            ],
            wait: true, // Wait for the API tests job to complete before proceeding
            propagate: true // Propagate the result of the API tests job to this pipeline
}