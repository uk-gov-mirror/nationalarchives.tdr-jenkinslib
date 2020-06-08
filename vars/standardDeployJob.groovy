def call(Map params) {
    pipeline {
        agent {
            label "master"
        }
        parameters {
            choice(name: "STAGE", choices: ["intg", "staging", "prod"], description: "The stage you are building the auth server for")
        }
        stages {
            stage("Docker") {
                agent {
                    label "master"
                }
                steps {
                    script {
                        echo "STAGE: ${params.STAGE}"
                        slackSend color: "good", message: "*${params.imageName}* :whale: The '${params.toDeploy}' image has been tagged with '${params.stage}' in Docker Hub", channel: "#bot-testing"
                    }
                }
            }
        }
    }
}
