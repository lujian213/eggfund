package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.Investor;
import io.github.lujian213.eggfund.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FileUserDetailsManager extends FileSystemDaoImpl implements UserDetailsService {

    protected final Log logger = LogFactory.getLog(getClass());
    protected Map<String, SecurityUser> users = new HashMap<>();

    public FileUserDetailsManager(@Value("${repo.folder}") File repoFile) {
        super(repoFile);
        try {
            File usersFile = new File(repoFile, Constants.USERS_FILE_NAME);
            if (usersFile.isFile()) {
                users = Constants.MAPPER.readerForMapOf(SecurityUser.class).readValue(usersFile);
            }
        } catch (IOException e) {
            logger.error("Read user from file failed: ", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        users.put("admin", createAdmin());
        SecurityUser user = users.get(username.toLowerCase(Locale.ROOT));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
    }

    public void saveUsers(Collection<Investor> investors) throws IOException {
        users = convertToUsers(investors);
        File userFile = new File(repoFile, Constants.USERS_FILE_NAME);
        Constants.MAPPER.writeValue(userFile, users);

    }

    private Map<String, SecurityUser> convertToUsers(Collection<Investor> investors) {
        return investors.stream()
                .map(Investor::getId)
                .map(this::createUser)
                .collect(Collectors.toMap(SecurityUser::getUsername, Function.identity()));
    }

    private SecurityUser createUser(String id) {
        return new SecurityUser(id.toLowerCase(Locale.ROOT), Constants.DEFAULT_AABB, List.of(Constants.DEFAULT_ROLE));
    }

    private SecurityUser createAdmin() {
        return new SecurityUser("admin", Constants.DEFAULT_AABB, List.of("ADMIN"));
    }

    public static class SecurityUser{
        private final String username;
        private final String password;
        private final List<String> roles;

        SecurityUser(String username, String password, List<String> roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}



