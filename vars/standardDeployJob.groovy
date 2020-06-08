def call(Map calledParams) {
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
                        slackSend color: "good", message: "*${calledParams.imageName}* :whale: The '${calledParams.toDeploy}' image has been tagged with '${calledParams.stage}' in Docker Hub", channel: "#bot-testing"
                    }
                }
            }
        }
    }
}
