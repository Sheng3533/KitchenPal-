package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.User;
import com.example.kitchenpalapi.repository.UserRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private String hash(String plain) {
        return BCrypt.withDefaults().hashToString(12, plain.toCharArray());
    }

    private boolean verify(String plain, String hashed) {
        if (hashed == null) return false;
        try {
            return BCrypt.verifyer().verify(plain.toCharArray(), hashed.toCharArray()).verified;
        } catch (Exception e) {
            return false;
        }
    }

    public User register(String phone, String password, String nickname) {
        if (phone == null || phone.isBlank() || password == null || password.length() < 6) {
            throw new RuntimeException("手机号或密码不合法（密码至少6位）");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已注册");
        }
        User user = new User();
        user.setPhone(phone);
        user.setPassword(hash(password));
        user.setNickname(nickname != null ? nickname : "美食探索家");
        user.setTastePreferences("清淡,咸鲜,素食");
        user.setCookingCount(0);
        user.setFavoriteCount(0);
        user.setStreakDays(0);
        return userRepository.save(user);
    }

    public User login(String phone, String password) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOpt.get();
        if (!verify(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /** 演示版找回密码：无验证码，直接按手机号重置（生产需叠加验证） */
    public void resetPassword(String phone, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        User user = getUserByPhone(phone);
        user.setPassword(hash(newPassword));
        userRepository.save(user);
    }

    public User updateUser(Long id, User update) {
        User user = getUserById(id);
        if (update.getNickname() != null) user.setNickname(update.getNickname());
        if (update.getAvatar() != null) user.setAvatar(update.getAvatar());
        if (update.getTastePreferences() != null) user.setTastePreferences(update.getTastePreferences());
        if (update.getDarkMode() != null) user.setDarkMode(update.getDarkMode());
        if (update.getCookingNotify() != null) user.setCookingNotify(update.getCookingNotify());
        if (update.getStatus() != null) user.setStatus(update.getStatus());
        if (update.getPhone() != null) user.setPhone(update.getPhone());
        if (update.getCookingCount() != null) user.setCookingCount(update.getCookingCount());
        if (update.getStreakDays() != null) user.setStreakDays(update.getStreakDays());
        if (update.getFavoriteCount() != null) user.setFavoriteCount(update.getFavoriteCount());
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        if (!verify(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        user.setPassword(hash(newPassword));
        userRepository.save(user);
        return true;
    }
}
