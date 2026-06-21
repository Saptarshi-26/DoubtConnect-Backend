package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.SignUpRequest;
import com.saptarshi.doubtconnect.dto.UpdateUserDto;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.UserRepository;
import com.saptarshi.doubtconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/getUser/{id}")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
        Optional<User> user = userService.findUser(id);
        return user.isPresent() ? new ResponseEntity<>(user.get(), HttpStatus.OK) : new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getAllUser")
    public ResponseEntity<?> findAll() {
        List<User> user = userService.findAllUsers();
        return user.isEmpty() ? new ResponseEntity<>("No user found", HttpStatus.NOT_FOUND) : new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable  Long id) {
        boolean delete = userService.deleteUser(id);
        return delete ? new ResponseEntity<>("User deleted", HttpStatus.NO_CONTENT) : new ResponseEntity<>("User not found ", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserDto dto) {
        boolean update = userService.updateUser(id, dto);
        return update ? new ResponseEntity<>("Updated", HttpStatus.OK) : new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);

    }

}
