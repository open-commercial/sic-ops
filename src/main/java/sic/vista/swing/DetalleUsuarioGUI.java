package sic.vista.swing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import sic.RestClient;
import sic.modelo.Cliente;
import sic.modelo.EmpresaActiva;
import sic.modelo.PaginaRespuestaRest;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.modelo.TipoDeOperacion;

public class DetalleUsuarioGUI extends JDialog {
    
    private Usuario usuarioParaModificar;
    private final TipoDeOperacion operacion;    
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public DetalleUsuarioGUI() {
        this.initComponents();
        operacion = TipoDeOperacion.ALTA;
        this.setIcon();
        lblAvisoSeguridad.setVisible(false);        
    }

    public DetalleUsuarioGUI(Usuario usuario) {
        this.initComponents();
        this.usuarioParaModificar = usuario;
        operacion = TipoDeOperacion.ACTUALIZACION;        
        this.setIcon();
        lbl_Contrasenia.setForeground(Color.BLACK);
        lbl_RepetirContrasenia.setForeground(Color.BLACK);
    }    

    private void setIcon() {
        ImageIcon iconoVentana = new ImageIcon(DetalleUsuarioGUI.class.getResource("/sic/icons/Group_16x16.png"));
        this.setIconImage(iconoVentana.getImage());
    }
    
    private void cargarUsuarioParaModificar() {
        txtNombre.setText(usuarioParaModificar.getNombre());
        txtApellido.setText(usuarioParaModificar.getApellido());
        txtEmail.setText(usuarioParaModificar.getEmail());
        txtUsername.setText(usuarioParaModificar.getUsername());
        chkHabilitado.setSelected(usuarioParaModificar.isHabilitado());
        usuarioParaModificar.getRoles().forEach(rol -> {
            if (Rol.ADMINISTRADOR.equals(rol)) {
                chk_Administrador.setSelected(true);
            }
            if (Rol.VENDEDOR.equals(rol)) {
                chk_Vendedor.setSelected(true);
            }
            if (Rol.VIAJANTE.equals(rol)) {
                chk_Viajante.setSelected(true);
            }
            if (Rol.CLIENTE.equals(rol)) {
                chk_Cliente.setSelected(true);
                cmb_Cliente.setEnabled(true);
                this.panelClientes.setEnabled(true);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btn_Guardar = new javax.swing.JButton();
        panelPrincipal = new javax.swing.JPanel();
        lbl_Username = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        panelRoles = new javax.swing.JPanel();
        chk_Administrador = new javax.swing.JCheckBox();
        chk_Viajante = new javax.swing.JCheckBox();
        chk_Vendedor = new javax.swing.JCheckBox();
        chk_Cliente = new javax.swing.JCheckBox();
        lblNombre = new javax.swing.JLabel();
        lblApellido = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtApellido = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        chkHabilitado = new javax.swing.JCheckBox();
        lblHabilitado = new javax.swing.JLabel();
        panelSeguridad = new javax.swing.JPanel();
        lbl_Contrasenia = new javax.swing.JLabel();
        txt_Contrasenia = new javax.swing.JPasswordField();
        lbl_RepetirContrasenia = new javax.swing.JLabel();
        txt_RepetirContrasenia = new javax.swing.JPasswordField();
        lblAvisoSeguridad = new javax.swing.JLabel();
        panelClientes = new javax.swing.JPanel();
        cmb_Cliente = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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

        panelPrincipal.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lbl_Username.setForeground(java.awt.Color.red);
        lbl_Username.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_Username.setText("* Usuario:");

        panelRoles.setBorder(javax.swing.BorderFactory.createTitledBorder("Roles"));

        chk_Administrador.setText("Administrador");
        chk_Administrador.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chk_Administrador.setMargin(new java.awt.Insets(2, -2, 2, 2));

        chk_Viajante.setText("Viajante");
        chk_Viajante.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chk_Viajante.setMargin(new java.awt.Insets(2, -2, 2, 2));

        chk_Vendedor.setText("Vendedor");
        chk_Vendedor.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chk_Vendedor.setMargin(new java.awt.Insets(2, -2, 2, 2));

        chk_Cliente.setText("Cliente");
        chk_Cliente.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chk_ClienteItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout panelRolesLayout = new javax.swing.GroupLayout(panelRoles);
        panelRoles.setLayout(panelRolesLayout);
        panelRolesLayout.setHorizontalGroup(
            panelRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRolesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chk_Cliente, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chk_Administrador, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chk_Viajante, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chk_Vendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelRolesLayout.setVerticalGroup(
            panelRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRolesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chk_Administrador)
                    .addComponent(chk_Viajante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chk_Cliente)
                    .addComponent(chk_Vendedor))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblNombre.setForeground(java.awt.Color.red);
        lblNombre.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNombre.setText("* Nombre:");

        lblApellido.setForeground(java.awt.Color.red);
        lblApellido.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblApellido.setText("* Apellido:");

        lblEmail.setForeground(java.awt.Color.red);
        lblEmail.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEmail.setText("* Email:");

        chkHabilitado.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        lblHabilitado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblHabilitado.setText("Habilitado:");

        panelSeguridad.setBorder(javax.swing.BorderFactory.createTitledBorder("Seguridad"));

        lbl_Contrasenia.setForeground(java.awt.Color.red);
        lbl_Contrasenia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_Contrasenia.setText("* Contraseña:");

        txt_Contrasenia.setPreferredSize(new java.awt.Dimension(125, 20));

        lbl_RepetirContrasenia.setForeground(java.awt.Color.red);
        lbl_RepetirContrasenia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_RepetirContrasenia.setText("* Repetir:");

        txt_RepetirContrasenia.setPreferredSize(new java.awt.Dimension(125, 20));

        lblAvisoSeguridad.setText("(Dejar en blanco para mantener la actual)");

        javax.swing.GroupLayout panelSeguridadLayout = new javax.swing.GroupLayout(panelSeguridad);
        panelSeguridad.setLayout(panelSeguridadLayout);
        panelSeguridadLayout.setHorizontalGroup(
            panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSeguridadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAvisoSeguridad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelSeguridadLayout.createSequentialGroup()
                        .addGroup(panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lbl_RepetirContrasenia, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbl_Contrasenia))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_Contrasenia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txt_RepetirContrasenia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        panelSeguridadLayout.setVerticalGroup(
            panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSeguridadLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAvisoSeguridad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lbl_Contrasenia)
                    .addComponent(txt_Contrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lbl_RepetirContrasenia)
                    .addComponent(txt_RepetirContrasenia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelClientes.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente asignado"));

        javax.swing.GroupLayout panelClientesLayout = new javax.swing.GroupLayout(panelClientes);
        panelClientes.setLayout(panelClientesLayout);
        panelClientesLayout.setHorizontalGroup(
            panelClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmb_Cliente, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelClientesLayout.setVerticalGroup(
            panelClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelClientesLayout.createSequentialGroup()
                .addComponent(cmb_Cliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPrincipalLayout.createSequentialGroup()
                        .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lbl_Username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblHabilitado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblNombre, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblApellido, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblEmail, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtApellido, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtNombre)
                            .addComponent(txtEmail)
                            .addComponent(chkHabilitado, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtUsername)))
                    .addComponent(panelRoles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelSeguridad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lbl_Username)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblNombre)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblApellido)
                    .addComponent(txtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblEmail)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblHabilitado)
                    .addComponent(chkHabilitado))
                .addGap(18, 18, 18)
                .addComponent(panelSeguridad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelRoles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_Guardar))
                    .addComponent(panelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_Guardar)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_GuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_GuardarActionPerformed
        try {
            String idClienteParaVincular = "";
            if (operacion == TipoDeOperacion.ALTA) {
                if (new String(txt_Contrasenia.getPassword()).equals(new String(txt_RepetirContrasenia.getPassword()))) {
                    Usuario usuario = new Usuario();
                    usuario.setNombre(txtNombre.getText().trim());
                    usuario.setApellido(txtApellido.getText().trim());
                    usuario.setEmail(txtEmail.getText().trim());
                    usuario.setUsername(txtUsername.getText().trim());
                    usuario.setPassword(new String(txt_Contrasenia.getPassword()));
                    usuario.setHabilitado(chkHabilitado.isSelected());
                    List<Rol> roles = new ArrayList<>();
                    if (chk_Administrador.isSelected()) {
                        roles.add(Rol.ADMINISTRADOR);
                    }
                    if (chk_Vendedor.isSelected()) {
                        roles.add(Rol.VENDEDOR);
                    }
                    if (chk_Viajante.isSelected()) {
                        roles.add(Rol.VIAJANTE);
                    }
                    if (chk_Cliente.isSelected()) {
                        roles.add(Rol.CLIENTE);
                        idClienteParaVincular = Long.toString(((Cliente)cmb_Cliente.getSelectedItem()).getId_Cliente());
                    }
                    usuario.setRoles(roles);
                    RestClient.getRestTemplate().postForObject("/usuarios?idCliente=" + idClienteParaVincular, usuario, Usuario.class);                 
                    LOGGER.warn("El usuario " + usuario.getUsername() + " se creo correctamente.");
                    this.dispose();
                } else {                    
                    JOptionPane.showMessageDialog(this, 
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_password_diferentes"),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            if (operacion == TipoDeOperacion.ACTUALIZACION) {
                if (new String(txt_Contrasenia.getPassword()).equals(new String(txt_RepetirContrasenia.getPassword()))) {                    
                    usuarioParaModificar.setNombre(txtNombre.getText().trim());
                    usuarioParaModificar.setApellido(txtApellido.getText().trim());
                    usuarioParaModificar.setEmail(txtEmail.getText().trim());
                    usuarioParaModificar.setUsername(txtUsername.getText().trim());
                    usuarioParaModificar.setHabilitado(chkHabilitado.isSelected());
                    usuarioParaModificar.setPassword(new String(txt_Contrasenia.getPassword()));                    
                    List<Rol> roles = new ArrayList<>();
                    if (chk_Administrador.isSelected()) {
                        roles.add(Rol.ADMINISTRADOR);
                    }
                    if (chk_Vendedor.isSelected()) {
                        roles.add(Rol.VENDEDOR);
                    }
                    if (chk_Viajante.isSelected()) {
                        roles.add(Rol.VIAJANTE);
                    }
                    if (chk_Cliente.isSelected()) {
                        roles.add(Rol.CLIENTE);
                        idClienteParaVincular = Long.toString(((Cliente)cmb_Cliente.getSelectedItem()).getId_Cliente());
                    }
                    usuarioParaModificar.setRoles(roles);
                    RestClient.getRestTemplate().put("/usuarios?idCliente=" + idClienteParaVincular, usuarioParaModificar);
                    LOGGER.warn("El usuario " + usuarioParaModificar.getUsername() + " se modifico correctamente.");
                    this.dispose();                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_password_diferentes"),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }                
            }
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
        this.cmb_Cliente.setEnabled(false);
        this.panelClientes.setEnabled(false);
        if (operacion == TipoDeOperacion.ACTUALIZACION) {
            this.setTitle("Modificar Usuario");
            this.cargarUsuarioParaModificar();
        } else if (operacion == TipoDeOperacion.ALTA) {
            this.setTitle("Nuevo Usuario");
        }
    }//GEN-LAST:event_formWindowOpened

    private void chk_ClienteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chk_ClienteItemStateChanged
        if (chk_Cliente.isSelected() == true) {
            cmb_Cliente.setEnabled(true);
            this.panelClientes.setEnabled(true);
            cmb_Cliente.removeAllItems();
            try {
                String criteriaBusqueda = "/clientes/busqueda/criteria?idEmpresa="
                        + String.valueOf(EmpresaActiva.getInstance().getEmpresa().getId_Empresa())
                        + "&pagina=0&tamanio=" + Integer.MAX_VALUE + "&conSaldo=false";
                PaginaRespuestaRest<Cliente> response = RestClient.getRestTemplate()
                        .exchange(criteriaBusqueda, HttpMethod.GET, null,
                                new ParameterizedTypeReference<PaginaRespuestaRest<Cliente>>() {
                        })
                        .getBody();
                response.getContent().stream().forEach((c) -> {
                    cmb_Cliente.addItem(c);
                });
                if (usuarioParaModificar != null) {
                    cmb_Cliente.setSelectedItem(RestClient.getRestTemplate().getForObject("/clientes/usuarios/" + usuarioParaModificar.getId_Usuario(), Cliente.class));
                }
            } catch (RestClientResponseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ResourceAccessException ex) {
                LOGGER.error(ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            cmb_Cliente.requestFocus();
        } else {
            cmb_Cliente.removeAllItems();
            cmb_Cliente.setEnabled(false);
            this.panelClientes.setEnabled(false);
        }
    }//GEN-LAST:event_chk_ClienteItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Guardar;
    private javax.swing.JCheckBox chkHabilitado;
    private javax.swing.JCheckBox chk_Administrador;
    private javax.swing.JCheckBox chk_Cliente;
    private javax.swing.JCheckBox chk_Vendedor;
    private javax.swing.JCheckBox chk_Viajante;
    private javax.swing.JComboBox<Cliente> cmb_Cliente;
    private javax.swing.JLabel lblApellido;
    private javax.swing.JLabel lblAvisoSeguridad;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblHabilitado;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lbl_Contrasenia;
    private javax.swing.JLabel lbl_RepetirContrasenia;
    private javax.swing.JLabel lbl_Username;
    private javax.swing.JPanel panelClientes;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelRoles;
    private javax.swing.JPanel panelSeguridad;
    private javax.swing.JTextField txtApellido;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JPasswordField txt_Contrasenia;
    private javax.swing.JPasswordField txt_RepetirContrasenia;
    // End of variables declaration//GEN-END:variables
}
