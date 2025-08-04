package com.example.demo.entity;

import java.util.List;

public class CodeRequest {
    public String code;
    public List<String> testCases;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<String> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<String> testCases) {
		this.testCases = testCases;
	}
	
    
    
}

