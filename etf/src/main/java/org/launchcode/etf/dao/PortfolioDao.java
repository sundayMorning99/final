package org.launchcode.etf.dao;

import org.launchcode.etf.model.Portfolio;
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
public class PortfolioDao {
    private final JdbcTemplate jdbcTemplate;

    public PortfolioDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Portfolio> portfolioRowMapper = new RowMapper<Portfolio>() {
        @Override
        public Portfolio mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Portfolio(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("user_id"),
                rs.getBoolean("is_public")
            );
        }
    };

    public List<Portfolio> findAll() {
        String sql = "SELECT * FROM portfolio ORDER BY name";
        return jdbcTemplate.query(sql, portfolioRowMapper);
    }

    public List<Portfolio> findByUserId(Long userId) {
        String sql = "SELECT * FROM portfolio WHERE user_id = ? ORDER BY name";
        return jdbcTemplate.query(sql, portfolioRowMapper, userId);
    }

    public List<Portfolio> findAllSorted(String sortBy, Long userId, boolean isAdmin) {
        StringBuilder sql = new StringBuilder("SELECT * FROM portfolio");
        
        //Admin can see all portofolios, so you don't have to add WHERE clause.
        if (!isAdmin) {
            sql.append(" WHERE user_id = ? OR is_public = TRUE");
        }
        
        if ("userId".equals(sortBy)) {
            sql.append(" ORDER BY user_id");
        } else {
            sql.append(" ORDER BY name");
        }
        
        if (isAdmin) {
            return jdbcTemplate.query(sql.toString(), portfolioRowMapper);
        } else {
            return jdbcTemplate.query(sql.toString(), portfolioRowMapper, userId);
        }
    }

    public List<Portfolio> findPublicPortfolios() {
        String sql = "SELECT * FROM portfolio WHERE is_public = TRUE ORDER BY name";
        return jdbcTemplate.query(sql, portfolioRowMapper);
    }

    public List<Portfolio> findByUserIdOrPublic(Long userId) {
        String sql = "SELECT * FROM portfolio WHERE user_id = ? OR is_public = TRUE ORDER BY name";
        return jdbcTemplate.query(sql, portfolioRowMapper, userId);
    }

    public List<Portfolio> search(String query, String sortBy, Long userId, boolean isAdmin) {
        StringBuilder sql = new StringBuilder("SELECT * FROM portfolio WHERE ");
        
        if (!isAdmin) {
            sql.append("(user_id = ? OR is_public = TRUE) AND ");
        }
        
        sql.append("name LIKE ? ");
        
        if ("userId".equals(sortBy)) {
            sql.append("ORDER BY user_id");
        } else {
            sql.append("ORDER BY name");
        }
        
        String searchParam = "%" + query + "%";
        
        if (isAdmin) {
            return jdbcTemplate.query(sql.toString(), portfolioRowMapper, searchParam);
        } else {
            return jdbcTemplate.query(sql.toString(), portfolioRowMapper, userId, searchParam);
        }
    }

    public Portfolio findById(Long id) {
        String sql = "SELECT * FROM portfolio WHERE id = ?";
        List<Portfolio> portfolios = jdbcTemplate.query(sql, portfolioRowMapper, id);
        return portfolios.isEmpty() ? null : portfolios.get(0);
    }

    // This method will check if the id is already existing.
    // If it is, it will update the existing ETF.
    // If it is not, it will create a new ETF. In controllers, you can just use save method. 
    public Portfolio save(Portfolio portfolio) {
        if (portfolio.getId() == null) {
            return create(portfolio);
        } else {
            return update(portfolio);
        }
    }

    // Again, I used KeyHolder to get the generated key after insert.
    private Portfolio create(Portfolio portfolio) {
        String sql = "INSERT INTO portfolio (name, user_id, is_public) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, portfolio.getName());
            ps.setLong(2, portfolio.getUserId());
            ps.setBoolean(3, portfolio.getIsPublic());
            return ps;
        }, keyHolder);
        
        portfolio.setId(keyHolder.getKey().longValue());
        return portfolio;
    }

    private Portfolio update(Portfolio portfolio) {
        String sql = "UPDATE portfolio SET name = ?, is_public = ? WHERE id = ?";
        jdbcTemplate.update(sql, portfolio.getName(), portfolio.getIsPublic(), portfolio.getId());
        return portfolio;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM portfolio WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
