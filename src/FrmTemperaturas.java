import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import datechooser.beans.DateChooserCombo;
import entidades.Temperatura; // Importamos la nueva entidad
import servicios.TemperaturaServicio; // Importamos el nuevo servicio

public class FrmTemperaturas extends JFrame {

    private JComboBox<String> cmbCiudad; // Cambiado de cmbMoneda
    private DateChooserCombo dccDesde, dccHasta;
    private JTabbedPane tpResultados; // Cambiado de tpCambiosMoneda
    private JPanel pnlGrafica; 
    private JPanel pnlEstadisticas;

    private List<Temperatura> temperaturas; // Cambiado de cambiosMonedas
    private List<String> ciudades; // Cambiado de monedas

    public FrmTemperaturas() {
        // --- Carga de Datos Inicial ---
        try {
            // El PDF especifica "Temperaturas.csv" 
            temperaturas = TemperaturaServicio.getDatos("datos/Temperaturas.csv");
            if (temperaturas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se pudo cargar el archivo 'Temperaturas.csv'", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Salir si no hay datos
            }
            ciudades = TemperaturaServicio.getCiudades(temperaturas);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // --- Configuración de la Ventana ---
        setTitle("Análisis de Temperaturas - Programación Funcional");
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Barra de Herramientas (Controles) ---
        JToolBar tbControles = new JToolBar();
        tbControles.add(new JLabel(" Ciudad: "));
        cmbCiudad = new JComboBox<>();
        cmbCiudad.setModel(new DefaultComboBoxModel<>(ciudades.toArray(new String[0])));
        tbControles.add(cmbCiudad);

        tbControles.add(new JLabel(" Desde: "));
        dccDesde = new DateChooserCombo();
        tbControles.add(dccDesde);

        tbControles.add(new JLabel(" Hasta: "));
        dccHasta = new DateChooserCombo();
        tbControles.add(dccHasta);

        // Botón Graficar
        JButton btnGraficar = new JButton("Graficar");
        btnGraficar.addActionListener(e -> btnGraficarClick()); // Usando Lambda
        tbControles.add(btnGraficar);

        // Botón Calcular Estadísticas
        JButton btnCalcularEstadisticas = new JButton("Calcular Estadísticas");
        btnCalcularEstadisticas.addActionListener(e -> btnCalcularEstadisticasClick()); // Usando Lambda
        tbControles.add(btnCalcularEstadisticas);

        add(tbControles, BorderLayout.NORTH);

        // --- Panel de Pestañas (Resultados) ---
        tpResultados = new JTabbedPane();
        pnlGrafica = new JPanel();
        pnlEstadisticas = new JPanel();

        tpResultados.addTab("Gráfica", pnlGrafica);
        tpResultados.addTab("Estadísticas", new JScrollPane(pnlEstadisticas));

        add(tpResultados, BorderLayout.CENTER);
    }

    private void btnGraficarClick() {
        if (cmbCiudad.getSelectedIndex() >= 0) {
            String ciudad = (String) cmbCiudad.getSelectedItem();
            LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Usamos el servicio de filtrado
            List<Temperatura> datosFiltrados = TemperaturaServicio.filtrar(temperaturas, ciudad, desde, hasta);

            // Creación de la serie para JFreeChart
            TimeSeries series = new TimeSeries("Temperatura");
            for (Temperatura item : datosFiltrados) {
                // JFreeChart usa 'Day' para las fechas
                series.add(new Day(item.getFecha().getDayOfMonth(),
                                 item.getFecha().getMonthValue(),
                                 item.getFecha().getYear()),
                           item.getTemperatura());
            }
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);

            // Creación del gráfico
            JFreeChart graficador = ChartFactory.createTimeSeriesChart(
                    "Evolución de Temperatura en " + ciudad,
                    "Fecha",
                    "Temperatura (°C)",
                    dataset,
                    true, true, false);

            ChartPanel pnlGraficador = new ChartPanel(graficador);
            pnlGraficador.setPreferredSize(new Dimension(600, 400));

            // Mostrar el gráfico en el panel
            pnlGrafica.removeAll();
            pnlGrafica.setLayout(new BorderLayout());
            pnlGrafica.add(pnlGraficador, BorderLayout.CENTER);
            pnlGrafica.revalidate();
            pnlGrafica.repaint(); // Asegurar que se redibuje

            tpResultados.setSelectedIndex(0); // Cambiar a la pestaña de gráfica
        }
    }

    private void btnCalcularEstadisticasClick() {
        if (cmbCiudad.getSelectedIndex() >= 0) {
            String ciudad = (String) cmbCiudad.getSelectedItem();
            LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Usamos el servicio de estadísticas
            var estadisticas = TemperaturaServicio.getEstadisticas(temperaturas, ciudad, desde, hasta);

            // Limpiamos el panel anterior
            pnlEstadisticas.removeAll();
            pnlEstadisticas.setLayout(new GridBagLayout());
            
            int fila = 0;
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.WEST; // Alinear a la izquierda

            // Mostramos los resultados (igual que en tu original)
            for (var estadistica : estadisticas.entrySet()) {
                gbc.gridy = fila;
                
                gbc.gridx = 0;
                pnlEstadisticas.add(new JLabel(estadistica.getKey() + ": "), gbc);
                
                gbc.gridx = 1;
                pnlEstadisticas.add(new JLabel(String.format("%.2f", estadistica.getValue())), gbc);
                
                fila++;
            }
            
            pnlEstadisticas.revalidate();
            pnlEstadisticas.repaint(); // Asegurar que se redibuje

            tpResultados.setSelectedIndex(1); // Cambiar a la pestaña de estadísticas
        }
    }
}