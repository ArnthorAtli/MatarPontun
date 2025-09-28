package is.hi.matarpontun.repository;

import is.hi.matarpontun.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Long> {
    Optional<Ward> findByWardName(String wardName);
    Optional<Ward> findByWardNameAndPassword(String wardName, String password);

}