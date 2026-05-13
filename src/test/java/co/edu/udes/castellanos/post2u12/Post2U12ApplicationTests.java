package co.edu.udes.castellanos.post2u12;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class Post2U12ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    // ── Prueba de contexto ──────────────────────────────────────────
    @Test
    void contextLoads() {
    }

    // ── Pruebas del HomeController ──────────────────────────────────
    @Test
    void debeResponderPaginaRaiz() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("API Castellanos Post2 U12")));
    }

    // ── Pruebas del Actuator ────────────────────────────────────────
    @Test
    void debeResponderHealthEnEstadoUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ── Pruebas CRUD de Productos ───────────────────────────────────
    @Test
    void debeListarProductosSembrados() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void debeObtenerProductoPorId() throws Exception {
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").isNotEmpty());
    }

    @Test
    void debeCrearUnProductoNuevo() throws Exception {
        String body = """
                {
                  "nombre": "Tablet 10 pulgadas",
                  "descripcion": "Tablet para estudio y consumo multimedia",
                  "precio": 1299900.00,
                  "stock": 4
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Tablet 10 pulgadas"));
    }

    @Test
    void debeActualizarUnProductoExistente() throws Exception {
        String body = """
                {
                  "nombre": "Teclado actualizado",
                  "descripcion": "Teclado mecanico actualizado con switches rojos",
                  "precio": 179900.00,
                  "stock": 10
                }
                """;

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Teclado actualizado"));
    }

    @Test
    void debeEliminarUnProductoExistente() throws Exception {
        // Crear producto temporal para eliminar
        String body = """
                {
                  "nombre": "Producto temporal",
                  "descripcion": "Producto creado para prueba de eliminacion",
                  "precio": 50000.00,
                  "stock": 1
                }
                """;

        String response = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extraer el ID del producto creado
        String idStr = response.split("\"id\":")[1].split(",")[0];
        long id = Long.parseLong(idStr.trim());

        mockMvc.perform(delete("/api/productos/" + id))
                .andExpect(status().isNoContent());
    }

    // ── Pruebas de validacion y errores ─────────────────────────────
    @Test
    void debeRetornar404CuandoProductoNoExiste() throws Exception {
        mockMvc.perform(get("/api/productos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("no encontrado")));
    }

    @Test
    void debeRetornar400CuandoDatosInvalidos() throws Exception {
        String body = """
                {
                  "nombre": "",
                  "descripcion": "",
                  "precio": -10,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
