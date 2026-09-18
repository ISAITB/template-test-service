#set($dollar = '$')
package ${package}.gitb.rest;

import com.gitb.model.core.*;
import com.gitb.model.ms.*;
import com.gitb.model.tr.TestResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring component that realises the messaging service.
 */
@RestController
public class MessagingServiceImpl implements MessagingService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MessagingServiceImpl.class);

    @Autowired
    private StateManager stateManager = null;
    @Autowired
    private HttpServletRequest request = null;
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
    @GetMapping("/api/messaging/getModuleDefinition")
    public GetModuleDefinitionResponse getModuleDefinition() {
        return new GetModuleDefinitionResponse();
    }

    /**
     * The initiate operation is called by the Test Bed when a new test session is being prepared.
     * <p/>
     * This call expects from the service to do the following:
     * <ul>
     *     <li>Record the session identifier to keep track of messages linked to the test session.</li>
     *     <li>Process, if needed, the configuration provided by the SUT.</li>
     *     <li>Return, if needed, configuration to be displayed to the user for the SUT actor.</li>
     * </ul>
     *
     * @param parameters The actor configuration provided by the SUT.
     * @return The session ID and any generated configuration to display for the SUT.
     */
    @Override
    @PostMapping("/api/messaging/initiate")
    public InitiateResponse initiate(@RequestBody InitiateRequest parameters) {
        // Get the ReplyTo address for the Test Bed callbacks based on WS-Addressing.
        String replyToAddress = utils.getReplyToAddressFromHeaders(request).orElseThrow();
        // Get the test session ID to use for tracking session state.
        String sessionId = utils.getTestSessionIdFromHeaders(request).orElseThrow();
        stateManager.createSession(sessionId, replyToAddress);
        LOG.info("Initiated a new session [{}] with callback address [{}]", sessionId, replyToAddress);
        return new InitiateResponse();
    }

    /**
     * The receive operation is called when the Test Bed is expecting to receive a message.
     * <p/>
     * The goal here is to be informed by the Test Bed on the characteristics of the message we are expecting to receive.
     * These characteristics would need to be recorded as part of this operation in the service's session state so
     * that incoming messages can be matched against them. Once the expected message is received, the TestBedNotifier
     * can then be used to ping the Test Bed.
     * <p/>
     * Besides the expected message's characteristics, the service should also record:
     * <ul>
     *     <li>The test session identifier.</li>
     *     <li>The call identifier (the identifier of the relevant 'receive' step that resulted in this call).</li>
     *     <li>The callback address of the Test Bed (this could also be fixed as a configuration property).</li>
     * </ul>
     *
     * @param parameters The input parameters to consider (if any).
     */
    @Override
    @PostMapping("/api/messaging/receive")
    public void receive(@RequestBody ReceiveRequest parameters) {
        LOG.info("Received 'receive' command from Test Bed for session [{}]", parameters.getSessionId());
    }

    /**
     * The send operation is called when the Test Bed wants to send a message through this service.
     * <p/>
     * This is the point where input is received for the call that this service needs to translate into an actual
     * communication. This communication would be specific to a communication protocol or a separate system's API.
     * <p/>
     * The result of the operation is typically an empty success or failure report depending on whether or not the
     * communication was successful. This report could however include additional information that would be reported
     * back to the Test Bed.
     *
     * @param parameters The input parameters and configuration to consider for the send operation.
     * @return A status report for the call that will be returned to the Test Bed.
     */
    @Override
    @PostMapping("/api/messaging/send")
    public SendResponse send(@RequestBody SendRequest parameters) {
        LOG.info("Received 'send' command from Test Bed for session [{}]", parameters.getSessionId());
#if($addSampleImplementation.equalsIgnoreCase("Y"))
        /*
        At this point we would expect the actual communication or simulation to take place. In this sample implementation
        we simply log the message received from the Test Bed.
         */
        String messageToSend = utils.getRequiredString(parameters.getInput(), "messageToSend");
        LOG.info("The message to send is [{}]", messageToSend);
#end
        return SendResponse.builder()
                .withReport(utils.createReport(TestResultType.SUCCESS))
                .build();
    }

    /**
     * The beginTransaction operation is called by the Test Bed with a transaction starts.
     * <p/>
     * Often there is no need to take any action here but it could be interesting to do so if you need specific
     * actions per transaction.
     *
     * @param parameters The transaction configuration.
     */
    @Override
    @PostMapping("/api/messaging/beginTransaction")
    public void beginTransaction(@RequestBody BeginTransactionRequest parameters) {
        LOG.info("Transaction starting for session [{}]", parameters.getSessionId());
    }

    /**
     * The endTransaction operation is the counterpart of the beginTransaction and is called when the transaction terminates.
     *
     * @param parameters The session ID this transaction related to.
     */
    @Override
    @PostMapping("/api/messaging/endTransaction")
    public void endTransaction(@RequestBody BasicRequest parameters) {
        LOG.info("Transaction ending for session [{}]", parameters.getSessionId());
    }

    /**
     * The finalize operation is called by the Test Bed when a test session completes.
     * <p/>
     * A typical action that needs to take place here is the cleanup of any resources that were specific to the session
     * in question. This would typically involve the state recorded for the session.
     *
     * @param parameters The session ID that completed.
     */
    @Override
    @PostMapping("/api/messaging/finalize")
    public void finalize(@RequestBody FinalizeRequest parameters) {
        LOG.info("Finalising session [{}]", parameters.getSessionId());
        // Cleanup in-memory state for the completed session.
        stateManager.destroySession(parameters.getSessionId());
    }

    /**
     * Get the OpenAPI specification for the messaging service operations.
     *
     * @return The OpenAPI specification for the services.
     * @throws IOException If an error occurs reading the specification.
     */
    @GetMapping(path = "/api/messaging", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getOpenApiSpec() throws IOException {
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rest/gitb_ms.json")) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

}

