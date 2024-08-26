package com.example.Spring_MiniProject_Eg3.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.Spring_MiniProject_Eg3.entity.Student;
import java.util.Collection;

public class StudentDetails implements UserDetails
{

    private String username;
    private String password;

    public StudentDetails(Student student)
    {
        this.username = student.getUsername();
        this.password = student.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}