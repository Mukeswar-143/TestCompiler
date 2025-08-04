package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CodeRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@CrossOrigin(origins = "*")
public class CompilerController {

	@GetMapping("/")
	public String homeString() {
		return "Hello from CompilerController!";
	}
	@PostMapping("/java")
	public ResponseEntity<List<String>> compileJava(@RequestBody CodeRequest request) throws IOException, InterruptedException {
	    String fileName = "Main.java";
	    Path tempDir = Files.createTempDirectory("code");
	    Path filePath = tempDir.resolve(fileName);
	    Files.write(filePath, request.code.getBytes());

	    // Compile once
	    Process compileProcess = new ProcessBuilder(
	        "docker", "run", "--rm",
	        "-v", tempDir.toAbsolutePath() + ":/app",
	        "-w", "/app",
	        "openjdk:17",
	        "javac", fileName
	    ).start();

	    int compileExitCode = compileProcess.waitFor();
	    String compileErrors = new String(compileProcess.getErrorStream().readAllBytes());

	    if (compileExitCode != 0) {
	        return ResponseEntity.ok(List.of("Compilation Error:\n" + compileErrors));
	    }

	    List<String> results = new java.util.ArrayList<>();

	    for (String input : request.testCases) {
	        String escapedInput = input.replace("\"", "\\\"");
	        Process runProcess = new ProcessBuilder(
	            "docker", "run", "--rm",
	            "-v", tempDir.toAbsolutePath() + ":/app",
	            "-w", "/app",
	            "openjdk:17",
	            "sh", "-c", "echo \"" + escapedInput + "\" | java Main"
	        ).start();

	        runProcess.waitFor();
	        String output = new String(runProcess.getInputStream().readAllBytes());
	        String error = new String(runProcess.getErrorStream().readAllBytes());

	        results.add(error.isEmpty() ? output.trim() : "Runtime Error:\n" + error);
	    }
System.out.println(results);
	    return ResponseEntity.ok(results);
	}
	
	@PostMapping("/python")
	public ResponseEntity<List<String>> executePython(@RequestBody CodeRequest request) throws IOException, InterruptedException {
	    String fileName = "main.py";
	    Path tempDir = Files.createTempDirectory("python-code");
	    Path filePath = tempDir.resolve(fileName);
	    Files.write(filePath, request.code.getBytes());

	    List<String> results = new ArrayList<>();

	    for (String input : request.testCases) {
	        String escapedInput = input.replace("\"", "\\\"");

	        Process runProcess = new ProcessBuilder(
	            "docker", "run", "--rm",
	            "-v", tempDir.toAbsolutePath() + ":/app",
	            "-w", "/app",
	            "python:3.10",
	            "sh", "-c", "echo \"" + escapedInput + "\" | python3 " + fileName
	        ).start();

	        runProcess.waitFor();

	        String output = new String(runProcess.getInputStream().readAllBytes()).trim();
	        String error = new String(runProcess.getErrorStream().readAllBytes()).trim();
	        results.add(error.isEmpty() ? output : error);
	    }
System.out.println(results);
	    return ResponseEntity.ok(results);
	}


}

