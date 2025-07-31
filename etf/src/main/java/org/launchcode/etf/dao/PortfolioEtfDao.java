package org.launchcode.etf.dao;

import org.launchcode.etf.model.PortfolioEtf;
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
public class PortfolioEtfDao {
    private final JdbcTemplate jdbcTemplate;

    public PortfolioEtfDao(JdbcTemplate jdbcTemplate) {
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

    // This method retrieves all ETFs associated with a specific portfolio.
    // It uses a JOIN query to get the ETFs linked to the portfolio through the portfolio_etf table.
    // The method returns a list of Etf objects that are part of the specified portfolio.
    // It is conventional to take the first letter of table names as alias, so 'e' for etf and 'pe' for portfolio_etf.
    public List<Etf> findEtfsByPortfolioId(Long portfolioId) {
        String sql = "SELECT e.* FROM etf e JOIN portfolio_etf pe ON e.id = pe.etf_id WHERE pe.portfolio_id = ?";
        return jdbcTemplate.query(sql, etfRowMapper, portfolioId);
    }

    public PortfolioEtf addEtfToPortfolio(Long portfolioId, Long etfId) {
        String sql = "INSERT INTO portfolio_etf (portfolio_id, etf_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, portfolioId);
            ps.setLong(2, etfId);
            return ps;
        }, keyHolder);
        
        return new PortfolioEtf(keyHolder.getKey().longValue(), portfolioId, etfId);
    }

    public void removeEtfFromPortfolio(Long portfolioId, Long etfId) {
        String sql = "DELETE FROM portfolio_etf WHERE portfolio_id = ? AND etf_id = ?";
        jdbcTemplate.update(sql, portfolioId, etfId);
    }

    public boolean existsByPortfolioIdAndEtfId(Long portfolioId, Long etfId) {
        String sql = "SELECT COUNT(*) FROM portfolio_etf WHERE portfolio_id = ? AND etf_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, portfolioId, etfId);
        return count != null && count > 0;
    }
}
