package gr.aegean.mapper.row;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import gr.aegean.entity.EmailUpdateToken;


public class EmailUpdateTokenRowMapper implements RowMapper<EmailUpdateToken> {
    @Override
    public EmailUpdateToken mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("expiry_date");
        LocalDateTime expiryDate = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new EmailUpdateToken(
                resultSet.getInt("user_id"),
                resultSet.getString("token"),
                resultSet.getString("email"),
                expiryDate);
    }
}