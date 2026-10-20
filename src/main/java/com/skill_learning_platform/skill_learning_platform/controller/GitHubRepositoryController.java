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
public class GitHubRepositoryController {

    @GetMapping("/repositories")
    public ResponseEntity<?> getRepositories(
            @RequestParam String username) {

        try {

            String apiUrl =
                    "https://api.github.com/users/"
                    + username
                    + "/repos?sort=updated&per_page=10";

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header(
                                    "Accept",
                                    "application/vnd.github+json"
                            )
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                return ResponseEntity
                        .status(response.statusCode())
                        .body("Unable to load GitHub repositories.");
            }

            return ResponseEntity.ok(response.body());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Error connecting to GitHub.");
        }
    }
}