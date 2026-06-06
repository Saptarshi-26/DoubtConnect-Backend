package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.SessionActionDTO;
import com.saptarshi.doubtconnect.dto.SessionRequestDTO;
import com.saptarshi.doubtconnect.dto.UpdateSessionDTO;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.service.SessionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionRequestController {

    @Autowired
    SessionRequestService service;


    @PostMapping("/save")
    public ResponseEntity<?> saveSession(@RequestBody SessionRequestDTO sessionRequestDTO){
        boolean sendRequest = service.sendRequest(sessionRequestDTO);
        return sendRequest?new ResponseEntity<>("Session Request created", HttpStatus.CREATED):
                new ResponseEntity<>("Student or Teacher not found ",HttpStatus.NOT_FOUND);
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<List<SessionRequest>> sessionOfStudent(@PathVariable Long id){

        List<SessionRequest> listSessionRequest = service.findByStudentProfile(id);
        return listSessionRequest.isEmpty()?new ResponseEntity<>(listSessionRequest,HttpStatus.NOT_FOUND):
                new ResponseEntity<>(listSessionRequest,HttpStatus.OK);
    }

    @GetMapping("/teacher/{id}")
    public ResponseEntity<List<SessionRequest>> sessionOfTeacher(@PathVariable Long id){

        List<SessionRequest> listSessionRequest = service.findTeacherProfile(id);
        return listSessionRequest.isEmpty()?new ResponseEntity<>(listSessionRequest,HttpStatus.NOT_FOUND):
                new ResponseEntity<>(listSessionRequest,HttpStatus.OK);
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptSession(@RequestBody SessionActionDTO sessionActionDTO){
        String status = service.acceptRequest(sessionActionDTO);
        return status.equals("ACCEPTED")||status.equals("ALREADY PROCESSED")?new ResponseEntity<>(status,HttpStatus.ACCEPTED):
                new ResponseEntity<>(status,HttpStatus.NOT_FOUND);

    }
    @PostMapping("/reject")
    public ResponseEntity<?> rejectSession(@RequestBody SessionActionDTO sessionActionDTO){
        String status = service.rejectRequest(sessionActionDTO);
        return status.equals("REJECTED")||status.equals("ALREADY PROCESSED")?new ResponseEntity<>(status,HttpStatus.ACCEPTED):
                new ResponseEntity<>(status,HttpStatus.NOT_FOUND);

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id){
        boolean hasDelete = service.deleteSession(id);
        return hasDelete?new ResponseEntity<>("DELETED",HttpStatus.NO_CONTENT):
                new ResponseEntity<>("NO SESSION FOUND",HttpStatus.NOT_FOUND);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<?> getStatus(@PathVariable Long id ){
        String status = service.getStatus(id);
        return status.equals("NOT FOUND")?new ResponseEntity<>(status,HttpStatus.NOT_FOUND):
                new ResponseEntity<>(status,HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id , @RequestBody UpdateSessionDTO dto){
        boolean isUpdate = service.updateSession(id,dto);
        return isUpdate?new ResponseEntity<>("Updated",HttpStatus.OK):
                new ResponseEntity<>("SESSION NOT FOUND",HttpStatus.NOT_FOUND);
    }










}
