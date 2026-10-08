package com.example.kitchenpalapi.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.kitchenpalapi.entity.User;
import com.example.kitchenpalapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 密码迁移：启动时把数据库里仍是明文的密码（BCrypt 哈希以 $2a$/$2b$/$2y$ 开头）重新哈希。
 * 解决「旧明文账户在引入 BCrypt 后无法登录」的问题。已哈希的跳过，幂等。
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger("PwdMigrate");

    private final UserRepository userRepository;

    public PasswordMigrationRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        int migrated = 0;
        for (User u : userRepository.findAll()) {
            String p = u.getPassword();
            if (p == null || !p.startsWith("$2")) {
                u.setPassword(BCrypt.withDefaults().hashToString(12, p != null ? p.toCharArray() : "123456".toCharArray()));
                userRepository.save(u);
                migrated++;
            }
        }
        if (migrated > 0) log.info("已迁移 {} 个明文密码为 BCrypt 哈希", migrated);
    }
}
