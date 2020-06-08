def call(Map params) {
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
                        def releaseBranch = "release-${env.STAGE}"
                        
                        echo "Release Branch: ${releaseBranch}"
                        echo "TO_DEPLOY: ${params.TO_DEPLOY}"
                        slackSend color: "good", message: "*${params.IMAGE_NAME}* :whale: The '${params.TO_DEPLOY}' image has been tagged with '${params.STAGE}' in Docker Hub", channel: "#bot-testing"
                    }
                }
            }
        }
    }
}
