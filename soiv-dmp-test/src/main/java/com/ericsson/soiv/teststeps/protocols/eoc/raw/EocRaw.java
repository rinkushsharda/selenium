package com.ericsson.soiv.teststeps.protocols.eoc.raw;

import com.ericsson.jive.core.execution.ClientTransaction;
import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.execution.TransactionResult;
import com.ericsson.jive.core.frameworkconfiguration.Level;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpRequest;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpVersion;
import com.ericsson.soiv.constants.ClientHandle;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.http.TextHttpRequestBuilder;
import com.ericsson.soiv.utils.http.TextHttpResponseBuilderExpected;

import static com.ericsson.jive.core.execution.TransactionFactory.newClientTransaction;
import static com.ericsson.soiv.utils.HttpUtils.newUri;


public class EocRaw extends TestStepBase<TestResultInfo> {
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private HttpMethod httpMethod = null;
    private int statusCode;
    private String uri = "";
    private String body = "";


    public EocRaw(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode, String uri) {
        super(title);
        this.resultInfo = resultInfo;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
        this.uri = uri;
    }

    public EocRaw(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode, String uri, String body) {
        super(title);
        this.resultInfo = resultInfo;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
        this.uri = uri;
        this.body = body;
    }

    @Override
    public TestResultInfo execute() {
        if (!resultInfo.getShouldContinue()) {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }

        Jive.log("Step title: " + getTitle());
        HttpResponse response = sendEocRequest(body, uri, statusCode, httpMethod);
        resultInfo.setResult(response);
        return resultInfo;
    }

    private HttpResponse sendEocRequest(String body, String uri, int expectedStatusCode, HttpMethod httpMethod) {
        TextHttpRequestBuilder httpRequestBuilder;
        if (httpMethod.equals(HttpMethod.GET)) {
            // An HTTP GET request to be sent without body
            //@formatter:off
            httpRequestBuilder = TextHttpRequestBuilder.newTextHttpRequestBuilder()
                    .setHttpMethod(httpMethod)
                    .setHttpVersion(HttpVersion.v11)
                    .setUri(newUri(uri));
            //@formatter:on
        }else {
            // An HTTP POST request to be sent with body
            //@formatter:off
            httpRequestBuilder = TextHttpRequestBuilder.newTextHttpRequestBuilder()
                    .setHttpMethod(httpMethod)
                    .setHttpVersion(HttpVersion.v11)
                    .addHttpHeader()
                    .setHttpHeaderName("Content-Type")
                    .setHttpHeaderValue("application/json").back()
                    .setUri(newUri(uri))
                    .setBody(body);
            //@formatter:on
        }

        HttpRequest httpRequest = httpRequestBuilder.build();
        //@formatter:off
        HttpResponse expectedHttpResponse = TextHttpResponseBuilderExpected.newTextHttpResponseBuilder()
                .matchHttpVersion(HttpVersion.v11)
                .matchStatusCode(expectedStatusCode)
                .build();
        //@formatter:on

        ClientTransaction<HttpRequest, HttpResponse> transaction = newClientTransaction(httpRequest, expectedHttpResponse);
        Jive.queue(ClientHandle.HTTP_CLIENT_EOC.name(), transaction);
        Jive.log(Level.DEBUG, "Queue performed");
        TransactionResult transactionResult = Jive.executeAndVerify();
        Jive.log(Level.DEBUG, "ExecuteandVerify performed");
        HttpResponse response = transactionResult.getResultForTransaction(transaction);
        Jive.log(Level.DEBUG, "Got result");
        return response;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public EocRaw setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
