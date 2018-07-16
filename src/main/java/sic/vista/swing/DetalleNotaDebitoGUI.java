package sic.vista.swing;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import sic.RestClient;
import sic.modelo.Cliente;
import sic.modelo.EmpresaActiva;
import sic.modelo.NotaDebito;
import sic.modelo.NotaDebitoCliente;
import sic.modelo.NotaDebitoProveedor;
import sic.modelo.Proveedor;
import sic.modelo.Recibo;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.TipoDeComprobante;
import sic.modelo.UsuarioActivo;
import sic.util.FormatosFechaHora;
import sic.util.FormatterFechaHora;
import sic.util.FormatterNumero;

public class DetalleNotaDebitoGUI extends JDialog {
    private final Cliente cliente;
    private Recibo recibo;
    private final Proveedor proveedor;
    private long idRecibo;
    private boolean notaDebitoCreada;   
    private long idNotaDebitoProveedor;
    private final FormatterFechaHora formatter = new FormatterFechaHora(FormatosFechaHora.FORMATO_FECHA_HISPANO);
    private final static BigDecimal IVA_21 = new BigDecimal("21");
    private final static BigDecimal CIEN = new BigDecimal("100");
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public DetalleNotaDebitoGUI(Cliente cliente, long idRecibo) {
        this.initComponents();
        this.setIcon();
        this.notaDebitoCreada = false;
        this.cliente = cliente;
        this.proveedor = null;
        this.idRecibo = idRecibo;
    }
    
    public DetalleNotaDebitoGUI(Proveedor proveedor, long idRecibo) {
        this.initComponents();
        this.setIcon();
        this.notaDebitoCreada = false;
        this.proveedor = proveedor;
        this.cliente = null;
        this.idRecibo = idRecibo;
    }
    
    public DetalleNotaDebitoGUI(long idNotaDebitoProveedor) {
        this.initComponents();
        this.setIcon();
        this.notaDebitoCreada = false;
        this.proveedor = null;
        this.cliente = null;
        this.idNotaDebitoProveedor = idNotaDebitoProveedor;
    }
    
    public boolean isNotaDebitoCreada() {
        return notaDebitoCreada;
    }
    
    private void autorizarNotaDebito(NotaDebito notaDebito) {
        if (notaDebito != null && (notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_A
                || notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_B)) {
            try {
                RestClient.getRestTemplate().postForObject("/notas/" + notaDebito.getIdNota() + "/autorizacion",
                        null, NotaDebito.class);
                JOptionPane.showMessageDialog(this,
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_autorizada"),
                        "Aviso", JOptionPane.INFORMATION_MESSAGE);
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
    
    private void setIcon() {
        ImageIcon iconoVentana = new ImageIcon(DetalleNotaDebitoGUI.class.getResource("/sic/icons/SIC_24_square.png"));
        this.setIconImage(iconoVentana.getImage());
    }
    
    private void cargarDetalleCliente() {
        txtNombre.setText(cliente.getRazonSocial());
        txtDomicilio.setText(cliente.getDireccion()
                + " " + cliente.getNombreLocalidad()
                + " " + cliente.getNombreProvincia()
                + " " + cliente.getNombrePais());
        txtIDFiscal.setText(cliente.getIdFiscal());
        txtCondicionIVA.setText(cliente.getNombreCondicionIVA());
    }

    private void cargarDetalleProveedor() {
        txtNombre.setText(proveedor.getRazonSocial());
        txtDomicilio.setText(proveedor.getDireccion()
                + " " + proveedor.getLocalidad().getNombre()
                + " " + proveedor.getLocalidad().getProvincia().getNombre()
                + " " + proveedor.getLocalidad().getProvincia().getPais());
        txtIDFiscal.setText(proveedor.getIdFiscal());
        txtCondicionIVA.setText(proveedor.getCondicionIVA().getNombre());
    }
    
    private void cargarDetalleRecibo() {
        lblDetallePago.setText("Nº Recibo: " + recibo.getNumSerie() + " - " + recibo.getNumRecibo() + " - " + recibo.getConcepto());
        lblMontoPago.setText("$" + FormatterNumero.formatConRedondeo(recibo.getMonto()));
        lblImportePago.setText("$" + FormatterNumero.formatConRedondeo(recibo.getMonto()));
        txtNoGravado.setValue(recibo.getMonto());
        txtTotal.setValue(recibo.getMonto());
    }
    
    private void cargarDetalleComprobante() {
        txtMontoRenglon2.setValue(new BigDecimal(txtMontoRenglon2.getText()));
        BigDecimal iva = ((BigDecimal) txtMontoRenglon2.getValue()).multiply(IVA_21.divide(CIEN, 15, RoundingMode.HALF_UP));
        lblIvaNetoRenglon2.setText("$" + FormatterNumero.formatConRedondeo(iva));
        lblImporteRenglon2.setText("$" + FormatterNumero.formatConRedondeo((new BigDecimal(txtMontoRenglon2.getValue().toString()).add(iva))));
        txtSubTotalBruto.setValue(new BigDecimal(txtMontoRenglon2.getValue().toString()));
        txtIVA21Neto.setValue(iva);
        txtNoGravado.setValue(recibo.getMonto());
        txtTotal.setValue(recibo.getMonto().add(new BigDecimal(txtMontoRenglon2.getValue().toString())).add(iva)); 
    }
    
    private void guardarNotaDebitoCliente() {
        NotaDebitoCliente notaDebitoCliente = new NotaDebitoCliente();
        notaDebitoCliente.setFecha(new Date());
        notaDebitoCliente.setIva21Neto(new BigDecimal(txtIVA21Neto.getValue().toString()));
        notaDebitoCliente.setIva105Neto(BigDecimal.ZERO);
        notaDebitoCliente.setMontoNoGravado(recibo.getMonto());
        notaDebitoCliente.setMotivo(cmbDescripcionRenglon2.getSelectedItem().toString());
        NotaDebito nd;
        try {
            notaDebitoCliente.setRenglonesNotaDebito(Arrays.asList(RestClient.getRestTemplate()
                    .getForObject("/notas/renglon/debito/recibo/" + recibo.getIdRecibo()
                            + "?monto=" + new BigDecimal(txtSubTotalBruto.getValue().toString())
                            + "&ivaPorcentaje=21", RenglonNotaDebito[].class)));
            notaDebitoCliente.setSubTotalBruto(new BigDecimal(txtSubTotalBruto.getValue().toString()));
            notaDebitoCliente.setTotal(RestClient.getRestTemplate().getForObject("/notas/debito/total"
                    + "?subTotalBruto=" + new BigDecimal(txtSubTotalBruto.getValue().toString())
                    + "&iva21Neto=" + notaDebitoCliente.getIva21Neto()
                    + "&montoNoGravado=" + notaDebitoCliente.getMontoNoGravado(), BigDecimal.class));
            notaDebitoCliente.setUsuario(UsuarioActivo.getInstance().getUsuario());
            nd = RestClient.getRestTemplate()
                    .postForObject("/notas/debito/empresa/" + EmpresaActiva.getInstance().getEmpresa().getId_Empresa()
                            + "/cliente/" + cliente.getId_Cliente()
                            + "/usuario/" + UsuarioActivo.getInstance().getUsuario().getId_Usuario()
                            + "/recibo/" + recibo.getIdRecibo(), notaDebitoCliente, NotaDebito.class);
            if (nd != null) {
                notaDebitoCreada = true;
                int reply = JOptionPane.showConfirmDialog(this,
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_reporte"),
                        "Aviso", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        byte[] reporte = RestClient.getRestTemplate()
                                .getForObject("/notas/" + nd.getIdNota() + "/reporte", byte[].class);
                        File f = new File(System.getProperty("user.home") + "/NotaDebito.pdf");
                        Files.write(f.toPath(), reporte);
                        Desktop.getDesktop().open(f);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                ResourceBundle.getBundle("Mensajes").getString("mensaje_error_plataforma_no_soportada"),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                this.dispose();
            }
            boolean FEHabilitada = RestClient.getRestTemplate().getForObject("/configuraciones-del-sistema/empresas/"
                    + EmpresaActiva.getInstance().getEmpresa().getId_Empresa()
                    + "/factura-electronica-habilitada", Boolean.class);
            if (FEHabilitada) {
                this.autorizarNotaDebito(nd);
            }
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_IOException"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarNotaDebitoProveedor() {
        NotaDebitoProveedor notaDebitoProveedor = new NotaDebitoProveedor();
        notaDebitoProveedor.setFecha(new Date());
        notaDebitoProveedor.setIva21Neto(new BigDecimal(txtIVA21Neto.getValue().toString()));
        notaDebitoProveedor.setIva105Neto(BigDecimal.ZERO);
        notaDebitoProveedor.setMontoNoGravado(recibo.getMonto());
        notaDebitoProveedor.setMotivo(cmbDescripcionRenglon2.getSelectedItem().toString());
        notaDebitoProveedor.setSerie(Long.parseLong(txt_Serie.getText()));
        notaDebitoProveedor.setNroNota(Long.parseLong(txt_Numero.getText()));
        try {
            notaDebitoProveedor.setRenglonesNotaDebito(Arrays.asList(RestClient.getRestTemplate()
                    .getForObject("/notas/renglon/debito/recibo/" + recibo.getIdRecibo()
                    + "?monto=" + new BigDecimal(txtSubTotalBruto.getValue().toString())
                    + "&ivaPorcentaje=21", RenglonNotaDebito[].class)));
            notaDebitoProveedor.setSubTotalBruto(new BigDecimal(txtSubTotalBruto.getValue().toString()));
            notaDebitoProveedor.setTotal(RestClient.getRestTemplate().getForObject("/notas/debito/total"
                    + "?subTotalBruto=" + new BigDecimal(txtSubTotalBruto.getValue().toString())
                    + "&iva21Neto=" + notaDebitoProveedor.getIva21Neto()
                    + "&montoNoGravado=" + notaDebitoProveedor.getMontoNoGravado(), BigDecimal.class));
            notaDebitoProveedor.setUsuario(UsuarioActivo.getInstance().getUsuario());
            NotaDebito nd = RestClient.getRestTemplate()
                    .postForObject("/notas/debito/empresa/" + EmpresaActiva.getInstance().getEmpresa().getId_Empresa()
                    + "/proveedor/" + proveedor.getId_Proveedor()
                    + "/usuario/" + UsuarioActivo.getInstance().getUsuario().getId_Usuario()
                    + "/recibo/" + recibo.getIdRecibo(), notaDebitoProveedor, NotaDebito.class);
            notaDebitoCreada = (nd != null);
            this.dispose();
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDetalleNotaDebitoProveedor() {
        try {
            NotaDebitoProveedor notaDebitoProveedor = RestClient.getRestTemplate().getForObject("/notas/" + idNotaDebitoProveedor, NotaDebitoProveedor.class);
            this.setTitle(notaDebitoProveedor.getTipoComprobante() + " Nº " + notaDebitoProveedor.getSerie() + " - " + notaDebitoProveedor.getNroNota()
                    + " con fecha " + formatter.format(notaDebitoProveedor.getFecha()) + " del Proveedor: " + notaDebitoProveedor.getProveedor().getRazonSocial());
            txt_Serie.setEnabled(false);
            txt_Numero.setEnabled(false);
            txt_Serie.setText(String.valueOf(notaDebitoProveedor.getSerie()));
            txt_Numero.setText(String.valueOf(notaDebitoProveedor.getNroNota()));
            txtNombre.setText(notaDebitoProveedor.getProveedor().getRazonSocial());
            txtDomicilio.setText(notaDebitoProveedor.getProveedor().getDireccion()
                    + " " + notaDebitoProveedor.getProveedor().getLocalidad().getNombre()
                    + " " + notaDebitoProveedor.getProveedor().getLocalidad().getProvincia().getNombre()
                    + " " + notaDebitoProveedor.getProveedor().getLocalidad().getProvincia().getPais());
            txtCondicionIVA.setText(notaDebitoProveedor.getProveedor().getCondicionIVA().getNombre());
            txtIDFiscal.setText(notaDebitoProveedor.getProveedor().getIdFiscal());
            lblDetallePago.setText("Nº Recibo: " + notaDebitoProveedor.getRecibo().getNumSerie() + " - " + notaDebitoProveedor.getRecibo().getNumRecibo() + " - " + notaDebitoProveedor.getRecibo().getConcepto());
            lblMontoPago.setText("$" + FormatterNumero.formatConRedondeo(notaDebitoProveedor.getRecibo().getMonto()));
            lblImportePago.setText("$" + FormatterNumero.formatConRedondeo(notaDebitoProveedor.getRecibo().getMonto()));
            List<RenglonNotaDebito> renglonesNotaDebito = Arrays.asList(RestClient.getRestTemplate().getForObject("/notas/renglones/debito/proveedores/" + idNotaDebitoProveedor, RenglonNotaDebito[].class));
            txtMontoRenglon2.setText(FormatterNumero.formatConRedondeo(renglonesNotaDebito.get(1).getImporteBruto()));
            lblIvaNetoRenglon2.setText(FormatterNumero.formatConRedondeo(renglonesNotaDebito.get(1).getIvaNeto()));
            lblImporteRenglon2.setText(FormatterNumero.formatConRedondeo(renglonesNotaDebito.get(1).getImporteNeto()));
            cmbDescripcionRenglon2.addItem(notaDebitoProveedor.getMotivo());
            txtSubTotalBruto.setValue(notaDebitoProveedor.getSubTotalBruto());
            txtIVA21Neto.setValue(notaDebitoProveedor.getIva21Neto());
            txtNoGravado.setValue(notaDebitoProveedor.getMontoNoGravado());
            txtTotal.setValue(notaDebitoProveedor.getTotal());
            txtMontoRenglon2.setEnabled(false);
            cmbDescripcionRenglon2.setEnabled(false);
            btnGuardar.setEnabled(false);
        } catch (RestClientResponseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ResourceAccessException ex) {
            LOGGER.error(ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCliente = new javax.swing.JPanel();
        lblNombreCliente = new javax.swing.JLabel();
        lblDomicilioCliente = new javax.swing.JLabel();
        lblIDFiscalCliente = new javax.swing.JLabel();
        lblCondicionIVACliente = new javax.swing.JLabel();
        txtCondicionIVA = new javax.swing.JTextField();
        txtIDFiscal = new javax.swing.JTextField();
        txtDomicilio = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        panelDetalle = new javax.swing.JPanel();
        lblDescripcion = new javax.swing.JLabel();
        lblIvaPorcentaje = new javax.swing.JLabel();
        lblImporteEncabezado = new javax.swing.JLabel();
        lblIvaEncabezado = new javax.swing.JLabel();
        lblIVAnetoDetallePago = new javax.swing.JLabel();
        lblImportePago = new javax.swing.JLabel();
        lbl_Monto = new javax.swing.JLabel();
        txtMontoRenglon2 = new javax.swing.JFormattedTextField();
        lblIvaNetoRenglon2 = new javax.swing.JLabel();
        lblImporteRenglon2 = new javax.swing.JLabel();
        lblGastoAdministrativo = new javax.swing.JLabel();
        lblIVA21 = new javax.swing.JLabel();
        lblDetallePago = new javax.swing.JLabel();
        lblMontoPago = new javax.swing.JLabel();
        lblIVADetallePago = new javax.swing.JLabel();
        panelResultados = new javax.swing.JPanel();
        lbl_IVA105 = new javax.swing.JLabel();
        txtIVA21Neto = new javax.swing.JFormattedTextField();
        lblTotal = new javax.swing.JLabel();
        txtTotal = new javax.swing.JFormattedTextField();
        txtSubTotalBruto = new javax.swing.JFormattedTextField();
        lblSubTotalBruto = new javax.swing.JLabel();
        lblIva21 = new javax.swing.JLabel();
        lblNoGravado = new javax.swing.JLabel();
        txtNoGravado = new javax.swing.JFormattedTextField();
        btnGuardar = new javax.swing.JButton();
        panelMotivo = new javax.swing.JPanel();
        lblMotivo = new javax.swing.JLabel();
        cmbDescripcionRenglon2 = new javax.swing.JComboBox<>();
        txt_Serie = new javax.swing.JFormattedTextField();
        lbl_separador = new javax.swing.JLabel();
        txt_Numero = new javax.swing.JFormattedTextField();
        lbl_NumComprobante = new javax.swing.JLabel();

        setLocationByPlatform(true);
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        lblNombreCliente.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNombreCliente.setText("Nombre:");

        lblDomicilioCliente.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDomicilioCliente.setText("Domicilio:");

        lblIDFiscalCliente.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblIDFiscalCliente.setText("ID Fiscal:");

        lblCondicionIVACliente.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCondicionIVACliente.setText("Condición IVA:");

        txtCondicionIVA.setEditable(false);
        txtCondicionIVA.setFocusable(false);

        txtIDFiscal.setEditable(false);
        txtIDFiscal.setFocusable(false);

        txtDomicilio.setEditable(false);
        txtDomicilio.setFocusable(false);

        txtNombre.setEditable(false);
        txtNombre.setFocusable(false);

        javax.swing.GroupLayout panelClienteLayout = new javax.swing.GroupLayout(panelCliente);
        panelCliente.setLayout(panelClienteLayout);
        panelClienteLayout.setHorizontalGroup(
            panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombreCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDomicilioCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCondicionIVACliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDomicilio, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtNombre)
                    .addGroup(panelClienteLayout.createSequentialGroup()
                        .addComponent(txtCondicionIVA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblIDFiscalCliente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIDFiscal)))
                .addContainerGap())
        );
        panelClienteLayout.setVerticalGroup(
            panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelClienteLayout.createSequentialGroup()
                .addGroup(panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombreCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDomicilioCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCondicionIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCondicionIVACliente)
                    .addComponent(txtIDFiscal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIDFiscalCliente)))
        );

        panelDetalle.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDescripcion.setText("Descripcion");

        lblIvaPorcentaje.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIvaPorcentaje.setText("% IVA");

        lblImporteEncabezado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImporteEncabezado.setText("Importe");

        lblIvaEncabezado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIvaEncabezado.setText("IVA");

        lblIVAnetoDetallePago.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIVAnetoDetallePago.setText("-");

        lblImportePago.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImportePago.setText("$0");

        lbl_Monto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_Monto.setText("Monto");

        txtMontoRenglon2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.##"))));
        txtMontoRenglon2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtMontoRenglon2.setText("0");
        txtMontoRenglon2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtMontoRenglon2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtMontoRenglon2FocusLost(evt);
            }
        });
        txtMontoRenglon2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMontoRenglon2ActionPerformed(evt);
            }
        });
        txtMontoRenglon2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtMontoRenglon2KeyTyped(evt);
            }
        });

        lblIvaNetoRenglon2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIvaNetoRenglon2.setText("$0");

        lblImporteRenglon2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImporteRenglon2.setText("$0");

        lblGastoAdministrativo.setText("Gasto Administrativo");

        lblIVA21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIVA21.setText("21%");

        lblDetallePago.setText("nroPago + nota");

        lblMontoPago.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMontoPago.setText("$0");

        lblIVADetallePago.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIVADetallePago.setText("-");

        javax.swing.GroupLayout panelDetalleLayout = new javax.swing.GroupLayout(panelDetalle);
        panelDetalle.setLayout(panelDetalleLayout);
        panelDetalleLayout.setHorizontalGroup(
            panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetalleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescripcion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblGastoAdministrativo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDetallePago, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtMontoRenglon2)
                    .addComponent(lbl_Monto, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(lblMontoPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblIvaPorcentaje, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                    .addComponent(lblIVA21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblIVADetallePago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblIvaEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(lblIVAnetoDetallePago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblIvaNetoRenglon2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblImporteRenglon2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblImporteEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                    .addComponent(lblImportePago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelDetalleLayout.setVerticalGroup(
            panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetalleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblDescripcion)
                    .addComponent(lbl_Monto)
                    .addComponent(lblIvaPorcentaje)
                    .addComponent(lblIvaEncabezado)
                    .addComponent(lblImporteEncabezado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblDetallePago)
                    .addComponent(lblMontoPago)
                    .addComponent(lblIVADetallePago)
                    .addComponent(lblIVAnetoDetallePago)
                    .addComponent(lblImportePago))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblGastoAdministrativo)
                    .addComponent(txtMontoRenglon2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIVA21)
                    .addComponent(lblIvaNetoRenglon2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblImporteRenglon2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDetalleLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblIVAnetoDetallePago, lblImportePago});

        panelResultados.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lbl_IVA105.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl_IVA105.setText("I.V.A.");

        txtIVA21Neto.setEditable(false);
        txtIVA21Neto.setForeground(new java.awt.Color(29, 156, 37));
        txtIVA21Neto.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getCurrencyInstance())));
        txtIVA21Neto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtIVA21Neto.setText("0");
        txtIVA21Neto.setFocusable(false);
        txtIVA21Neto.setFont(new java.awt.Font("DejaVu Sans", 0, 15)); // NOI18N
        txtIVA21Neto.setValue(0);

        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotal.setText("TOTAL");

        txtTotal.setEditable(false);
        txtTotal.setForeground(new java.awt.Color(29, 156, 37));
        txtTotal.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getCurrencyInstance())));
        txtTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotal.setText("0");
        txtTotal.setFocusable(false);
        txtTotal.setFont(new java.awt.Font("DejaVu Sans", 1, 30)); // NOI18N
        txtTotal.setValue(0);

        txtSubTotalBruto.setEditable(false);
        txtSubTotalBruto.setForeground(new java.awt.Color(29, 156, 37));
        txtSubTotalBruto.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getCurrencyInstance())));
        txtSubTotalBruto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSubTotalBruto.setText("0");
        txtSubTotalBruto.setFocusable(false);
        txtSubTotalBruto.setFont(new java.awt.Font("DejaVu Sans", 0, 15)); // NOI18N
        txtSubTotalBruto.setValue(0);

        lblSubTotalBruto.setText("SubTotal Bruto");

        lblIva21.setText("21 %");

        lblNoGravado.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoGravado.setText("No Gravado");

        txtNoGravado.setEditable(false);
        txtNoGravado.setForeground(new java.awt.Color(29, 156, 37));
        txtNoGravado.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getCurrencyInstance())));
        txtNoGravado.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNoGravado.setText("0");
        txtNoGravado.setFocusable(false);
        txtNoGravado.setFont(new java.awt.Font("DejaVu Sans", 0, 15)); // NOI18N
        txtNoGravado.setValue(0);

        javax.swing.GroupLayout panelResultadosLayout = new javax.swing.GroupLayout(panelResultados);
        panelResultados.setLayout(panelResultadosLayout);
        panelResultadosLayout.setHorizontalGroup(
            panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSubTotalBruto)
                    .addComponent(lblSubTotalBruto, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtIVA21Neto)
                    .addComponent(lblIva21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_IVA105, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtNoGravado)
                    .addComponent(lblNoGravado, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtTotal)
                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelResultadosLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblIva21, lblNoGravado, lblSubTotalBruto, lbl_IVA105, txtIVA21Neto, txtNoGravado, txtSubTotalBruto});

        panelResultadosLayout.setVerticalGroup(
            panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblSubTotalBruto)
                    .addComponent(lbl_IVA105)
                    .addComponent(lblNoGravado)
                    .addComponent(lblTotal))
                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelResultadosLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelResultadosLayout.createSequentialGroup()
                                .addComponent(lblIva21, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtIVA21Neto)
                                    .addComponent(txtNoGravado)))
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelResultadosLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(txtSubTotalBruto, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        btnGuardar.setForeground(java.awt.Color.blue);
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/Accept_16x16.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        panelMotivo.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblMotivo.setText("Motivo:");

        cmbDescripcionRenglon2.setEditable(true);
        cmbDescripcionRenglon2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cheque Rechazado - Sin Fondos", "Cheque Rechazado - Cuenta Embargada", "Cheque Rechazado", "Irregularidad Cadena de Endosos" }));

        javax.swing.GroupLayout panelMotivoLayout = new javax.swing.GroupLayout(panelMotivo);
        panelMotivo.setLayout(panelMotivoLayout);
        panelMotivoLayout.setHorizontalGroup(
            panelMotivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMotivoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMotivo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDescripcionRenglon2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMotivoLayout.setVerticalGroup(
            panelMotivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMotivoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMotivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblMotivo)
                    .addComponent(cmbDescripcionRenglon2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txt_Serie.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txt_Serie.setText("0");

        lbl_separador.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_separador.setText("-");

        txt_Numero.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txt_Numero.setText("0");

        lbl_NumComprobante.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_NumComprobante.setText("Nº de Nota:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelMotivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelResultados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGuardar)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lbl_NumComprobante)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_Serie, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_separador, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_Numero, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txt_Numero, txt_Serie});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lbl_NumComprobante)
                    .addComponent(txt_Serie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_separador)
                    .addComponent(txt_Numero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelResultados, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {txt_Numero, txt_Serie});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (cliente != null) {
            this.guardarNotaDebitoCliente();
        } else if (proveedor != null) {
            this.guardarNotaDebitoProveedor();
        }        
    }//GEN-LAST:event_btnGuardarActionPerformed
      
    private void txtMontoRenglon2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtMontoRenglon2FocusGained
        SwingUtilities.invokeLater(() -> {
            txtMontoRenglon2.selectAll();
        });
    }//GEN-LAST:event_txtMontoRenglon2FocusGained

    private void txtMontoRenglon2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtMontoRenglon2FocusLost
        this.cargarDetalleComprobante();
    }//GEN-LAST:event_txtMontoRenglon2FocusLost

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if (cliente != null || proveedor != null) {
            this.setTitle("Nueva Nota de Debito");
            try {
                recibo = RestClient.getRestTemplate().getForObject("/recibos/" + idRecibo, Recibo.class);
            } catch (RestClientResponseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ResourceAccessException ex) {
                LOGGER.error(ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_error_conexion"),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            if (cliente != null) {
                lbl_NumComprobante.setVisible(false);
                txt_Serie.setVisible(false);
                lbl_separador.setVisible(false);
                txt_Numero.setVisible(false);
                this.cargarDetalleCliente();
            } else if (proveedor != null) {                
                this.cargarDetalleProveedor();
            }
            this.cargarDetalleRecibo();
        } else {
            btnGuardar.setVisible(false);
            this.cargarDetalleNotaDebitoProveedor();
        }
    }//GEN-LAST:event_formWindowOpened
    
    private void txtMontoRenglon2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMontoRenglon2KeyTyped
        if (evt.getKeyChar() == KeyEvent.VK_MINUS) {
            evt.consume();
        }
    }//GEN-LAST:event_txtMontoRenglon2KeyTyped

    private void txtMontoRenglon2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMontoRenglon2ActionPerformed
        this.cargarDetalleComprobante();
    }//GEN-LAST:event_txtMontoRenglon2ActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JComboBox<String> cmbDescripcionRenglon2;
    private javax.swing.JLabel lblCondicionIVACliente;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblDetallePago;
    private javax.swing.JLabel lblDomicilioCliente;
    private javax.swing.JLabel lblGastoAdministrativo;
    private javax.swing.JLabel lblIDFiscalCliente;
    private javax.swing.JLabel lblIVA21;
    private javax.swing.JLabel lblIVADetallePago;
    private javax.swing.JLabel lblIVAnetoDetallePago;
    private javax.swing.JLabel lblImporteEncabezado;
    private javax.swing.JLabel lblImportePago;
    private javax.swing.JLabel lblImporteRenglon2;
    private javax.swing.JLabel lblIva21;
    private javax.swing.JLabel lblIvaEncabezado;
    private javax.swing.JLabel lblIvaNetoRenglon2;
    private javax.swing.JLabel lblIvaPorcentaje;
    private javax.swing.JLabel lblMontoPago;
    private javax.swing.JLabel lblMotivo;
    private javax.swing.JLabel lblNoGravado;
    private javax.swing.JLabel lblNombreCliente;
    private javax.swing.JLabel lblSubTotalBruto;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lbl_IVA105;
    private javax.swing.JLabel lbl_Monto;
    private javax.swing.JLabel lbl_NumComprobante;
    private javax.swing.JLabel lbl_separador;
    private javax.swing.JPanel panelCliente;
    private javax.swing.JPanel panelDetalle;
    private javax.swing.JPanel panelMotivo;
    private javax.swing.JPanel panelResultados;
    private javax.swing.JTextField txtCondicionIVA;
    private javax.swing.JTextField txtDomicilio;
    private javax.swing.JTextField txtIDFiscal;
    private javax.swing.JFormattedTextField txtIVA21Neto;
    private javax.swing.JFormattedTextField txtMontoRenglon2;
    private javax.swing.JFormattedTextField txtNoGravado;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JFormattedTextField txtSubTotalBruto;
    private javax.swing.JFormattedTextField txtTotal;
    private javax.swing.JFormattedTextField txt_Numero;
    private javax.swing.JFormattedTextField txt_Serie;
    // End of variables declaration//GEN-END:variables
}
