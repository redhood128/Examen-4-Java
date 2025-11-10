package servicios; 

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;



import entidades.Temperatura; 

public class TemperaturaServicio {

 
    public static List<Temperatura> getDatos(String nombreArchivo) {
       
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 
        
        try {
            
            InputStream is = TemperaturaServicio.class.getResourceAsStream("/" + nombreArchivo);
            
            if (is == null) {
                
                throw new Exception("Archivo no encontrado en el classpath (src): " + nombreArchivo);
            }
            
            
            Stream<String> lineas = new BufferedReader(new InputStreamReader(is)).lines();
            

            return lineas.skip(1) 
                    .map(linea -> linea.split(","))
                    .map(textos -> new Temperatura(textos[0], 
                            LocalDate.parse(textos[1], formatoFecha), 
                            Double.parseDouble(textos[2]))) 
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            ex.printStackTrace(); 
            return Collections.emptyList();
        }
    }

    
    public static List<String> getCiudades(List<Temperatura> temperaturas) {
        return temperaturas.stream()
                .map(Temperatura::getCiudad) 
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    
    public static List<Temperatura> filtrar(List<Temperatura> temperaturas,
            String ciudad, LocalDate desde, LocalDate hasta) {
        return temperaturas.stream()
                .filter(item -> item.getCiudad().equals(ciudad))
                .filter(item -> !item.getFecha().isBefore(desde))
                .filter(item -> !item.getFecha().isAfter(hasta))
                .sorted((t1, t2) -> t1.getFecha().compareTo(t2.getFecha())) 
                .collect(Collectors.toList());
    }

    

    public static double getPromedio(List<Double> datos) {
        if (datos.isEmpty()) return 0;
        return datos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double getDesviacionEstandar(List<Double> datos) {
        if (datos.size() < 2) return 0;
        double promedio = getPromedio(datos);
        double sumatoria = datos.stream()
                .mapToDouble(Double::doubleValue)
                .map(val -> Math.pow(val - promedio, 2))
                .sum();
        return Math.sqrt(sumatoria / (datos.size() - 1));
    }

    public static double getMaximo(List<Double> datos) {
        if (datos.isEmpty()) return 0;
        return datos.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public static double getMinimo(List<Double> datos) {
        if (datos.isEmpty()) return 0;
        return datos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    public static double getMediana(List<Double> datos) {
        if (datos.isEmpty()) return 0;
        var datosOrdenados = datos.stream().sorted().collect(Collectors.toList());
        int n = datosOrdenados.size();
        return n % 2 == 0 ? (datosOrdenados.get(n / 2) + datosOrdenados.get(n / 2 - 1)) / 2 : datosOrdenados.get(n / 2);
    }

    public static double getModa(List<Double> datos) {
        if (datos.isEmpty()) return 0;
        return datos.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0.0);
    }

  
    public static Map<String, Double> getEstadisticas(List<Temperatura> temperaturas,
            String ciudad, LocalDate desde, LocalDate hasta) {

        
        var datosFiltrados = filtrar(temperaturas, ciudad, desde, hasta);
        
        
        var valores = datosFiltrados.stream()
                .map(Temperatura::getTemperatura) 
                .collect(Collectors.toList());

       
        Map<String, Double> estadisticas = new LinkedHashMap<>();
        estadisticas.put("Promedio", getPromedio(valores));
        estadisticas.put("Desviación Estandar", getDesviacionEstandar(valores));
        estadisticas.put("Máximo", getMaximo(valores));
        estadisticas.put("Mínimo", getMinimo(valores));
        estadisticas.put("Mediana", getMediana(valores));
        estadisticas.put("Moda", getModa(valores));

        return estadisticas;
    }
}
