pipeline {
    agent any
    environment {
        JAVA17 = '/usr/lib/jvm/java-17-openjdk-amd64'
    }

    stages {

        stage('Build') {
            steps {
                script {
                    sh "./gradlew clean -Dorg.gradle.java.home=${env.JAVA17}"
                    sh "./gradlew application:bootJar -Dorg.gradle.java.home=${env.JAVA17}"
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
                sh "./gradlew ui:test -Dorg.gradle.java.home=${env.JAVA17}"
            }
            post {
                always {
                  publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui/build/reports/tests/test/', reportFiles: 'index.html', reportName: 'Unit Tests Report', reportTitles: ''])
                }
            }
        }

        stage('Integration Test') {
            steps {
                echo 'Testing..'
                sh "./gradlew ui:integrationTest -Dorg.gradle.java.home=${env.JAVA17}"
            }
            post {
                always {
                   publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui/build/reports/tests/integrationTest/', reportFiles: 'index.html', reportName: 'Integration Tests Report', reportTitles: ''])
                }
            }
        }

        stage('Mutation Test') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Mutation Testing'
                    sh "./gradlew ui:piTest -Dorg.gradle.java.home=${env.JAVA17}"
                }
            }
            post {
                always {
                   publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui/build/reports/pitest/', reportFiles: 'index.html', reportName: 'Mutation Test Report', reportTitles: ''])
                }
            }
        }

        stage('Checkstyle') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Checkstyle..'
                    sh "./gradlew ui:checkstyleMain ui:checkstyleTest ui:checkstyleIntegrationTest ui:checkstyleTestFixtures -Dorg.gradle.java.home=${env.JAVA17}"
                }
            }
            post {
                always {
                  publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui/build/reports/checkstyle/', reportFiles: 'main.html,test.html,integrationTest.html,testFixtures.html', reportName: 'Checkstyle Report', reportTitles: ''])
                }
            }
        }

        stage('Pmd') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Pmd..'
                    sh "./gradlew ui:pmdMain ui:pmdTest ui:pmdIntegrationTest ui:pmdTestFixtures -Dorg.gradle.java.home=${env.JAVA17}"
                }
            }
            post {
                always {
                  publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui/build/reports/pmd/', reportFiles: 'main.html,test.html,integrationTest.html,testFixtures.html', reportName: 'Pmd Report', reportTitles: ''])
                }
            }
        }

        stage('Coverage') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Coverage..'
                    sh "./gradlew ui:jacocoTestCoverageVerification -Dorg.gradle.java.home=${env.JAVA17}"
                    sh "./gradlew ui:jacocoTestReport -Dorg.gradle.java.home=${env.JAVA17}"
                }
            }
            post {
                always {
                  jacoco(
                    execPattern: 'ui/build/jacoco/*.exec',
                    classPattern: 'ui/build/classes',
                    sourcePattern: 'ui/src/main/java',
                    exclusionPattern: 'ui/src/test*'
                  )
                }
            }
        }

        stage('Spotbugs') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Spotbugs..'
                    sh "./gradlew ui:spotbugsMain ui:spotbugsTest ui:spotbugsIntegrationTest ui:spotbugsTestFixtures -Dorg.gradle.java.home=${env.JAVA17}"
                }
            }
            post {
                always {
                  publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui/build/reports/spotbugs/', reportFiles: 'main.html,test.html,integrationTest.html,testFixtures.html', reportName: 'Spotbugs Report', reportTitles: ''])
                }
            }
        }

        stage('UI-Demo') {
            environment {
                TESTS_FILES = "tests/test/index.html,tests/integrationTest/index.html"
                CHECKSTYLE_FILES = "checkstyle/main.html,checkstyle/test.html,checkstyle/integrationTest.html,checkstyle/testFixtures.html"
                PMD_FILES = "pmd/main.html,pmd/test.html,pmd/integrationTest.html,pmd/testFixtures.html"
                SPOTBUGS_FILES = "spotbugs/main.html,spotbugs/test.html,spotbugs/integrationTest.html,spotbugs/testFixtures.html"
            }
            steps {
                echo 'UI-Demo All..'
                sh "./gradlew ui-demo:test ui-demo:integrationTest -Dorg.gradle.java.home=${env.JAVA17}"
                sh "./gradlew ui-demo:checkstyleMain ui-demo:checkstyleTest ui-demo:checkstyleIntegrationTest ui-demo:checkstyleTestFixtures -Dorg.gradle.java.home=${env.JAVA17}"
                sh "./gradlew ui-demo:pmdMain ui-demo:pmdTest ui-demo:pmdIntegrationTest ui-demo:pmdTestFixtures -Dorg.gradle.java.home=${env.JAVA17}"
                sh "./gradlew ui-demo:spotbugsMain ui-demo:spotbugsTest ui-demo:spotbugsIntegrationTest ui-demo:spotbugsTestFixtures -Dorg.gradle.java.home=${env.JAVA17}"
            }
            post {
                always {
                  publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'ui-demo/build/reports/', reportFiles: "${env.TESTS_FILES},${env.CHECKSTYLE_FILES},${env.PMD_FILES},${env.SPOTBUGS_FILES}", reportName: 'UI-Demo Report', reportTitles: ''])
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'production') {
                        sh "cp application/build/libs/application-boot.jar ./../app.jar"
                    } else {
                        sh "cp application/build/libs/application-boot.jar ./../app-test.jar"
                    }
                }
            }
        }
    }
}