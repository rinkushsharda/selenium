package com.ericsson.soiv.utils.http;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpVersion;
import com.ericsson.jive.core.protocols.http.messagetypes.TextHttpRequest;
import com.ericsson.jive.core.protocols.http.messagetypes.builder.BuilderHttpRequest;
import com.ericsson.jive.core.typing.BaseBuilder;

/**
 * Builder capable of creating {@link TextHttpRequest}s.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class TextHttpRequestBuilder extends BuilderHttpRequest<BaseBuilder<?>, TextHttpRequestBuilder> {
    private static final HttpVersion VERSION = HttpVersion.v11;

    private final TextHttpRequest httpRequest;

    private TextHttpRequestBuilder() {
        super(null);
        httpRequest = new TextHttpRequest();
        httpRequest.version.setValue(VERSION);
    }

    @Override
    protected TextHttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Creates a new builder for creating {@link TextHttpRequest}s.
     *
     * @return the new {@link TextHttpRequestBuilder}
     */
    public static TextHttpRequestBuilder newTextHttpRequestBuilder() {
        return new TextHttpRequestBuilder();
    }

    /**
     * Sets the value of the HTTP body.
     *
     * @param body
     *            the value
     * @return the updated {@link TextHttpRequestBuilder}
     */
    public TextHttpRequestBuilder setBody(Object body) {
        httpRequest.body.setValue(body);
        return this;
    }

    /**
     * Builds and returns the populated {@link TextHttpRequest}.
     *
     * @return the populated {@link TextHttpRequest}
     */
    public TextHttpRequest build() {
        return httpRequest;
    }

    @Override
    public String getClassName() {
        return "TextHttpRequest";
    }
}
