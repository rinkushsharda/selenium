package com.ericsson.soiv.utils.http;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpVersion;
import com.ericsson.jive.core.protocols.http.messagetypes.TextHttpResponse;
import com.ericsson.jive.core.protocols.http.messagetypes.builderexpected.BuilderExpectedHttpResponse;
import com.ericsson.jive.core.typing.BaseBuilder;
import com.ericsson.jive.core.matching.NodeMatchType;

/**
 * Builder capable of creating {@link TextHttpResponse}s.
 *
 * @since 1.0.0
 */
@SuppressWarnings ("unchecked")
public class TextHttpResponseBuilderExpected extends BuilderExpectedHttpResponse<BaseBuilder<?>, TextHttpResponseBuilderExpected> {
    private static final Integer RESPONSE_STATUS_CODE_OK = 200;

    private final TextHttpResponse httpResponse;

    private TextHttpResponseBuilderExpected() {
        super(null);
        httpResponse = new TextHttpResponse();
        httpResponse.body.matchAs(NodeMatchType.ANY_ELEMENT_OR_OMITTED);
        httpResponse.header.matchAs(NodeMatchType.ANY_ELEMENT_OR_OMITTED);
        httpResponse.reason.matchAs(NodeMatchType.ANY_ELEMENT);
        httpResponse.statusCode.setValue(RESPONSE_STATUS_CODE_OK);
        httpResponse.version.setValue(HttpVersion.newHttpVersionFromString(HttpVersion.v11.toString()));
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
    public static TextHttpResponseBuilderExpected newTextHttpResponseBuilder() {
        return new TextHttpResponseBuilderExpected();
    }

    /**
     * Sets the value of the HTTP body.
     *
     * @param body the value
     * @return the updated {@link TextHttpResponseBuilder}
     */
    public TextHttpResponseBuilderExpected setBody(Object body) {
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
