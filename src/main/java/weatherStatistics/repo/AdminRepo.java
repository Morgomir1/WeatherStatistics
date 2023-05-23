package weatherStatistics.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import weatherStatistics.entity.Admin;

public interface AdminRepo extends JpaRepository<Admin, Long> {

}