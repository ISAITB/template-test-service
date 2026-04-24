#set($dollar = '$')
package ${package}.gitb.rest;

import com.gitb.model.core.*;
import com.gitb.model.ps.*;
import com.gitb.model.tr.TestResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring component that realises the processing service.
 */
@RestController
public class ProcessingServiceImpl implements ProcessingService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingServiceImpl.class);

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
    @GetMapping("/api/processing/getModuleDefinition")
    public GetModuleDefinitionResponse getModuleDefinition() {
        return new GetModuleDefinitionResponse();
    }

    /**
     * The purpose of the process operation is to execute one of the service's supported operations.
     * <p/>
     * What would typically take place here is as follows:
     * <ol>
     *    <li>Check that the requested operation is indeed supported by the service.</li>
     *    <li>For the requested operation collect and check the provided input parameters.</li>
     *    <li>Perform the requested operation and return the result to the Test Bed.</li>
     * </ol>
     *
     * @param processRequest The requested operation and input parameters.
     * @return The result.
     */
    @Override
    @PostMapping("/api/processing/process")
    public ProcessResponse process(@RequestBody ProcessRequest processRequest) {
        LOG.info("Received 'process' command from Test Bed for session [{}]", processRequest.getSessionId());
        ProcessResponse response = new ProcessResponse();
        response.setReport(utils.createReport(TestResultType.SUCCESS));
#if($addSampleImplementation.equalsIgnoreCase("Y"))
        String operation = processRequest.getOperation();
        if (operation == null) {
            throw new IllegalArgumentException("No processing operation provided");
        }
        String input = utils.getRequiredString(processRequest.getInput(), "input");
        String result = switch (operation) {
            case "uppercase" -> input.toUpperCase();
            case "lowercase" -> input.toLowerCase();
            default -> throw new IllegalArgumentException(String.format("Unexpected operation [%s].", operation));
        };
        response.getOutput().add(utils.createAnyContentSimple("output", result, ValueEmbeddingEnumeration.STRING));
        LOG.info("Completed operation [{}]. Input was [{}], output was [{}].", operation, input, result);
#end
        return response;
    }

    /**
     * The purpose of the beginTransaction operation is to begin a unique processing session.
     * <p/>
     * Transactions are used when processing services need to maintain state across several calls. If this is needed
     * then this implementation would generate a session identifier and record the session for subsequent 'process' calls.
     * <p/>
     * In the typical case where no state needs to be maintained, you can provide an empty implementation for this method.
     *
     * @param beginTransactionRequest Optional configuration parameters to consider when starting a processing transaction.
     * @return The response with the generated session ID for the processing transaction.
     */
    @Override
    @PostMapping("/api/processing/beginTransaction")
    public BeginTransactionResponse beginTransaction(@RequestBody BeginTransactionRequest beginTransactionRequest) {
        return new BeginTransactionResponse();
    }

    /**
     * The purpose of the endTransaction operation is to complete an ongoing processing session.
     * <p/>
     * The main actions to be taken as part of this operation are to remove the provided session identifier (if this
     * was being recorded to begin with), and to perform any custom cleanup tasks.
     *
     * @param parameters The identifier of the session to terminate.
     */
    @Override
    @PostMapping("/api/processing/endTransaction")
    public void endTransaction(@RequestBody BasicRequest parameters) {
    }

    /**
     * Get the OpenAPI specification for the processing service operations.
     *
     * @return The OpenAPI specification for the services.
     * @throws IOException If an error occurs reading the specification.
     */
    @GetMapping(path = "/api/processing", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getOpenApiSpec() throws IOException {
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rest/gitb_ps.json")) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

}

