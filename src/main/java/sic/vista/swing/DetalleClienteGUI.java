package sic.vista.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import sic.RestClient;
import sic.modelo.CategoriaIVA;
import sic.modelo.Cliente;
import sic.modelo.EmpresaActiva;
import sic.modelo.Localidad;
import sic.modelo.Pais;
import sic.modelo.Provincia;
import sic.modelo.Rol;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.modelo.UsuarioActivo;
import java.math.BigDecimal;

public class DetalleClienteGUI extends JDialog {

    private Cliente cliente = new Cliente();
    private List<Pais> paises;
    private List<Provincia> provincias;
    private List<Localidad> localidades;
    private final TipoDeOperacion operacion;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public DetalleClienteGUI() {
        this.initComponents();
        this.setIcon();
        operacion = TipoDeOperacion.ALTA;
    }

    public DetalleClienteGUI(Cliente cliente) {
        this.initComponents();
        this.setIcon();
        operacion = TipoDeOperacion.ACTUALIZACION;
        this.cliente = cliente;
    }

    public Cliente getClienteDadoDeAlta() {
        return (cliente.getId_Cliente() != 0L ? cliente : null);
    }

    private void setIcon() {
        ImageIcon iconoVentana = new ImageIcon(DetalleClienteGUI.class.getResource("/sic/icons/Client_16x16.png"));
        this.setIconImage(iconoVentana.getImage());
    }

    private void seleccionarCredencialSegunId(Long idCredencial) {
        if (idCredencial != null) {
            try {
                Usuario usuario = RestClient.getRestTemplate().getForObject("/usuarios/"
                        + idCredencial, Usuario.class);
                cmbCredencial.addItem(usuario);
                cmbCredencial.addItem(null);
            } catch (RestClientResponseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ResourceAccessException ex) {
                LOGGER.error(ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarViajanteSegunId(Long idViajante) {
        if (idViajante != null) {
            try {
                Usuario usuario = RestClient.getRestTemplate().getForObject("/usuarios/"
                        + idViajante, Usuario.class);
                cmbViajante.addItem(usuario);
                cmbViajante.addItem(null);
                cmbCredencial.addItem(usuario);
                cmbCredencial.addItem(null);
            } catch (RestClientResponseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ResourceAccessException ex) {
                LOGGER.error(ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarPaisSegunId(Long idPais) {
        int indice = 0;
        for (int i = 0; i < paises.size(); i++) {
            if (paises.get(i).getId_Pais() == idPais) {
                indice = i;
            }
        }
        cmbPais.setSelectedItem(paises.get(indice));
    }

    private void seleccionarProvinciaSegunId(Long idProvincia) {
        int indice = 0;
        for (int i = 0; i < provincias.size(); i++) {
            if (provincias.get(i).getId_Provincia() == idProvincia) {
                indice = i;
            }
        }
        cmbProvincia.setSelectedItem(provincias.get(indice));
    }

    private void seleccionarLocalidadSegunId(Long idLocalidad) {
        int indice = 0;
        for (int i = 0; i < localidades.size(); i++) {
            if (localidades.get(i).getId_Localidad() == idLocalidad) {
                indice = i;
            }
        }
        cmbLocalidad.setSelectedItem(localidades.get(indice));
    }

    private void cargarClienteParaModificar() {
        txtIdFiscal.setValue(cliente.getIdFiscal());
        txtNombreFiscal.setText(cliente.getNombreFiscal());
        txtNombreFantasia.setText(cliente.getNombreFantasia());
        txtBonificacion.setValue(cliente.getBonificacion());
        cmbCategoriaIVA.setSelectedItem(cliente.getCategoriaIVA());
        txtDireccion.setText(cliente.getDireccion());
        if (cliente.getIdLocalidad() != null) {
            this.seleccionarPaisSegunId(cliente.getIdPais());
            this.seleccionarProvinciaSegunId(cliente.getIdProvincia());
            this.seleccionarLocalidadSegunId(cliente.getIdLocalidad());
        }
        this.seleccionarCredencialSegunId(cliente.getIdCredencial());
        this.seleccionarViajanteSegunId(cliente.getIdViajante());
        txtTelefono.setText(cliente.getTelefono());
        txtContacto.setText(cliente.getContacto());
        txtEmail.setText(cliente.getEmail());
    }

    private void cargarComboBoxCategoriasIVA() {
        cmbCategoriaIVA.removeAllItems();
        for (CategoriaIVA c : CategoriaIVA.values()) {
            cmbCategoriaIVA.addItem(c);
        }
    }

    private void cargarComboBoxPaises() {
        cmbPais.removeAllItems();
        try {
            paises = new ArrayList(Arrays.asList(RestClient.getRestTemplate()
                    .getForObject("/paises", Pais[].class)));
            Pais paisVacio = new Pais();
            paisVacio.setNombre("");
            cmbPais.addItem(paisVacio);
            paises.stream().forEach(p -> cmbPais.addItem(p));
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarComboBoxProvinciasDelPais(Pais pais) {
        cmbProvincia.removeAllItems();
        try {
            if (!pais.getNombre().equals("")) {
                provincias = new ArrayList(Arrays.asList(RestClient.getRestTemplate()
                        .getForObject("/provincias/paises/" + pais.getId_Pais(),
                                Provincia[].class)));
                provincias.stream().forEach(p -> cmbProvincia.addItem(p));
            }
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarComboBoxLocalidadesDeLaProvincia(Provincia provincia) {
        cmbLocalidad.removeAllItems();
        try {
            if (!provincia.getNombre().equals("")) {
                localidades = new ArrayList(Arrays.asList(RestClient.getRestTemplate()
                        .getForObject("/localidades/provincias/" + provincia.getId_Provincia(),
                                Localidad[].class)));
                localidades.stream().forEach(l -> cmbLocalidad.addItem(l));
            }
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstadoDeComponentesSegunRolUsuario() {
        List<Rol> rolesDeUsuarioActivo = UsuarioActivo.getInstance().getUsuario().getRoles();
        if (rolesDeUsuarioActivo.contains(Rol.ADMINISTRADOR)) {
            btnNuevoUsuarioViajante.setEnabled(true);
            lblCredencial.setEnabled(true);
            cmbCredencial.setEnabled(true);
        } else {
            btnNuevoUsuarioViajante.setEnabled(false);
            lblCredencial.setEnabled(false);
            cmbCredencial.setEnabled(false);
        }
        if (rolesDeUsuarioActivo.contains(Rol.ADMINISTRADOR)
                || rolesDeUsuarioActivo.contains(Rol.ENCARGADO)) {
            btnNuevaLocalidad.setEnabled(true);
            btnNuevaProvincia.setEnabled(true);
            btnNuevoPais.setEnabled(true);
            lblViajante.setEnabled(true);
            cmbViajante.setEnabled(true);
            btnBuscarUsuarioViajante.setEnabled(true);
            lblCredencial.setEnabled(true);
            cmbCredencial.setEnabled(true);
            btnBuscarCredencial.setEnabled(true);
            lblBonificacion.setEnabled(true);
            txtBonificacion.setEnabled(true);
        } else {
            btnNuevaLocalidad.setEnabled(false);
            btnNuevaProvincia.setEnabled(false);
            btnNuevoPais.setEnabled(false);
            lblViajante.setEnabled(false);
            cmbViajante.setEnabled(false);
            btnBuscarUsuarioViajante.setEnabled(false);
            lblCredencial.setEnabled(false);
            cmbCredencial.setEnabled(false);
            btnBuscarCredencial.setEnabled(false);
            lblBonificacion.setEnabled(false);
            txtBonificacion.setEnabled(false);
        }
        if (rolesDeUsuarioActivo.contains(Rol.ADMINISTRADOR)
                || rolesDeUsuarioActivo.contains(Rol.ENCARGADO)
                || rolesDeUsuarioActivo.contains(Rol.VENDEDOR)) {
            btnNuevaCredencial.setEnabled(true);
            btnBuscarCredencial.setEnabled(true);
            lblCredencial.setEnabled(true);
            cmbCredencial.setEnabled(true);
        } else {
            btnNuevaCredencial.setEnabled(false);
            btnBuscarCredencial.setEnabled(false);
        }
        if (rolesDeUsuarioActivo.contains(Rol.VIAJANTE)
                && !rolesDeUsuarioActivo.contains(Rol.VENDEDOR)
                && !rolesDeUsuarioActivo.contains(Rol.ADMINISTRADOR)
                && !rolesDeUsuarioActivo.contains(Rol.ENCARGADO)) {
            this.seleccionarViajanteSegunId(UsuarioActivo.getInstance().getUsuario().getId_Usuario());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btn_Guardar = new javax.swing.JButton();
        panelPrincipal = new javax.swing.JPanel();
        lblBonificacion = new javax.swing.JLabel();
        txtBonificacion = new javax.swing.JFormattedTextField();
        lblIdFiscal = new javax.swing.JLabel();
        txtIdFiscal = new javax.swing.JFormattedTextField();
        lblNombreFiscal = new javax.swing.JLabel();
        txtNombreFiscal = new javax.swing.JTextField();
        lblNombreFantasia = new javax.swing.JLabel();
        txtNombreFantasia = new javax.swing.JTextField();
        lblCondicionIVA = new javax.swing.JLabel();
        cmbCategoriaIVA = new javax.swing.JComboBox<>();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        lblPais = new javax.swing.JLabel();
        cmbPais = new javax.swing.JComboBox<>();
        btnNuevoPais = new javax.swing.JButton();
        lblProvincia = new javax.swing.JLabel();
        cmbProvincia = new javax.swing.JComboBox<>();
        btnNuevaProvincia = new javax.swing.JButton();
        lblLocalidad = new javax.swing.JLabel();
        cmbLocalidad = new javax.swing.JComboBox<>();
        btnNuevaLocalidad = new javax.swing.JButton();
        lblViajante = new javax.swing.JLabel();
        cmbViajante = new javax.swing.JComboBox<>();
        btnBuscarUsuarioViajante = new javax.swing.JButton();
        btnNuevoUsuarioViajante = new javax.swing.JButton();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblContacto = new javax.swing.JLabel();
        txtContacto = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblCredencial = new javax.swing.JLabel();
        cmbCredencial = new javax.swing.JComboBox<>();
        btnBuscarCredencial = new javax.swing.JButton();
        btnNuevaCredencial = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Nuevo Cliente");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        btn_Guardar.setForeground(java.awt.Color.blue);
        btn_Guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/Accept_16x16.png"))); // NOI18N
        btn_Guardar.setText("Guardar");
        btn_Guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_GuardarActionPerformed(evt);
            }
        });

        panelPrincipal.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblBonificacion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBonificacion.setText("% Bonificación:");

        txtBonificacion.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.##"))));
        txtBonificacion.setValue(0);

        lblIdFiscal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblIdFiscal.setText("CUIT o DNI:");

        txtIdFiscal.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#"))));
        txtIdFiscal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtIdFiscalFocusLost(evt);
            }
        });

        lblNombreFiscal.setForeground(java.awt.Color.red);
        lblNombreFiscal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNombreFiscal.setText("* R. Social o Nombre:");

        lblNombreFantasia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNombreFantasia.setText("Nombre Fantasia:");

        lblCondicionIVA.setForeground(java.awt.Color.red);
        lblCondicionIVA.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCondicionIVA.setText("* Categoria IVA:");

        lblDireccion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDireccion.setText("Dirección:");

        lblPais.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPais.setText("Pais:");

        cmbPais.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbPaisItemStateChanged(evt);
            }
        });

        btnNuevoPais.setForeground(java.awt.Color.blue);
        btnNuevoPais.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/AddMap_16x16.png"))); // NOI18N
        btnNuevoPais.setText("Nuevo");
        btnNuevoPais.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoPaisActionPerformed(evt);
            }
        });

        lblProvincia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblProvincia.setText("Provincia:");

        cmbProvincia.setMaximumRowCount(5);
        cmbProvincia.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbProvinciaItemStateChanged(evt);
            }
        });

        btnNuevaProvincia.setForeground(java.awt.Color.blue);
        btnNuevaProvincia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/AddMap_16x16.png"))); // NOI18N
        btnNuevaProvincia.setText("Nueva");
        btnNuevaProvincia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaProvinciaActionPerformed(evt);
            }
        });

        lblLocalidad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLocalidad.setText("Localidad:");

        btnNuevaLocalidad.setForeground(java.awt.Color.blue);
        btnNuevaLocalidad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/AddMap_16x16.png"))); // NOI18N
        btnNuevaLocalidad.setText("Nueva");
        btnNuevaLocalidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaLocalidadActionPerformed(evt);
            }
        });

        lblViajante.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblViajante.setText("Viajante:");

        cmbViajante.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbViajanteItemStateChanged(evt);
            }
        });

        btnBuscarUsuarioViajante.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/Search_16x16.png"))); // NOI18N
        btnBuscarUsuarioViajante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarUsuarioViajanteActionPerformed(evt);
            }
        });

        btnNuevoUsuarioViajante.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/AddGroup_16x16.png"))); // NOI18N
        btnNuevoUsuarioViajante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoUsuarioViajanteActionPerformed(evt);
            }
        });

        lblTelefono.setForeground(java.awt.Color.red);
        lblTelefono.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTelefono.setText("* Teléfono:");

        lblContacto.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblContacto.setText("Contacto:");

        lblEmail.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEmail.setText("Email:");

        lblCredencial.setForeground(java.awt.Color.red);
        lblCredencial.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCredencial.setText("* Credencial:");

        cmbCredencial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCredencialItemStateChanged(evt);
            }
        });

        btnBuscarCredencial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/Search_16x16.png"))); // NOI18N
        btnBuscarCredencial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarCredencialActionPerformed(evt);
            }
        });

        btnNuevaCredencial.setForeground(java.awt.Color.blue);
        btnNuevaCredencial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/AddGroup_16x16.png"))); // NOI18N
        btnNuevaCredencial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaCredencialActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBonificacion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNombreFiscal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNombreFantasia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblIdFiscal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCondicionIVA, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDireccion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPais, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblProvincia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLocalidad, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblViajante, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTelefono, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblContacto, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEmail, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCredencial, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipalLayout.createSequentialGroup()
                        .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbProvincia, 0, 295, Short.MAX_VALUE)
                            .addComponent(cmbPais, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbViajante, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbLocalidad, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbCredencial, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, 0)
                        .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(panelPrincipalLayout.createSequentialGroup()
                                .addComponent(btnBuscarUsuarioViajante)
                                .addGap(0, 0, 0)
                                .addComponent(btnNuevoUsuarioViajante))
                            .addGroup(panelPrincipalLayout.createSequentialGroup()
                                .addComponent(btnBuscarCredencial)
                                .addGap(0, 0, 0)
                                .addComponent(btnNuevaCredencial))
                            .addComponent(btnNuevaProvincia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNuevoPais, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNuevaLocalidad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(txtBonificacion)
                    .addComponent(txtIdFiscal)
                    .addComponent(txtNombreFiscal)
                    .addComponent(txtNombreFantasia)
                    .addComponent(cmbCategoriaIVA, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtDireccion)
                    .addComponent(txtTelefono)
                    .addComponent(txtContacto)
                    .addComponent(txtEmail))
                .addContainerGap())
        );

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblBonificacion, lblIdFiscal, lblNombreFantasia, lblNombreFiscal});

        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBonificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBonificacion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblNombreFiscal)
                    .addComponent(txtNombreFiscal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblNombreFantasia)
                    .addComponent(txtNombreFantasia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblIdFiscal)
                    .addComponent(txtIdFiscal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCondicionIVA)
                    .addComponent(cmbCategoriaIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblDireccion)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblPais)
                    .addComponent(cmbPais, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevoPais))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProvincia)
                    .addComponent(cmbProvincia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevaProvincia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblLocalidad)
                    .addComponent(cmbLocalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevaLocalidad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblViajante)
                    .addComponent(cmbViajante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarUsuarioViajante)
                    .addComponent(btnNuevoUsuarioViajante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblTelefono)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblContacto)
                    .addComponent(txtContacto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblEmail)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCredencial)
                    .addComponent(cmbCredencial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarCredencial)
                    .addComponent(btnNuevaCredencial))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnNuevoPais, cmbPais});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnNuevaProvincia, cmbProvincia});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnNuevaLocalidad, cmbLocalidad, cmbViajante});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblCredencial, lblTelefono, lblViajante});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnBuscarCredencial, btnNuevaCredencial, cmbCredencial});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnBuscarUsuarioViajante, btnNuevoUsuarioViajante});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblBonificacion, lblIdFiscal});

        panelPrincipalLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {txtBonificacion, txtIdFiscal});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_Guardar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_Guardar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoPaisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoPaisActionPerformed
        DetallePaisGUI gui_DetallePais = new DetallePaisGUI();
        gui_DetallePais.setModal(true);
        gui_DetallePais.setLocationRelativeTo(this);
        gui_DetallePais.setVisible(true);
        this.cargarComboBoxPaises();
        this.cargarComboBoxProvinciasDelPais(cmbPais.getItemAt(cmbPais.getSelectedIndex()));
    }//GEN-LAST:event_btnNuevoPaisActionPerformed

    private void btnNuevaProvinciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaProvinciaActionPerformed
        DetalleProvinciaGUI gui_DetalleProvincia = new DetalleProvinciaGUI();
        gui_DetalleProvincia.setModal(true);
        gui_DetalleProvincia.setLocationRelativeTo(this);
        gui_DetalleProvincia.setVisible(true);
        this.cargarComboBoxProvinciasDelPais(cmbPais.getItemAt(cmbPais.getSelectedIndex()));
    }//GEN-LAST:event_btnNuevaProvinciaActionPerformed

    private void cmbPaisItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbPaisItemStateChanged
        if (cmbPais.getItemCount() > 0) {
            this.cargarComboBoxProvinciasDelPais(cmbPais.getItemAt(cmbPais.getSelectedIndex()));
        }
    }//GEN-LAST:event_cmbPaisItemStateChanged

    private void btnNuevaLocalidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaLocalidadActionPerformed
        DetalleLocalidadGUI gui_DetalleLocalidad = new DetalleLocalidadGUI();
        gui_DetalleLocalidad.setModal(true);
        gui_DetalleLocalidad.setLocationRelativeTo(this);
        gui_DetalleLocalidad.setVisible(true);
        this.cargarComboBoxLocalidadesDeLaProvincia(cmbProvincia.getItemAt(cmbProvincia.getSelectedIndex()));
    }//GEN-LAST:event_btnNuevaLocalidadActionPerformed

    private void cmbProvinciaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbProvinciaItemStateChanged
        if (cmbProvincia.getItemCount() > 0) {
            this.cargarComboBoxLocalidadesDeLaProvincia(cmbProvincia.getItemAt(cmbProvincia.getSelectedIndex()));
        } else {
            cmbLocalidad.removeAllItems();
        }
    }//GEN-LAST:event_cmbProvinciaItemStateChanged

    private void btn_GuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_GuardarActionPerformed
        try {
            String idLocalidad = "";
            String idViajante = "";
            String idCredencial = "";
            cliente.setIdFiscal((Long) txtIdFiscal.getValue());
            cliente.setNombreFiscal(txtNombreFiscal.getText().trim());
            cliente.setNombreFantasia(txtNombreFantasia.getText().trim());
            cliente.setBonificacion(new BigDecimal(txtBonificacion.getText()));
            cliente.setCategoriaIVA((CategoriaIVA) cmbCategoriaIVA.getSelectedItem());
            cliente.setDireccion(txtDireccion.getText().trim());
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setContacto(txtContacto.getText().trim());
            cliente.setEmail(txtEmail.getText().trim());
            if (cmbLocalidad.getSelectedItem() != null) {
                idLocalidad = String.valueOf(
                        ((Localidad) cmbLocalidad.getSelectedItem()).getId_Localidad());
            }
            if (cmbViajante.getSelectedItem() != null) {
                idViajante = String.valueOf(
                        ((Usuario) cmbViajante.getSelectedItem()).getId_Usuario());
            }
            if (cmbCredencial.getSelectedItem() != null) {
                idCredencial = String.valueOf(
                        ((Usuario) cmbCredencial.getSelectedItem()).getId_Usuario());
            }
            if (operacion == TipoDeOperacion.ALTA) {
                cliente = RestClient.getRestTemplate().postForObject(
                        "/clientes?idEmpresa=" + EmpresaActiva.getInstance().getEmpresa().getId_Empresa()
                        + "&idLocalidad=" + idLocalidad
                        + "&idViajante=" + idViajante
                        + "&idCredencial=" + idCredencial,
                        cliente, Cliente.class);
                JOptionPane.showMessageDialog(this, "El Cliente se guardó correctamente!",
                        "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
            if (operacion == TipoDeOperacion.ACTUALIZACION) {
                RestClient.getRestTemplate().put(
                        "/clientes?idEmpresa=" + EmpresaActiva.getInstance().getEmpresa().getId_Empresa()
                        + "&idLocalidad=" + idLocalidad
                        + "&idViajante=" + idViajante
                        + "&idCredencial=" + idCredencial,
                        cliente);
                JOptionPane.showMessageDialog(this, "El Cliente se modificó correctamente!",
                        "Aviso", JOptionPane.INFORMATION_MESSAGE);

            }
            this.dispose();
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btn_GuardarActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.cargarComboBoxCategoriasIVA();
        this.cargarComboBoxPaises();
        this.cambiarEstadoDeComponentesSegunRolUsuario();
        if (operacion == TipoDeOperacion.ACTUALIZACION) {
            this.setTitle("Modificar Cliente Nº " + cliente.getNroCliente());
            this.cargarClienteParaModificar();
        } else if (operacion == TipoDeOperacion.ALTA) {
            this.setTitle("Nuevo Cliente");
        }
    }//GEN-LAST:event_formWindowOpened

    private void btnNuevaCredencialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaCredencialActionPerformed
        DetalleUsuarioGUI gui_DetalleUsuario = new DetalleUsuarioGUI(Rol.COMPRADOR);
        gui_DetalleUsuario.setModal(true);
        gui_DetalleUsuario.setLocationRelativeTo(this);
        gui_DetalleUsuario.setVisible(true);
        if (gui_DetalleUsuario.getUsuarioCreado() != null && gui_DetalleUsuario.getUsuarioCreado().getRoles().contains(Rol.COMPRADOR)) {
            cmbCredencial.removeAllItems();
            cmbCredencial.addItem(gui_DetalleUsuario.getUsuarioCreado());
            cmbCredencial.addItem(null);
            cmbCredencial.setSelectedIndex(0);
        }
    }//GEN-LAST:event_btnNuevaCredencialActionPerformed

    private void btnBuscarUsuarioViajanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarUsuarioViajanteActionPerformed
        BuscarUsuariosGUI buscarUsuariosGUI = new BuscarUsuariosGUI(new Rol[]{Rol.VIAJANTE});
        buscarUsuariosGUI.setModal(true);
        buscarUsuariosGUI.setLocationRelativeTo(this);
        buscarUsuariosGUI.setVisible(true);
        if (buscarUsuariosGUI.getUsuarioSeleccionado() != null) {
            cmbViajante.removeAllItems();
            cmbViajante.addItem(buscarUsuariosGUI.getUsuarioSeleccionado());
            cmbViajante.addItem(null);
        }
    }//GEN-LAST:event_btnBuscarUsuarioViajanteActionPerformed

    private void cmbViajanteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbViajanteItemStateChanged
        if (cmbViajante.getSelectedItem() == null) {
            cmbViajante.removeAllItems();
        }
    }//GEN-LAST:event_cmbViajanteItemStateChanged

    private void cmbCredencialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbCredencialItemStateChanged
        if (cmbCredencial.getSelectedItem() == null) {
            cmbCredencial.removeAllItems();
        }
    }//GEN-LAST:event_cmbCredencialItemStateChanged

    private void btnBuscarCredencialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarCredencialActionPerformed
        Rol[] rolesParaFiltrar = new Rol[]{Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR};
        BuscarUsuariosGUI buscarUsuariosGUI = new BuscarUsuariosGUI(rolesParaFiltrar);
        buscarUsuariosGUI.setModal(true);
        buscarUsuariosGUI.setLocationRelativeTo(this);
        buscarUsuariosGUI.setVisible(true);
        if (buscarUsuariosGUI.getUsuarioSeleccionado() != null) {
            cmbCredencial.removeAllItems();
            cmbCredencial.addItem(buscarUsuariosGUI.getUsuarioSeleccionado());
            cmbCredencial.addItem(null);
        }
    }//GEN-LAST:event_btnBuscarCredencialActionPerformed

    private void btnNuevoUsuarioViajanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoUsuarioViajanteActionPerformed
        DetalleUsuarioGUI gui_DetalleUsuario = new DetalleUsuarioGUI(Rol.VIAJANTE);
        gui_DetalleUsuario.setModal(true);
        gui_DetalleUsuario.setLocationRelativeTo(this);
        gui_DetalleUsuario.setVisible(true);
        if (gui_DetalleUsuario.getUsuarioCreado() != null && gui_DetalleUsuario.getUsuarioCreado().getRoles().contains(Rol.VIAJANTE)) {
            cmbViajante.removeAllItems();
            cmbViajante.addItem(gui_DetalleUsuario.getUsuarioCreado());
            cmbViajante.addItem(null);
            cmbViajante.setSelectedIndex(0);
        }
    }//GEN-LAST:event_btnNuevoUsuarioViajanteActionPerformed

    private void txtIdFiscalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtIdFiscalFocusLost
        if (txtIdFiscal.getText().equals("")) {
            txtIdFiscal.setValue(null);
        }
    }//GEN-LAST:event_txtIdFiscalFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarCredencial;
    private javax.swing.JButton btnBuscarUsuarioViajante;
    private javax.swing.JButton btnNuevaCredencial;
    private javax.swing.JButton btnNuevaLocalidad;
    private javax.swing.JButton btnNuevaProvincia;
    private javax.swing.JButton btnNuevoPais;
    private javax.swing.JButton btnNuevoUsuarioViajante;
    private javax.swing.JButton btn_Guardar;
    private javax.swing.JComboBox<CategoriaIVA> cmbCategoriaIVA;
    private javax.swing.JComboBox<Usuario> cmbCredencial;
    private javax.swing.JComboBox<Localidad> cmbLocalidad;
    private javax.swing.JComboBox<Pais> cmbPais;
    private javax.swing.JComboBox<Provincia> cmbProvincia;
    private javax.swing.JComboBox<Usuario> cmbViajante;
    private javax.swing.JLabel lblBonificacion;
    private javax.swing.JLabel lblCondicionIVA;
    private javax.swing.JLabel lblContacto;
    private javax.swing.JLabel lblCredencial;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblIdFiscal;
    private javax.swing.JLabel lblLocalidad;
    private javax.swing.JLabel lblNombreFantasia;
    private javax.swing.JLabel lblNombreFiscal;
    private javax.swing.JLabel lblPais;
    private javax.swing.JLabel lblProvincia;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblViajante;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JFormattedTextField txtBonificacion;
    private javax.swing.JTextField txtContacto;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JFormattedTextField txtIdFiscal;
    private javax.swing.JTextField txtNombreFantasia;
    private javax.swing.JTextField txtNombreFiscal;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
