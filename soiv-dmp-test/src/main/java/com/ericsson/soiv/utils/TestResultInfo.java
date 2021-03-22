package com.ericsson.soiv.utils;

public class TestResultInfo {
	private Boolean shouldContinue = true;
	private Exception exception = null;
	private Object result = null;

	public Exception getException() {
		return exception;
	}

	public TestResultInfo setException(Exception exception) {
		this.exception = exception;
		return this;
	}

	public Object getResult() {
		return result;
	}

	public TestResultInfo setResult(Object result) {
		this.result = result;
		return this;
	}

	public Boolean getShouldContinue() {
		return (this.shouldContinue);
	}

	public TestResultInfo setShouldContinue(Boolean stepVerdict) {
		this.shouldContinue = stepVerdict;
		return this;
	}

}
