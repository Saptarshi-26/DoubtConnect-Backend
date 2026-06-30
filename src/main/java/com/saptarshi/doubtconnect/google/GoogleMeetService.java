package com.saptarshi.doubtconnect.google;

import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class GoogleMeetService {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    public String createMeet(
            TeacherProfile teacherProfile,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        String accessToken =
                googleOAuthService.getAccessToken(teacherProfile);

        if (accessToken == null) {
            return null;
        }
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject event = new JSONObject();

        event.put("summary", "DoubtConnect Session");

        event.put("start",
                new JSONObject()
                        .put("dateTime", startTime.toString()));

        event.put("end",
                new JSONObject()
                        .put("dateTime", endTime.toString()));

        event.put(
                "conferenceData",
                new JSONObject()
                        .put(
                                "createRequest",
                                new JSONObject()
                                        .put(
                                                "requestId",
                                                java.util.UUID.randomUUID().toString()
                                        )
                        )
        );

        HttpEntity<String> request =
                new HttpEntity<>(event.toString(), headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1",
                        request,
                        String.class
                );

        JSONObject json = new JSONObject(response.getBody());

        if (json.has("hangoutLink")) {
            return json.getString("hangoutLink");
        }

        if (json.has("conferenceData")) {

            JSONObject conferenceData =
                    json.getJSONObject("conferenceData");

            if (conferenceData.has("entryPoints")) {

                for (Object object : conferenceData.getJSONArray("entryPoints")) {

                    JSONObject entryPoint = (JSONObject) object;

                    if ("video".equals(entryPoint.getString("entryPointType"))) {
                        return entryPoint.getString("uri");
                    }
                }
            }
        }

        return null;

    }
}

