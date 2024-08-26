package com.example.Spring_MiniProject_Eg3.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.Spring_MiniProject_Eg3.entity.Student;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>

{
    Optional<Student> findByUsername(String username);

}