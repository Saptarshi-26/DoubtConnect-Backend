//package com.saptarshi.doubtconnect.google;
//
//import com.saptarshi.doubtconnect.entity.TeacherProfile;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Service
//public class GoogleMeetService {
//
//    // LocalDateTime.toString() omits seconds when they are zero (e.g. "10:00"
//    // instead of "10:00:00"), which breaks RFC3339 and Google rejects it with
//    // a generic 400. This formatter guarantees seconds are always present.
//    private static final DateTimeFormatter RFC3339_FORMATTER =
//            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//
//    @Autowired
//    private GoogleOAuthService googleOAuthService;
//
//    public String createMeet(
//            TeacherProfile teacherProfile,
//            LocalDateTime startTime,
//            LocalDateTime endTime) {
//
//        String accessToken =
//                googleOAuthService.getAccessToken(teacherProfile);
//
//        if (accessToken == null) {
//            System.out.println(
//                    "No Google access token for teacher: " + teacherProfile.getId());
//            return null;
//        }
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//
//        headers.setBearerAuth(accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        JSONObject event = new JSONObject();
//
//        event.put("summary", "DoubtConnect Session");
//
//        event.put("start",
//                new JSONObject()
//                        .put("dateTime", startTime.format(RFC3339_FORMATTER))
//                        .put("timeZone", "Asia/Kolkata"));
//
//        event.put("end",
//                new JSONObject()
//                        .put("dateTime", endTime.format(RFC3339_FORMATTER))
//                        .put("timeZone", "Asia/Kolkata"));
//
//        JSONObject conferenceSolutionKey =
//                new JSONObject()
//                        .put("type", "hangoutsMeet");
//
//        JSONObject createRequest =
//                new JSONObject()
//                        .put("requestId", java.util.UUID.randomUUID().toString())
//                        .put("conferenceSolutionKey", conferenceSolutionKey);
//
//        event.put(
//                "conferenceData",
//                new JSONObject()
//                        .put("createRequest", createRequest)
//        );
//
//        HttpEntity<String> request =
//                new HttpEntity<>(event.toString(), headers);
//
//        ResponseEntity<String> response;
//
//        try {
//
//            response = restTemplate.postForEntity(
//                    "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1",
//                    request,
//                    String.class
//            );
//
//        } catch (HttpClientErrorException e) {
//
//            System.err.println(
//                    "Google Meet creation failed for teacher "
//                            + teacherProfile.getId()
//                            + ": " + e.getStatusCode()
//                            + " - " + e.getResponseBodyAsString());
//
//            return null;
//        }
//
//        if (response.getBody() == null) {
//            System.err.println(
//                    "Google Meet creation returned empty body for teacher "
//                            + teacherProfile.getId());
//            return null;
//        }
//
//        JSONObject json = new JSONObject(response.getBody());
//
//        if (json.has("hangoutLink")) {
//            return json.getString("hangoutLink");
//        }
//
//        if (json.has("conferenceData")) {
//
//            JSONObject conferenceData =
//                    json.getJSONObject("conferenceData");
//
//            if (conferenceData.has("entryPoints")) {
//
//                for (Object object : conferenceData.getJSONArray("entryPoints")) {
//
//                    JSONObject entryPoint = (JSONObject) object;
//
//                    if ("video".equals(entryPoint.getString("entryPointType"))) {
//                        return entryPoint.getString("uri");
//                    }
//                }
//            }
//        }
//
//        System.err.println(
//                "Google Meet creation succeeded but no meet link found for teacher "
//                        + teacherProfile.getId() + ": " + response.getBody());
//
//        return null;
//    }
//}