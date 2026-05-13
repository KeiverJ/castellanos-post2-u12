package co.edu.udes.castellanos.post2u12.config;

import java.math.BigDecimal;
import java.util.List;

import co.edu.udes.castellanos.post2u12.domain.Producto;
import co.edu.udes.castellanos.post2u12.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductoDataSeeder implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public ProductoDataSeeder(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        productoRepository.saveAll(List.of(
                new Producto(
                        "Teclado mecanico",
                        "Teclado mecanico con switches tactiles y retroiluminacion",
                        new BigDecimal("159900.00"),
                        12),
                new Producto(
                        "Mouse inalambrico",
                        "Mouse ergonomico con conexion Bluetooth y receptor USB",
                        new BigDecimal("89900.00"),
                        25),
                new Producto(
                        "Monitor 24 pulgadas",
                        "Monitor Full HD con panel IPS y tasa de refresco de 75 Hz",
                        new BigDecimal("749900.00"),
                        8)));
    }
}
