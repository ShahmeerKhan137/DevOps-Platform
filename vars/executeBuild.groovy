def call(String buildTool) {
    echo "Running build process for: ${buildTool}"
    
    if (buildTool == 'maven') {
        sh 'mvn clean package -DskipTests'
    } else if (buildTool == 'gradle') {
        sh './gradlew build -x test || gradle build -x test'
    } else if (buildTool == 'nodejs') {
        sh 'npm install'
        sh 'npm run build --if-present'
    } else if (buildTool == 'python') {
        sh 'pip install -r requirements.txt || pip3 install -r requirements.txt'
    }
}