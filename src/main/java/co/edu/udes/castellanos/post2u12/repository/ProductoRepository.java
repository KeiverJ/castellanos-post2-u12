package co.edu.udes.castellanos.post2u12.repository;

import co.edu.udes.castellanos.post2u12.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
