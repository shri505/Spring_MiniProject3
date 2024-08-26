package com.example.Spring_MiniProject_Eg3.service;

import com.example.Spring_MiniProject_Eg3.entity.Course;
import com.example.Spring_MiniProject_Eg3.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.Spring_MiniProject_Eg3.entity.Student;
import com.example.Spring_MiniProject_Eg3.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
@Service
public class StudentService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Student> userDetail = studentRepository.findByUsername(username);
        return userDetail.map(StudentDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public String addUser(Student student) {
        Optional<Student> optionalValue = studentRepository.findByUsername(student.getUsername());
        if (optionalValue.isPresent()) {
            return "User already Registered";
        }
        student.setPassword(encoder.encode(student.getPassword()));
        studentRepository.save(student);
        return "User Added Successfully";
    }

    public Student createUser(Student student) {
        student.setPassword(encoder.encode(student.getPassword()));
        return studentRepository.save(student);
    }

    public Student getUserById(int id) {
        return studentRepository.findById(id).orElse(null);
    }

    public List<Student> getAllUsers() {
        return studentRepository.findAll();
    }

    public Student updateUser(int id, Student student) {
        Student existingUser = studentRepository.findById(id).orElse(null);
        if (existingUser != null) {
            existingUser.setUsername(student.getUsername());
            existingUser.setEmail(student.getEmail());
            if (!existingUser.getPassword().equals(student.getPassword())) {
                existingUser.setPassword(encoder.encode(student.getPassword()));
            }
            return studentRepository.save(existingUser);
        }
        return null;
    }

    public void deleteUser(int id) {
        studentRepository.deleteById(id);
    }

    // New method to enroll a student in a course
    public void enrollStudentToCourse(int studentId, int courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Add the course to the student's enrolled courses
        student.getEnrolledCourses().add(course);

        // Save the student entity with updated courses
        studentRepository.save(student);
    }

}

