package com.ericsson.soiv.utils.http;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpVersion;
import com.ericsson.jive.core.protocols.http.messagetypes.TextHttpResponse;
import com.ericsson.jive.core.protocols.http.messagetypes.builder.BuilderHttpResponse;
import com.ericsson.jive.core.typing.BaseBuilder;

/**
 * Builder capable of creating {@link TextHttpResponse}s.
 *
 * @since 1.0.0
 */
@SuppressWarnings ("unchecked")
public class TextHttpResponseBuilder extends BuilderHttpResponse<BaseBuilder<?>, TextHttpResponseBuilder> {
    private static final String RESPONSE_REASON_OK = "OK";
    private static final Integer RESPONSE_STATUS_CODE_OK = 200;

    private final TextHttpResponse httpResponse;

    private TextHttpResponseBuilder() {
        super(null);
        httpResponse = new TextHttpResponse();
        httpResponse.body.setValue("");
        httpResponse.reason.setValue(RESPONSE_REASON_OK);
        httpResponse.statusCode.setValue(RESPONSE_STATUS_CODE_OK);
        HttpVersion httpVersion = HttpVersion.newHttpVersionFromString(HttpVersion.v11.toString());
        httpResponse.version.setValue(httpVersion);
    }

    @Override
    protected TextHttpResponse getHttpResponse() {
        return httpResponse;
    }

    /**
     * Creates a new builder for creating {@link TextHttpResponse}s.
     *
     * @return the new {@link TextHttpResponseBuilder}
     */
    public static TextHttpResponseBuilder newTextHttpResponseBuilder() {
        return new TextHttpResponseBuilder();
    }

    /**
     * Sets the value of the HTTP body.
     *
     * @param body the value
     * @return the updated {@link TextHttpResponseBuilder}
     */
    public TextHttpResponseBuilder setBody(Object body) {
        httpResponse.body.setValue(body);
        return this;
    }

    /**
     * Builds and returns the populated {@link TextHttpResponse}.
     *
     * @return the populated {@link TextHttpResponse}
     */
    public TextHttpResponse build() {
        return httpResponse;
    }

    @Override
    public String getClassName() {
        return "TextHttpResponse";
    }
}
