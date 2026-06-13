package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class LectorArchivoHospitalario {

    private static final List<Charset> ENCODINGS_CSV = List.of(
            StandardCharsets.UTF_8,
            Charset.forName("windows-1252"),
            StandardCharsets.ISO_8859_1
    );

    public ContenidoArchivoHospitalario leer(MultipartFile archivo)
            throws IOException {
        String nombre = archivo.getOriginalFilename();
        String nombreNormalizado = nombre == null
                ? ""
                : nombre.toLowerCase(Locale.ROOT);

        if (nombreNormalizado.endsWith(".csv")) {
            return leerCsv(archivo.getBytes());
        }
        if (nombreNormalizado.endsWith(".xlsx")) {
            return leerXlsx(archivo);
        }
        throw new IOException("Solo se admiten archivos .csv o .xlsx");
    }

    private ContenidoArchivoHospitalario leerCsv(byte[] bytes)
            throws IOException {
        String contenido = decodificarCsv(bytes);
        char delimitador = detectarDelimitador(contenido);
        CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimitador)
                .setQuote('"')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();

        try (CSVParser parser = formato.parse(new StringReader(contenido))) {
            List<String> columnas = parser.getHeaderNames().stream()
                    .map(NormalizadorColumnas::normalizar)
                    .toList();
            validarCabecera(columnas);

            List<FilaArchivoHospitalario> filas = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> valores = new LinkedHashMap<>();
                for (int indice = 0; indice < columnas.size(); indice++) {
                    String valor = indice < record.size()
                            ? record.get(indice)
                            : "";
                    valores.put(columnas.get(indice), limpiar(valor));
                }

                FilaArchivoHospitalario fila = new FilaArchivoHospitalario(
                        Math.toIntExact(record.getRecordNumber() + 1),
                        valores
                );
                if (!fila.estaVacia()) {
                    filas.add(fila);
                }
            }
            return new ContenidoArchivoHospitalario(columnas, filas);
        }
    }

    private ContenidoArchivoHospitalario leerXlsx(MultipartFile archivo)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IOException("El Excel no contiene hojas");
            }

            Sheet hoja = workbook.getSheetAt(0);
            Row cabecera = hoja.getRow(0);
            if (cabecera == null) {
                throw new IOException("El Excel no contiene cabecera");
            }

            DataFormatter formatter = new DataFormatter();
            List<String> columnas = new ArrayList<>();
            for (int indice = 0; indice < cabecera.getLastCellNum(); indice++) {
                columnas.add(NormalizadorColumnas.normalizar(
                        formatter.formatCellValue(cabecera.getCell(indice))
                ));
            }
            validarCabecera(columnas);

            List<FilaArchivoHospitalario> filas = new ArrayList<>();
            for (int numeroFila = 1; numeroFila <= hoja.getLastRowNum(); numeroFila++) {
                Row row = hoja.getRow(numeroFila);
                Map<String, String> valores = new LinkedHashMap<>();
                for (int indice = 0; indice < columnas.size(); indice++) {
                    Cell cell = row == null ? null : row.getCell(indice);
                    valores.put(
                            columnas.get(indice),
                            limpiar(formatter.formatCellValue(cell))
                    );
                }

                FilaArchivoHospitalario fila = new FilaArchivoHospitalario(
                        numeroFila + 1,
                        valores
                );
                if (!fila.estaVacia()) {
                    filas.add(fila);
                }
            }
            return new ContenidoArchivoHospitalario(columnas, filas);
        }
    }

    private String decodificarCsv(byte[] bytes) throws IOException {
        CharacterCodingException ultimoError = null;
        for (Charset charset : ENCODINGS_CSV) {
            try {
                CharsetDecoder decoder = charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);
                CharBuffer resultado = decoder.decode(ByteBuffer.wrap(bytes));
                return resultado.toString();
            } catch (CharacterCodingException error) {
                ultimoError = error;
            }
        }
        throw new IOException(
                "No se pudo decodificar el CSV como UTF-8, Windows-1252 o ISO-8859-1",
                ultimoError
        );
    }

    private char detectarDelimitador(String contenido) {
        String primeraLinea = contenido.lines().findFirst().orElse("");
        int puntoYComa = contarFueraDeComillas(primeraLinea, ';');
        int coma = contarFueraDeComillas(primeraLinea, ',');
        return puntoYComa >= coma ? ';' : ',';
    }

    private int contarFueraDeComillas(String linea, char delimitador) {
        boolean entreComillas = false;
        int total = 0;
        for (int indice = 0; indice < linea.length(); indice++) {
            char actual = linea.charAt(indice);
            if (actual == '"') {
                if (entreComillas
                        && indice + 1 < linea.length()
                        && linea.charAt(indice + 1) == '"') {
                    indice++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (!entreComillas && actual == delimitador) {
                total++;
            }
        }
        return total;
    }

    private void validarCabecera(List<String> columnas) throws IOException {
        if (columnas.isEmpty() || columnas.stream().allMatch(String::isBlank)) {
            throw new IOException("El archivo no contiene columnas reconocibles");
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
