package ${package}.gitb.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitb.model.core.LogLevel;
import com.gitb.model.core.LogRequest;
import com.gitb.model.ms.NotifyForMessageRequest;
import com.gitb.model.tr.TAR;
import com.gitb.model.tr.TestResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Component used to notify the Test Bed of received queries.
 * <p/>
 * The main reason of defining this as a separate component is to facilitate making these notifications asynchronous
 * (see the notifyTestBed method that is marked as async).
 */
@Component
public class TestBedNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(TestBedNotifier.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private Utils utils = null;

    /**
     * Send a log message to the Test Bed at a given severity level.
     *
     * @param sessionId The session identifier.
     * @param callbackAddress The Test Bed's callback address to use.
     * @param message The log message.
     * @param level The severity level.
     */
    @Async
    public void sendLogMessage(String sessionId, String callbackAddress, String message, LogLevel level) {
        try {
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(callbackAddress + "/log"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(
                            LogRequest.builder()
                                    .withSessionId(sessionId)
                                    .withMessage(message)
                                    .withLevel(level)
                                    .build()
                    )))
                    .build();
            try (var client = HttpClient.newBuilder().build()) {
                var response = client.send(httpRequest, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() >= 400) {
                    LOG.warn("Failed to send log message. Status code: {}", response.statusCode());
                }
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise log request", e);
        } catch (IOException | InterruptedException e) {
            LOG.warn("Error while sending log message for session [{}]", sessionId, e);
        }
    }

    /**
     * Notify the Test Bed for a given session.
     *
     * @param sessionId The session ID to notify the Test Bed for.
     * @param callId The 'receive' call ID to notify the Test Bed for.
     * @param report The report to notify the Test Bed with.
     */
    @Async
    public void notifyTestBed(String sessionId, String callId, String callback, TAR report){
        try {
            LOG.info("Notifying Test Bed for session [{}]", sessionId);
            callTestBed(sessionId, callId, report, callback);
        } catch (Exception e) {
            LOG.warn("Error while notifying Test Bed for session [{}]", sessionId, e);
            callTestBed(sessionId, callId, utils.createReport(TestResultType.FAILURE), callback);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Call the Test Bed to notify it of received communication.
     *
     * @param sessionId The session ID that this notification relates to.
     * @param callId The 'receive' call ID to notify the Test Bed for.
     * @param report The TAR report to send back.
     * @param callbackAddress The address on which the call is to be made.
     */
    private void callTestBed(String sessionId, String callId, TAR report, String callbackAddress) {
        try {
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(callbackAddress + "/notifyForMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(
                            NotifyForMessageRequest.builder()
                                    .withSessionId(sessionId)
                                    .withCallId(callId)
                                    .withReport(report)
                                    .build()
                    )))
                    .build();
            try (var client = HttpClient.newBuilder().build()) {
                var response = client.send(httpRequest, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() >= 400) {
                    LOG.warn("Failed to send notification. Status code: {}", response.statusCode());
                }
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise notification request", e);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Error while calling Test Bed for session [" + sessionId + "]", e);
        }
    }

}
