def call(Map config) {
  library("tdr-jenkinslib")

  def versionBumpBranch = "version-bump-${BUILD_NUMBER}-${config.version}"
  def pullRequestTitlePrefix = "Jenkins generated version bump from build number"

  pipeline {
    agent none

    parameters {
      choice(name: "VERSION", choices: ["patch", "minor", "major"], description: "The version of the library to be deployed")
    }
    stages {
      stage("Publish package") {
        agent {
          ecs {
            inheritFrom "npm"
          }
        }
        when {
          //Only trigger version bump for non-version bump commits
          //Prevents infinite loop of creating pull requests for version bumps
          //Npm merge version bump commit messages in format: 'Jenkins generated version bump from build number 000'
          expression {
            return !(config.currentGitCommit =~ /$pullRequestTitlePrefix (\d+)/)
          }
        }
        stages {
          stage("Create and push version bump GitHub branch") {
            steps {
              script {
                tdr.configureJenkinsGitUser()
              }

              sh "git checkout -b ${versionBumpBranch}"
            }
          }
          stage("Update npm version") {
            steps {
              sh 'npm ci'
              sh "npm run $config.buildCommand"

              //commits change to the branch
              sshagent(['github-jenkins']) {
                sh "npm version ${config.version}"
              }

              withCredentials([string(credentialsId: 'npm-login', variable: 'LOGIN_TOKEN')]) {
                sh "npm config set //registry.npmjs.org/:_authToken=$LOGIN_TOKEN"
                sh 'npm publish --access public'
              }
            }
          }
          stage("Push version bump GitHub branch") {
            steps {
              script {
                tdr.pushGitHubBranch(versionBumpBranch)
              }
            }
          }
          stage("Create version bump pull request") {
            steps {
              script {
                tdr.createGitHubPullRequest(
                  pullRequestTitle: "${pullRequestTitlePrefix} ${BUILD_NUMBER}",
                  buildUrl: env.BUILD_URL,
                  repo: "tdr-file-metadata",
                  branchToMergeTo: "master",
                  branchToMerge: versionBumpBranch
                )
              }
            }
          }
        }
      }
    }
  }
}
