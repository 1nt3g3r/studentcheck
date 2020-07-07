package com.intgroup.htmlcheck.service.security;

import com.intgroup.htmlcheck.domain.security.Role;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.repository.RoleRepository;
import com.intgroup.htmlcheck.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository("userService")
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Set<String> userTokenCache = ConcurrentHashMap.newKeySet();

    public boolean isUserWithTokenPresent(String token) {
        if (userTokenCache.contains(token)) {
            return true;
        }

        boolean userFound = findUserByToken(token) != null;
        if (userFound) {
            userTokenCache.add(token);

            return true;
        }

        return false;
    }

    public void invalidateTokenCache() {
        userTokenCache.clear();
    }

    public void removeTokenFromCache(String token) {
        userTokenCache.remove(token);
    }

    public boolean userWithEmailExists(String email) {
        return userRepository.userWithEmailExists(email);
    }

    public User findUserByEmail(String email) {
        if (email != null) {
            email = email.toLowerCase();
        }
        return userRepository.findByEmail(email);
    }

    public User findUserByToken(String token) {
        if (token == null) {
            return null;
        }

        return userRepository.findByToken(token);
    }

    public void saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(1);
        Role userRole = roleRepository.findByRole("USER");
        user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        userRepository.save(user);
    }

    public void saveFilledUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(1);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void activate(long userId) {
        User user = userRepository.getOne(userId);
        user.setActive(1);
        userRepository.save(user);
    }

    public void deactivate(long userId) {
        User user = userRepository.getOne(userId);
        user.setActive(0);
        userRepository.save(user);
    }

    public void deleteUserById(long userId) {
        userRepository.deleteById(userId);
    }

    public User getUserById(long userId) {
        return userRepository.getOne(userId);
    }

    public void changePassword(long userId, String password) {
        User user = getUserById(userId);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
    }

    public User getUser() {
        return userRepository.findByEmail(getUsername());
    }

    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }


    public User getUser(String email, String password) {
        User user = findUserByEmail(email);

        if (user == null) {
            System.out.println("No user found with email: " + email);
            return null;
        }

        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            System.out.println("Passwords not matches");
            return null;
        }
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public boolean isLoggedIn() {
        return getUser() != null;
    }

    public void clear() {
        userRepository.deleteAll();
    }

    public String generateToken() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256");
        salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (byte b : salt.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println(new UserService().generateToken());
    }
}