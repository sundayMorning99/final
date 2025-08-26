package org.launchcode.etf.dao;

import org.launchcode.etf.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    // You can also create using DataSource. 
    // public UserDao(DataSource dataSource){this.jdbcTemplate = new JdbcTemplate(dataSource);}
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
            );
        }
    };

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY username";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    // Required by fraho library
    // I can just combine getUserByUsername and findByUsername methods, but keeping them separate for clarity.
    public User getUserByUsername(String username) {
        return findByUsername(username);
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, username);
        return users.isEmpty() ? null : users.get(0);
    }

    // Required by fraho library - return roles for username
    public List<String> getRoles(String username) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return List.of("ROLE_" + user.getRole());
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? null : users.get(0);
    }

    // Unlike JpaRepository, we can't get updated id. 
    // So we have to use KeyHolder to get the generated key after insert.
    // This method is used to create a new user.
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            return ps;
        }, keyHolder);
        
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    public List<User> search(String query, String sortBy, String sortDirection) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE username LIKE ?");

        String direction = "desc".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";

        if ("role".equals(sortBy)) {
            sql.append(" ORDER BY role ").append(direction)
            .append(", username ").append(direction);
        } else if ("id".equals(sortBy)) {
            sql.append(" ORDER BY id ").append(direction);
        } else {
            sql.append(" ORDER BY username ").append(direction);
        }

        String searchParam = "%" + query + "%";
        return jdbcTemplate.query(sql.toString(), userRowMapper, searchParam);
    }


    public User update(User user) {
        String sql = "UPDATE users SET username = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getUsername(), user.getRole(), user.getId());
        return user;
    }

    public void updatePassword(Long userId, String newPasswordHash) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, newPasswordHash, userId);
    }

    public void deleteById(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public boolean usernameExists(String username, Long excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username, excludeUserId);
        return count != null && count > 0;
    }
}
