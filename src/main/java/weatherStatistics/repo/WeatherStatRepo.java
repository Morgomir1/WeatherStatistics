package weatherStatistics.repo;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import weatherStatistics.entity.WeatherStat;

import java.util.List;

@Repository
public interface WeatherStatRepo extends JpaRepository<WeatherStat, Integer> {

}
