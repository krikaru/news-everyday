package com.example.newseveryday.service;

import com.example.newseveryday.model.Role;
import com.example.newseveryday.model.AppUser;
import com.example.newseveryday.repo.UserRepo;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Data
public class UserService implements UserDetailsService {
    private final UserRepo userRepo;

    public AppUser createUser(AppUser user) {
        user.setRoles(Set.of(Role.USER));
        user.setActive(true);
        return userRepo.save(user);
    }

    public List<AppUser> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AppUser> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return optionalUser.get();
    }

    public AppUser getUserByEmail(String email) {
        return userRepo.findByEmail(email).get();
    }
}
