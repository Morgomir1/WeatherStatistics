package weatherStatistics.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import weatherStatistics.entity.WeatherStat;

import java.util.List;

@Repository
public interface WeatherStatRepo extends JpaRepository<WeatherStat, String> {

}
