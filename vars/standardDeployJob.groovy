def call(Map config) {
    pipeline {
        agent {
            label "master"
        }
        parameters {
            choice(name: "STAGE", choices: ["intg", "staging"], description: "The stage you are building the auth server for")
            string(name: "TO_DEPLOY", description: "The git tag, branch or commit reference to deploy, e.g. 'v123'")
        }
        stages {
            stage("Docker") {
                agent {
                    label "master"
                }
                steps {
                    script {
                        echo "STAGE: ${params.STAGE}"
                        echo "TO_DEPLOY: ${params.TO_DEPLOY}"
                        slackSend color: "good", message: "*${config.imageName}* :whale: The '${params.TO_DEPLOY}' image has been tagged with '${params.STAGE}' in Docker Hub", channel: "#bot-testing"
                    }
                }
            }
        }
    }
}
