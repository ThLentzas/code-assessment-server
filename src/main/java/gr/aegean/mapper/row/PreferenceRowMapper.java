package gr.aegean.mapper.row;

import gr.aegean.entity.Preference;
import gr.aegean.model.analysis.quality.QualityAttribute;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;


public class PreferenceRowMapper implements RowMapper<Preference> {
    @Override
    public Preference mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Preference(
                resultSet.getInt("analysis_id"),
                QualityAttribute.valueOf(resultSet.getString("quality_attribute")),
                resultSet.getDouble("weight"));
    }
}
