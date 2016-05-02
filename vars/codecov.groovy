def call(credsName=null) {
  if (!credsName) {
    def path = scm.repositories[0].URIs[0].path
    def repoOwner = path.split('/')[0]
    def repoName = path.split('/')[-1].replaceAll(/\.git$/, '')
    credsName = repoName
    if (repoOwner && repoOwner != 'docker') {
      credsName = "${repoOwner}-${credsName}"
    }
  }

  def ghprbPullId = ""
  def branchName = env.BRANCH_NAME
  if (branchName =~ /^PR-\d+$/) {
    ghprbPullId = branchName[3..-1]
    branchName = ""
  }

  // Set some env variables so codecov detection script works correctly
  withEnv(["GIT_BRANCH=${branchName}", "GIT_COMMIT=${gitCommit()}", "ghprbPullId=${ghprbPullId}"]) {
    withCredentials([[$class: 'StringBinding', credentialsId: "${credsName}.codecov-token", variable: 'CODECOV_TOKEN']]) {
      sh "bash <(curl -s https://codecov.io/bash) -t \$CODECOV_TOKEN"
    }
  }
}
