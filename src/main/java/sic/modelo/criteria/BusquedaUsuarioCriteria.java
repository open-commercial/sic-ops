package sic.modelo.criteria;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.Rol;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaUsuarioCriteria {

    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private List<Rol> roles;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
}

