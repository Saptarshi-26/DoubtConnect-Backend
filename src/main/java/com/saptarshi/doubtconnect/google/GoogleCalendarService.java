package com.saptarshi.doubtconnect.google;

import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class GoogleCalendarService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.calendar.scope}")
    private String scope;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Autowired
    private GoogleCredentialRepository googleCredentialRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    public String getAccessToken(Long teacherProfileId){
        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(teacherProfileId);
        if(teacher.isEmpty())
            return null;
        Optional<GoogleCredential> credential =
                googleCredentialRepository.findByTeacherProfile(teacher.get());

        if (credential.isEmpty()) {
            return null;
        }
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("refresh_token", credential.get().getRefreshToken());
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<LinkedMultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                String.class
        );
        JSONObject json = new JSONObject(response.getBody());

        String accessToken = json.getString("access_token");

        return accessToken;

    }

}
