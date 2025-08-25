package com.example.auth_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.auth_server.model.User;
import com.example.auth_server.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("alice").isEmpty()) {
            User alice = new User();
            alice.setUsername("alice");
            alice.setPassword("password");
            userRepository.save(alice);
        }
        if (userRepository.findByUsername("bob").isEmpty()) {
            User bob = new User();
            bob.setUsername("bob");
            bob.setPassword("password");
            userRepository.save(bob);
        }
        System.out.println("Initial users loaded into DB");
    }
}
