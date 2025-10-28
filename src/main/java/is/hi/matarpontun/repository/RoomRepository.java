package is.hi.matarpontun.repository;
import is.hi.matarpontun.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomRepository extends JpaRepository<Room, Long> {
    long countByWard_Id(Long wardId);
}
