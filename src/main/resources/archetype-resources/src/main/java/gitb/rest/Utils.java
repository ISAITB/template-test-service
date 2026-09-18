package ${package}.gitb.rest;

import com.gitb.model.core.*;
import com.gitb.model.tr.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.ZonedDateTime;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Class containing utility methods.
 */
@Component
public class Utils {

    /** HTTP header name for the ReplyTo address. */
    private static final String REPLY_TO_HEADER = "Gitb-Reply-To";
    /** HTTP header name for the test session ID. */
    public static final String TEST_SESSION_ID_HEADER = "Gitb-Test-Session-Identifier";


    /**
     * Create a report for the given result.
     * <p/>
     * This method creates the report, sets its time and constructs an empty context map to return values with.
     *
     * @param result The overall result of the report.
     * @return The report.
     */
    public TAR createReport(TestResultType result) {
        return TAR.builder()
                .withContext(AnyContent.builder().withType("map").build())
                .withResult(result)
                .withDate(ZonedDateTime.now())
                .build();
    }

    /**
     * Create a parameter definition.
     *
     * @param name The name of the parameter.
     * @param type The type of the parameter. This needs to match one of the GITB types.
     * @param use The use (required or optional).
     * @param kind The kind og parameter it is (whether it should be provided as the specific value, as BASE64 content or as a URL that needs to be looked up to obtain the value).
     * @param description The description of the parameter.
     * @return The created parameter.
     */
    public TypedParameter createParameter(String name, String type, UsageEnumeration use, ConfigurationType kind, String description) {
        return TypedParameter.builder()
                .withName(name)
                .withType(type)
                .withUse(use)
                .withKind(kind)
                .withDesc(description)
                .build();
    }

    /**
     * Collect the inputs that match the provided name.
     *
     * @param parameterItems The items to look through.
     * @param inputName The name of the input to look for.
     * @return The collected inputs (not null).
     */
    public List<AnyContent> getInputsForName(List<AnyContent> parameterItems, String inputName) {
        List<AnyContent> inputs = new ArrayList<>();
        if (parameterItems != null) {
            for (AnyContent anInput: parameterItems) {
                if (inputName.equals(anInput.getName())) {
                    inputs.add(anInput);
                }
            }
        }
        return inputs;
    }

    /**
     * Get a single required input for the provided name.
     *
     * @param parameterItems The items to look through.
     * @param inputName The name of the input to look for.
     * @return The input.
     */
    public AnyContent getSingleRequiredInputForName(List<AnyContent> parameterItems, String inputName) {
        var inputs = getInputsForName(parameterItems, inputName);
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException(String.format("No input named [%s] was found.", inputName));
        } else if (inputs.size() > 1) {
            throw new IllegalArgumentException(String.format("Multiple inputs named [%s] were found when only one was expected.", inputName));
        }
        return inputs.getFirst();
    }

    /**
     * Get a single optional input for the provided name.
     *
     * @param parameterItems The items to look through.
     * @param inputName The name of the input to look for.
     * @return The input.
     */
    public Optional<AnyContent> getSingleOptionalInputForName(List<AnyContent> parameterItems, String inputName) {
        var inputs = getInputsForName(parameterItems, inputName);
        if (inputs.isEmpty()) {
            return Optional.empty();
        } else if (inputs.size() > 1) {
            throw new IllegalArgumentException(String.format("Multiple inputs named [%s] were found when at most one was expected.", inputName));
        } else {
            return Optional.of(inputs.getFirst());
        }
    }

    /**
     * Convert the provided content to a string value.
     *
     * @param content The content to convert.
     * @return The string value.
     */
    public String asString(AnyContent content) {
        if (content == null || content.getValue() == null) {
            return null;
        } else if (content.getEmbeddingMethod() == ValueEmbeddingEnumeration.BASE_64) {
            // Value provided as BASE64 string.
            return new String(Base64.getDecoder().decode(content.getValue()));
        } else if (content.getEmbeddingMethod() == ValueEmbeddingEnumeration.URI) {
            // Value provided as URI to look up.
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(content.getValue()))
                    .GET()
                    .build();
            try (var client = HttpClient.newHttpClient()) {
                return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            } catch (IOException | InterruptedException e) {
                throw new IllegalArgumentException(String.format("Error while calling URI [%s]", content.getValue()), e);
            }
        } else {
            // Value provided as String.
            return content.getValue();
        }
    }

    /**
     * Get a single required input for the provided name as a string value.
     *
     * @param parameterItems The items to look through.
     * @param inputName The name of the input to look for.
     * @return The input's string value.
     */
    public String getRequiredString(List<AnyContent> parameterItems, String inputName) {
        return asString(getSingleRequiredInputForName(parameterItems, inputName));
    }

    /**
     * Get a single required input for the provided name as a binary value.
     *
     * @param parameterItems The items to look through.
     * @param inputName The name of the input to look for.
     * @return The input's byte[] value.
     */
    public byte[] getRequiredBinary(List<AnyContent> parameterItems, String inputName) {
        var input = getSingleRequiredInputForName(parameterItems, inputName);
        if (input.getEmbeddingMethod() == null || input.getEmbeddingMethod() == ValueEmbeddingEnumeration.BASE_64) {
            // Base64 encoded string.
            return Base64.getDecoder().decode(input.getValue());
        } else if (input.getEmbeddingMethod() == ValueEmbeddingEnumeration.URI) {
            // Remote URI to read from.
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(input.getValue()))
                    .GET()
                    .build();
            try (var client = HttpClient.newHttpClient()) {
                return client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
            } catch (IOException | InterruptedException e) {
                throw new IllegalArgumentException(String.format("Error while calling URI [%s]", input.getValue()), e);
            }
        } else {
            throw new IllegalArgumentException(String.format("Input [%s] was expected to be provided as a BASE64 string or a URI.", inputName));
        }
    }

    /**
     * Get a single optional input for the provided name as a string value.
     *
     * @param parameterItems The items to look through.
     * @param inputName The name of the input to look for.
     * @return The input's string value.
     */
    public Optional<String> getOptionalString(List<AnyContent> parameterItems, String inputName) {
        var input = getSingleOptionalInputForName(parameterItems, inputName);
        return input.map(this::asString);
    }

    /**
     * Create a AnyContent object value based on the provided parameters.
     *
     * @param name The name of the value.
     * @param value The value itself.
     * @param embeddingMethod The way in which this value is to be considered.
     * @return The value.
     */
    public AnyContent createAnyContentSimple(String name, String value, ValueEmbeddingEnumeration embeddingMethod) {
        return AnyContent.builder()
                .withName(name)
                .withValue(value)
                .withEmbeddingMethod(embeddingMethod)
                .build();
    }

    /**
     * Parse the received HTTP headers to retrieve the "reply-to" address.
     *
     * @param request The HTTP request.
     * @return The header's value.
     */
    public Optional<String> getReplyToAddressFromHeaders(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(REPLY_TO_HEADER));
    }

    /**
     * Parse the received HTTP headers to retrieve the test session identifier.
     *
     * @param request The HTTP request.
     * @return The header's value.
     */
    public Optional<String> getTestSessionIdFromHeaders(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(TEST_SESSION_ID_HEADER));
    }

}

