def call() {
    echo "Checking application configuration files..."
    
    if (fileExists('pom.xml')) {
        return 'maven'
    } else if (fileExists('build.gradle') || fileExists('build.gradle.kts')) {
        return 'gradle'
    } else if (fileExists('package.json')) {
        return 'nodejs'
    } else if (fileExists('requirements.txt') || fileExists('pyproject.toml')) {
        return 'python'
    } else {
        error "No matching build file found! Unsupported application stack."
    }
}