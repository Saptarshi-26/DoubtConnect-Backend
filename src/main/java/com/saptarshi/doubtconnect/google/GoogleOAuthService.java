package com.saptarshi.doubtconnect.google;

import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class GoogleOAuthService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.calendar.scope}")
    private String scope;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private GoogleCredentialRepository googleCredentialRepository;

    private boolean isOwner(
            TeacherProfile teacherProfile,
            Authentication authentication) {

        return teacherProfile.getUser()
                .getUsername()
                .equals(authentication.getName());
    }

    public GoogleConnectionStatusDto isGoogleConnected(
            Long teacherProfileId,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new GoogleConnectionStatusDto(false);
        }

        if (!isOwner(
                teacher.get(),
                authentication)) {
            return new GoogleConnectionStatusDto(false);
        }

        boolean connected =
                googleCredentialRepository
                        .findByTeacherProfile(teacher.get())
                        .isPresent();

        return new GoogleConnectionStatusDto(connected);
    }

    public String getAuthorizationUrl(Long teacherProfileId,Authentication authentication) {
        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return null;
        }

        if (!isOwner(teacher.get(), authentication)) {
            return null;
        }


        String url =
                "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=" + clientId +
                        "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                        "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                        "&response_type=code" +
                        "&access_type=offline" +
                        "&prompt=consent"+
                        "&state=" + teacherProfileId;

        //System.out.println(url);

        return url;
    }

    @Transactional
    public String saveTeacherCredential(String code, Long teacherProfileId) {
        Optional<TeacherProfile> teacherProfile =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacherProfile.isEmpty()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

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

        String refreshToken = json.optString("refresh_token", null);

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return "Teacher not found";
        }

        Optional<GoogleCredential> credential =
                googleCredentialRepository.findByTeacherProfile(teacher.get());

        GoogleCredential googleCredential;

        if (credential.isPresent()) {
            googleCredential = credential.get();
        } else {
            googleCredential = new GoogleCredential();
            googleCredential.setTeacherProfile(teacher.get());
        }

        if (refreshToken != null) {
            googleCredential.setRefreshToken(refreshToken);
            googleCredentialRepository.save(googleCredential);
        }

        return "Google Calendar connected successfully";
    }

    public String getAccessToken(TeacherProfile teacherProfile) {

        Optional<GoogleCredential> credential =
                googleCredentialRepository.findByTeacherProfile(teacherProfile);

        if (credential.isEmpty()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();

        LinkedMultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", credential.get().getRefreshToken());
        body.add("grant_type", "refresh_token");

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

        return json.getString("access_token");
    }
}
