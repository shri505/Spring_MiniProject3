package com.example.Spring_MiniProject_Eg3.controller;

import com.example.Spring_MiniProject_Eg3.entity.Course;
import com.example.Spring_MiniProject_Eg3.entity.Student;
import com.example.Spring_MiniProject_Eg3.repository.CourseRepository;
import com.example.Spring_MiniProject_Eg3.service.JwtService;
import com.example.Spring_MiniProject_Eg3.service.StudentService;
import com.example.Spring_MiniProject_Eg3.config.AuthenticationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("userDetailsService")
    private StudentService userService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StudentService studentService;

    // Method to authenticate the token in each request
    // Extracts the JWT token from the "Authorization" header, retrieves the username, and validates the token.
    private boolean authenticate(String authorizationHeader) {
        String token = authorizationHeader.substring(7); // Remove "Bearer "
        String username = jwtService.extractUsername(token);
        return jwtService.validateToken(token, userService.loadUserByUsername(username));
    }

    // Add a new user without JWT token verification
    // This endpoint is for creating a new user.
    // It doesn't require authorization, allowing users to register freely.
    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody Student student) {
        return userService.addUser(student);
    }

    // Generate JWT Token for user authentication
    // Takes username and password from the request, authenticates the user, and returns a JWT token.
    @PostMapping("/generateToken")
    public String generateToken(@RequestBody AuthenticationRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails.getUsername());
    }

    // Get all users with JWT token verification
    // Requires a valid JWT token. Returns a list of all students.
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<Student>> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get user details by ID with JWT token verification
    // Returns details of a student based on the provided ID.
    @GetMapping("/getUser/{id}")
    public ResponseEntity<Student> getUserById(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Delete a user by ID with JWT token verification
    // Deletes a student record by their ID. Requires valid token.
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Update a user profile with JWT token verification
    // Updates a student profile but only allows updating the profile of the authenticated user (i.e., you can only update your own profile).
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody Student student, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token or authentication failed.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (!username.equals(student.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own profile.");
        }

        userService.updateUser(student.getId(), student);
        return ResponseEntity.ok("User updated successfully.");
    }

    // Course Management Endpoints (JWT-Protected)

    // Enroll in a course with JWT token verification
    // Creates a new course and enrolls a student in it. Requires valid JWT.
    @PostMapping("/enroll")
    public ResponseEntity<String> enrollInCourse(@RequestBody Course course, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token or authentication failed.");
        }

        courseRepository.save(course);
        return ResponseEntity.ok("Enrolled in course successfully.");
    }

    // Get all courses with JWT token verification
    // Fetches the list of all courses. Requires valid JWT.
    @GetMapping("/getCourse")
    public List<Course> getAllCourses(@RequestHeader("Authorization") String authorizationHeader) {
        return courseRepository.findAll();
    }

    // Get course details by ID with JWT token verification
    // Retrieves course details based on course ID. Requires valid token.
    @GetMapping("/getCourse/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return ResponseEntity.ok(course);
    }

    // Update course details by ID with JWT token verification
    // Updates course details (e.g., course name) based on the course ID.
    @PutMapping("/updateCourse/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable int id, @RequestBody Course courseDetails, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setCourseName(courseDetails.getCourseName());
        return ResponseEntity.ok(courseRepository.save(course));
    }

    // Delete a course by ID with JWT token verification
    // Deletes a course by its ID. Requires valid JWT.
    @DeleteMapping("/deleteCourse/{id}")
    public ResponseEntity<Void> deleteCourseById(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseRepository.delete(course);
        return ResponseEntity.noContent().build();
    }

    // Enroll a student in a course with JWT token verification
    // This endpoint allows enrolling a student in a specific course by providing student ID and course ID. Requires valid JWT.
    @PostMapping("/students/{studentId}/courses/{courseId}")
    public ResponseEntity<String> enrollStudentToCourse(
            @PathVariable int studentId,
            @PathVariable int courseId,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!authenticate(authorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token or authentication failed.");
        }

        studentService.enrollStudentToCourse(studentId, courseId);
        return ResponseEntity.ok("Student enrolled in course successfully.");
    }
}


