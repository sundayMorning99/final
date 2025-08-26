package org.launchcode.etf.dao;

import org.launchcode.etf.model.Etf;
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
public class EtfDao {
    private final JdbcTemplate jdbcTemplate;

    public EtfDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Etf> etfRowMapper = new RowMapper<Etf>() {
        @Override
        public Etf mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Etf(
                rs.getLong("id"),
                rs.getString("ticker"),
                rs.getString("description"),
                rs.getString("asset_class"),
                rs.getBigDecimal("expense_ratio"),
                rs.getLong("user_id"),
                rs.getBoolean("is_public")
            );
        }
    };

    public List<Etf> findAll() {
        String sql = "SELECT * FROM etf ORDER BY ticker";
        return jdbcTemplate.query(sql, etfRowMapper);
    }

    public List<Etf> findByUserId(Long userId) {
        String sql = "SELECT * FROM etf WHERE user_id = ? ORDER BY ticker";
        return jdbcTemplate.query(sql, etfRowMapper, userId);
    }

    public List<Etf> findPublicEtfs() {
        String sql = "SELECT * FROM etf WHERE is_public = TRUE ORDER BY ticker";
        return jdbcTemplate.query(sql, etfRowMapper);
    }

    public List<Etf> findByUserIdOrPublic(Long userId) {
        String sql = "SELECT * FROM etf WHERE user_id = ? OR is_public = TRUE ORDER BY ticker";
        return jdbcTemplate.query(sql, etfRowMapper, userId);
    }

    public List<Etf> search(String query, String sortBy, String sortDirection, Long userId, boolean isAdmin) {
        StringBuilder sql = new StringBuilder("SELECT * FROM etf WHERE ");
        
        if (!isAdmin) {
            sql.append("(user_id = ? OR is_public = TRUE) AND ");
        }
        
        sql.append("(ticker LIKE ? OR description LIKE ?) ");
        
        //equalsIgnoreCase to make it case insensitive - "DESC", "desc", "Desc" are all treated the same.
        String direction = "desc".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        
        if ("assetClass".equals(sortBy)) {
            sql.append("ORDER BY asset_class ").append(direction);
        } else {
            sql.append("ORDER BY ticker ").append(direction);
        }
        
        String searchParam = "%" + query + "%";
        
        if (isAdmin) {
            return jdbcTemplate.query(sql.toString(), etfRowMapper, searchParam, searchParam);
        } else {
            return jdbcTemplate.query(sql.toString(), etfRowMapper, userId, searchParam, searchParam);
        }
    }

    public Etf findById(Long id) {
        String sql = "SELECT * FROM etf WHERE id = ?";
        List<Etf> etfs = jdbcTemplate.query(sql, etfRowMapper, id);
        return etfs.isEmpty() ? null : etfs.get(0);
    }

    // This method will check if the id is already existing.
    // If it is, it will update the existing ETF.
    // If it is not, it will create a new ETF. In controllers, you can just use save method. 
    public Etf save(Etf etf) {
        if (etf.getId() == null) {
            return create(etf);
        } else {
            return update(etf);
        }
    }

    // Unlike JpaRepository, we can't get updated id. 
    // So we have to use KeyHolder to get the generated key after insert.
    private Etf create(Etf etf) {
        String sql = "INSERT INTO etf (ticker, description, asset_class, expense_ratio, user_id, is_public) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, etf.getTicker());
            ps.setString(2, etf.getDescription());
            ps.setString(3, etf.getAssetClass());
            ps.setBigDecimal(4, etf.getExpenseRatio());
            ps.setLong(5, etf.getUserId());
            ps.setBoolean(6, etf.getIsPublic());
            return ps;
        }, keyHolder);
        
        etf.setId(keyHolder.getKey().longValue());
        return etf;
    }

    private Etf update(Etf etf) {
        String sql = "UPDATE etf SET ticker = ?, description = ?, asset_class = ?, expense_ratio = ?, is_public = ? WHERE id = ?";
        jdbcTemplate.update(sql, etf.getTicker(), etf.getDescription(), etf.getAssetClass(), 
                           etf.getExpenseRatio(), etf.getIsPublic(), etf.getId());
        return etf;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM etf WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<Etf> findAllSorted(String sortBy, String sortDirection, Long userId, boolean isAdmin) {
        StringBuilder sql = new StringBuilder("SELECT * FROM etf");
        
        if (!isAdmin) {
            sql.append(" WHERE user_id = ? OR is_public = TRUE");
        }

        //Again equalsIgnoreCase to make it case insensitive.
        String direction = "desc".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";

        if ("assetClass".equals(sortBy)) {
            sql.append(" ORDER BY asset_class ").append(direction);
        } else {
            sql.append(" ORDER BY ticker ").append(direction);
        }
        
        if (isAdmin) {
            return jdbcTemplate.query(sql.toString(), etfRowMapper);
        } else {
            return jdbcTemplate.query(sql.toString(), etfRowMapper, userId);
        }
    }
}

