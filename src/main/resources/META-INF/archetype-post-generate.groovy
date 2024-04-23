import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

def properties = request.properties
def projectPath = Paths.get(request.outputDirectory, request.artifactId)
String packageName = properties.get("package")
String packagePath = packageName.replace(".", "/")

boolean addSample = "Y".equalsIgnoreCase(properties.get("addSampleImplementation"))
boolean addMessagingService = "Y".equalsIgnoreCase(properties.get("addMessagingService"))
boolean addValidationService = "Y".equalsIgnoreCase(properties.get("addValidationService"))
boolean addProcessingService = "Y".equalsIgnoreCase(properties.get("addProcessingService"))

// Adapt messaging-only resources.
if (addMessagingService) {
    if (!addSample) {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputController.java"))
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web"))
        deleteFromFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/MessagingServiceImpl.java"), 109, 114)
    }
} else {
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/MessagingServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputController.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web"))
}
// Adapt validation-only resources.
if (addValidationService) {
    if (!addSample) {
        deleteFromFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ValidationServiceImpl.java"), 57, 90)
    }
} else {
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ValidationServiceImpl.java"))
}
// Adapt processing-only resources.
if (addProcessingService) {
    if (!addSample) {
        deleteFromFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ProcessingServiceImpl.java"), 57, 68)
    }
} else {
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ProcessingServiceImpl.java"))
}
// Adapt README.
if (addValidationService && !addSample) {
    deleteFromFile(projectPath.resolve("README.md"), 29, 32)
} else if (!addValidationService) {
    deleteFromFile(projectPath.resolve("README.md"), 27, 35)
}
if (addProcessingService && !addSample) {
    deleteFromFile(projectPath.resolve("README.md"), 22, 23)
} else if (!addProcessingService) {
    deleteFromFile(projectPath.resolve("README.md"), 20, 26)
}
if (addMessagingService && !addSample) {
    deleteFromFile(projectPath.resolve("README.md"), 10, 16)
} else if (!addMessagingService) {
    deleteFromFile(projectPath.resolve("README.md"), 8, 19)
}
// Adapt ServiceConfig.
if (!addValidationService) {
    deleteFromFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ServiceConfig.java"), 45, 58)
}
if (!addProcessingService) {
    deleteFromFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ServiceConfig.java"), 31, 44)
}
if (!addMessagingService) {
    deleteFromFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/ServiceConfig.java"), 17, 30)
}

/*
 * Function definitions.
 */

static def deleteFile(Path filePath) {
    Files.deleteIfExists(filePath)
}

static def deleteFromFile(Path filePath, int fromLine, int toLine) {
    def lineOffset = 2
    List<String> lines = Files.readAllLines filePath
    for (int line = toLine; line >= fromLine; line--) lines.remove(line - lineOffset)
    Files.delete(filePath)
    Files.write(filePath, lines, StandardOpenOption.CREATE)
}