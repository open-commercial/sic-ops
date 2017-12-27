package sic.vista.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import sic.RestClient;
import sic.modelo.FacturaVenta;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoMovimiento;
import sic.util.FormatterFechaHora;
import sic.util.FormatterNumero;
import sic.util.RenderTabla;

public class SeleccionDeProductosGUI extends JDialog {

    private final ModeloTabla modeloTablaResultados = new ModeloTabla();
    private FacturaVenta fv;
    private final HashMap<Long, Double> idsRenglonesYCantidades = new HashMap<>();
    private boolean modificarStock;
    private TipoMovimiento tipoMovimiento;    
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public SeleccionDeProductosGUI(long idFactura) {        
        this.initComponents();
        this.setIcon();
        this.setColumnas();
        this.recuperarFactura(idFactura);
    }
    
    public SeleccionDeProductosGUI(long idFactura, TipoMovimiento tipoMovimiento) {        
        this.initComponents();
        this.setIcon();
        this.setColumnas();
        this.tipoMovimiento = tipoMovimiento;
        this.recuperarFactura(idFactura);
    }
    
    public HashMap<Long, Double> getRenglonesConCantidadNueva() {
        return idsRenglonesYCantidades;
    }
    
    public long getIdFactura() {
        return fv.getId_Factura();
    }
    
    public boolean modificarStock() {
        return modificarStock;
    }
    
    private void setIcon() {
        ImageIcon iconoVentana = new ImageIcon(PuntoDeVentaGUI.class.getResource("/sic/icons/SIC_24_square.png"));
        this.setIconImage(iconoVentana.getImage());
    }
    
    private class ColoresProductosTablaRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla,
                Object valor, boolean isSelected, boolean hasFocus,
                int row, int column) {

            JLabel cell = (JLabel) super.getTableCellRendererComponent(tabla, valor, isSelected, hasFocus, row, 6);
            this.setHorizontalAlignment(SwingConstants.RIGHT);
            double cantidadOriginal = (double)tabla.getValueAt(row, 5);
            if (valor instanceof Double) {
                Double numero = (Double) valor;
                cell.setText(FormatterNumero.formatConRedondeo(numero));
                if (numero >= 0 || numero <= cantidadOriginal) {
                    cell.setBackground(Color.GREEN);
                    cell.setFont(new Font("Font", Font.BOLD, 12));
                }
                if (numero < 0 || numero > cantidadOriginal) {
                    cell.setBackground(Color.PINK);
                    cell.setFont(new Font("Font", Font.BOLD, 12));
                }
            } else {
                cell.setBackground(Color.WHITE);
                cell.setFont(new Font("Font", Font.BOLD, 12));
            }
            return cell;
        }
    }

    private void setColumnas() {
        //nombres de columnas
        String[] encabezados = new String[7];
        encabezados[0] = "Codigo";
        encabezados[1] = "Descripcion";
        encabezados[2] = "Medida";
        encabezados[3] = "P. Unitario";
        encabezados[4] = "IVA %";
        encabezados[5] = "Cantidad";
        encabezados[6] = "";
        modeloTablaResultados.setColumnIdentifiers(encabezados);
        tblResultados.setModel(modeloTablaResultados);

        //tipo de dato columnas
        Class[] tipos = new Class[modeloTablaResultados.getColumnCount()];
        tipos[0] = String.class;
        tipos[1] = String.class;
        tipos[2] = String.class;
        tipos[3] = Double.class;
        tipos[4] = Double.class;
        tipos[5] = Double.class;
        tipos[6] = Double.class;
        modeloTablaResultados.setClaseColumnas(tipos);
        tblResultados.getTableHeader().setReorderingAllowed(false);
        tblResultados.getTableHeader().setResizingAllowed(true);

        //editables                
        Boolean[] editables = {false, false, false, false, false, false, true};
        modeloTablaResultados.setEditables(editables);

        //render para los tipos de datos
        tblResultados.setDefaultRenderer(Double.class, new RenderTabla());
        tblResultados.getColumnModel().getColumn(6).setCellRenderer(new ColoresProductosTablaRenderer());
        //Finaliza la edicion de la tabla al perder el foco
        tblResultados.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        //size de columnas        
        tblResultados.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblResultados.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblResultados.getColumnModel().getColumn(2).setPreferredWidth(20);
        tblResultados.getColumnModel().getColumn(3).setPreferredWidth(30);
        tblResultados.getColumnModel().getColumn(4).setPreferredWidth(20);
        tblResultados.getColumnModel().getColumn(5).setPreferredWidth(20);
        tblResultados.getColumnModel().getColumn(6).setPreferredWidth(20);
    }
    
    private void recalcularRenglonesFactura() {
        for (int i = 0; i < tblResultados.getRowCount(); i++) {
            if (!(((Double) tblResultados.getValueAt(i, 6)) < 0)) {
                if (((Double) tblResultados.getValueAt(i, 6)) > 0 && ((Double) tblResultados.getValueAt(i, 6)) <= ((Double) tblResultados.getValueAt(i, 5))) {
                    idsRenglonesYCantidades.put(fv.getRenglones().get(i).getId_RenglonFactura(), ((Double) tblResultados.getValueAt(i, 6)));
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        MessageFormat.format(ResourceBundle.getBundle("Mensajes").getString(
                                "mensaje_nota_credito_renglon_no_valido"), fv.getRenglones().get(i).getDescripcionItem()),
                        "Error", JOptionPane.ERROR_MESSAGE);
                i = tblResultados.getRowCount();
                idsRenglonesYCantidades.clear();
            }
        }
    }
    
    private void cargarRenglonesAlTable() {
        fv.getRenglones().stream().map(r -> {
            Object[] fila = new Object[7];
            fila[0] = r.getCodigoItem();
            fila[1] = r.getDescripcionItem();
            fila[2] = r.getMedidaItem();
            fila[3] = r.getPrecioUnitario();
            fila[4] = r.getIva_porcentaje();
            fila[5] = r.getCantidad();
            fila[6] = 0.0;
            return fila;
        }).forEach(fila -> {
            modeloTablaResultados.addRow(fila);
        });
        tblResultados.setModel(modeloTablaResultados);
    }

    private void recuperarFactura(long idFactura) {
        try {
            fv = RestClient.getRestTemplate().getForObject("/facturas/" + idFactura, FacturaVenta.class);
            if (tipoMovimiento == null) {
                fv.setRenglones(new ArrayList(Arrays.asList(RestClient.getRestTemplate()
                        .getForObject("/facturas/" + fv.getId_Factura() + "/renglones", RenglonFactura[].class))));
            } else if (tipoMovimiento == TipoMovimiento.CREDITO) {
                fv.setRenglones(new ArrayList(Arrays.asList(RestClient.getRestTemplate()
                        .getForObject("/facturas/" + fv.getId_Factura() + "/renglones/notas/credito", RenglonFactura[].class))));
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblInstrucciones = new javax.swing.JLabel();
        btnContinuar = new javax.swing.JButton();
        spResultados = new javax.swing.JScrollPane();
        tblResultados = new javax.swing.JTable();
        chkModificarStock = new javax.swing.JCheckBox();
        chkSeleccionarTodo = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        lblInstrucciones.setText("NOTA: Indique en la ultima columna la cantidad que desea seleccionar de ese producto.");

        btnContinuar.setForeground(java.awt.Color.blue);
        btnContinuar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sic/icons/Accept_16x16.png"))); // NOI18N
        btnContinuar.setText("Continuar");
        btnContinuar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContinuarActionPerformed(evt);
            }
        });

        tblResultados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblResultados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblResultados.getTableHeader().setReorderingAllowed(false);
        tblResultados.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tblResultadosPropertyChange(evt);
            }
        });
        spResultados.setViewportView(tblResultados);

        chkModificarStock.setSelected(true);
        chkModificarStock.setText("Modificar Stock");

        chkSeleccionarTodo.setText("Seleccionar Todo");
        chkSeleccionarTodo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkSeleccionarTodoItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(chkModificarStock)
                        .addGap(0, 0, 0)
                        .addComponent(chkSeleccionarTodo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnContinuar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblInstrucciones)
                        .addGap(0, 366, Short.MAX_VALUE))
                    .addComponent(spResultados))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInstrucciones)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spResultados, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnContinuar)
                    .addComponent(chkModificarStock)
                    .addComponent(chkSeleccionarTodo))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.cargarRenglonesAlTable();
        this.setTitle(fv.getTipoComprobante() + " Nro: " + fv.getNumSerie() + " - " + fv.getNumFactura() 
                + " del Cliente: " + fv.getRazonSocialCliente() 
                + " con Fecha: " + (new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_HISPANO)).format(fv.getFecha()));
    }//GEN-LAST:event_formWindowOpened

    private void btnContinuarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinuarActionPerformed
        modificarStock = chkModificarStock.isSelected();
        this.recalcularRenglonesFactura();
        if (!this.getRenglonesConCantidadNueva().isEmpty()) {
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_productos_no_seleccionados"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnContinuarActionPerformed

    private void chkSeleccionarTodoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkSeleccionarTodoItemStateChanged
        int i = 0;
        if (chkSeleccionarTodo.isSelected()) {
            for (RenglonFactura r : fv.getRenglones()) {
                tblResultados.setValueAt(r.getCantidad(), i, 6);
                i++;
            }
        }
    }//GEN-LAST:event_chkSeleccionarTodoItemStateChanged

    private void tblResultadosPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tblResultadosPropertyChange
        if ("tableCellEditor".equals(evt.getPropertyName())) {
            chkSeleccionarTodo.setSelected(false);
        }
    }//GEN-LAST:event_tblResultadosPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContinuar;
    private javax.swing.JCheckBox chkModificarStock;
    private javax.swing.JCheckBox chkSeleccionarTodo;
    private javax.swing.JLabel lblInstrucciones;
    private javax.swing.JScrollPane spResultados;
    private javax.swing.JTable tblResultados;
    // End of variables declaration//GEN-END:variables
}
