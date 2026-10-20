package com.skill_learning_platform.skill_learning_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
public class GitHubController {

    // Get public GitHub information
    @GetMapping("/profile")
    public ResponseEntity<?> getGitHubProfile(
            @RequestParam String username) {

        try {

            String apiUrl =
                    "https://api.github.com/users/" + username;

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header("Accept", "application/vnd.github+json")
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                return ResponseEntity
                        .status(response.statusCode())
                        .body("GitHub user not found.");
            }

            return ResponseEntity.ok(response.body());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Error connecting to GitHub.");
        }
    }
}