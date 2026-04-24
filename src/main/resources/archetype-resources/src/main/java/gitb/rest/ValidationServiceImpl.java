#set($dollar = '$')
package ${package}.gitb.rest;

#if($addSampleImplementation.equalsIgnoreCase("Y"))
import com.gitb.model.core.*;
#end
import com.gitb.model.vs.*;
import com.gitb.model.tr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring component that realises the validation service.
 */
@RestController
public class ValidationServiceImpl implements ValidationService {

    /** Logger. **/
    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);

    @Autowired
    private Utils utils = null;

    /**
     * The purpose of the getModuleDefinition call is to inform its caller on how the service is supposed to be called.
     * <p/>
     * Note that defining the implementation of this service is optional, and can be empty unless you plan to publish
     * the service for use by third parties (in which case it serves as documentation on its expected inputs and outputs).
     *
     * @return The response.
     */
    @Override
    @GetMapping("/api/validation/getModuleDefinition")
    public GetModuleDefinitionResponse getModuleDefinition() {
        return new GetModuleDefinitionResponse();
    }

    /**
     * The validate operation is called to validate the input and produce a validation report.
     * <p/>
     * The expected input is described for the service's client through the getModuleDefinition call.
     *
     * @param parameters The input parameters and configuration for the validation.
     * @return The response containing the validation report.
     */
    @Override
    @PostMapping("/api/validation/validate")
    public ValidationResponse validate(@RequestBody ValidateRequest parameters) {
        LOG.info("Received 'validate' command from Test Bed for session [{}]", parameters.getSessionId());
        ValidationResponse result = new ValidationResponse();
        TAR report = utils.createReport(TestResultType.SUCCESS);
#if($addSampleImplementation.equalsIgnoreCase("Y"))
        // First extract the parameters and check to see if they are as expected.
        String providedText = utils.getRequiredString(parameters.getInput(), "text");
        String expectedText = utils.getRequiredString(parameters.getInput(), "expected");
        boolean mismatchIsError = Boolean.parseBoolean(utils.getOptionalString(parameters.getInput(), "mismatchIsError").orElse("true"));
        // Now do the validation.
        report.setContext(AnyContent.builder()
                        .withItem(AnyContent.builder()
                                .withName("text")
                                .withValue(providedText)
                                .build()
                        )
                        .withItem(AnyContent.builder()
                                .withName("expected")
                                .withValue(expectedText)
                                .build()
                        )
                        .build()
        );
        int warnings = 0;
        int errors = 0;
        if (!providedText.equals(expectedText)) {
            if (mismatchIsError) {
                errors += 1;
                report.getItems().add(ReportItem.builder().withDescription("The texts do not match.").withLevel(SeverityLevel.ERROR).build());
            } else {
                warnings += 1;
                report.getItems().add(ReportItem.builder().withDescription("The texts do not match.").withLevel(SeverityLevel.WARNING).build());
            }
            if (providedText.equalsIgnoreCase(expectedText)) {
                report.getItems().add(ReportItem.builder().withDescription("The texts match but only when ignoring case.").withLevel(SeverityLevel.INFO).build());
            }
        }
        if (errors > 0) {
            report.setResult(TestResultType.FAILURE);
        } else if (warnings > 0) {
            report.setResult(TestResultType.WARNING);
        }
        // Return the report.
#end
        result.setReport(report);
        return result;
    }

    /**
     * Get the OpenAPI specification for the validation service operations.
     *
     * @return The OpenAPI specification for the services.
     * @throws IOException If an error occurs reading the specification.
     */
    @GetMapping(path = "/api/validation", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getOpenApiSpec() throws IOException {
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rest/gitb_vs.json")) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

}

