import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

def properties = request.properties
def projectPath = Paths.get(request.outputDirectory, request.artifactId)
String packageName = properties.get("package")
String packagePath = packageName.replace(".", "/")

boolean restApi = "R".equalsIgnoreCase(properties.get("useRestApiOrSoapApi"))
boolean addSample = "Y".equalsIgnoreCase(properties.get("addSampleImplementation"))
boolean addMessagingService = "Y".equalsIgnoreCase(properties.get("addMessagingService"))
boolean addValidationService = "Y".equalsIgnoreCase(properties.get("addValidationService"))
boolean addProcessingService = "Y".equalsIgnoreCase(properties.get("addProcessingService"))

// Enable REST or SOAP - remove unused protocol's dependencies and implementation classes.
if (restApi) {
    // Remove all SOAP-specific classes.
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/MessagingServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/ValidationServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/ProcessingServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/ServiceConfig.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/TestBedNotifier.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/StateManager.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/Utils.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/ProxyInfo.java"))
    deleteDir(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap"))
    // Use the REST-flavoured controller; remove the SOAP one.
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputControllerSoap.java"))
    renameFile(
        projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputControllerRest.java"),
        projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputController.java")
    )
} else {
    // Remove all REST-specific classes.
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/MessagingServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/ValidationServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/ProcessingServiceImpl.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/TestBedNotifier.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/StateManager.java"))
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/Utils.java"))
    deleteDir(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest"))
    // Use the SOAP-flavoured controller; remove the REST one.
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputControllerRest.java"))
    renameFile(
        projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputControllerSoap.java"),
        projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputController.java")
    )
}

// Adapt messaging-only resources.
if (addMessagingService) {
    if (!addSample) {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputController.java"))
        deleteDir(projectPath.resolve("src/main/java/" + packagePath + "/web"))
    }
} else {
    if (restApi) {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/MessagingServiceImpl.java"))
    } else {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/MessagingServiceImpl.java"))
    }
    deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/web/UserInputController.java"))
    deleteDir(projectPath.resolve("src/main/java/" + packagePath + "/web"))
}
// Adapt validation-only resources.
if (!addValidationService) {
    if (restApi) {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/ValidationServiceImpl.java"))
    } else {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/ValidationServiceImpl.java"))
    }
}
// Adapt processing-only resources.
if (!addProcessingService) {
    if (restApi) {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/rest/ProcessingServiceImpl.java"))
    } else {
        deleteFile(projectPath.resolve("src/main/java/" + packagePath + "/gitb/soap/ProcessingServiceImpl.java"))
    }
}

/*
 * Function definitions.
 */

static def deleteFile(Path filePath) {
    Files.deleteIfExists(filePath)
}

static def deleteDir(Path dirPath) {
    if (Files.exists(dirPath)) {
        dirPath.toFile().deleteDir()
    }
}

static def renameFile(Path from, Path to) {
    Files.move(from, to)
}
