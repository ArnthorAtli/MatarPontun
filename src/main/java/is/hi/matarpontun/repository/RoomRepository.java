package is.hi.matarpontun.repository;
import is.hi.matarpontun.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    long countByWard_Id(Long wardId);
    Optional<Room> findByQrCode(String qrCode);
    java.util.List<Room> findByWard_Id(Long wardId);
}
