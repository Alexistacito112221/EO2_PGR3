package com.editortexto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

public class ArchivoDAO {

    public StyledDocument abrir(File archivo) throws IOException, BadLocationException {
        String extension = obtenerExtension(archivo);
        StyledDocument documento = new DefaultStyledDocument();

        if (extension.equals("rtf")) {
            try (InputStream entrada = Files.newInputStream(archivo.toPath())) {
                new RTFEditorKit().read(entrada, documento, 0);
            }
            quitarSaltoFinal(documento);
        } else if (extension.equals("txt")) {
            documento.insertString(
                    0,
                    Files.readString(archivo.toPath(), StandardCharsets.UTF_8),
                    null);
        } else {
            throw new IOException("El archivo debe ser TXT o RTF.");
        }

        return documento;
    }

    public void guardar(File archivo, StyledDocument documento)
            throws IOException, BadLocationException {
        String extension = obtenerExtension(archivo);

        if (extension.equals("rtf")) {
            try (OutputStream salida = Files.newOutputStream(archivo.toPath())) {
                new RTFEditorKit().write(salida, documento, 0, documento.getLength());
            }
        } else if (extension.equals("txt")) {
            String texto = documento.getText(0, documento.getLength());
            Files.writeString(archivo.toPath(), texto, StandardCharsets.UTF_8);
        } else {
            throw new IOException("El archivo debe ser TXT o RTF.");
        }
    }

    private String obtenerExtension(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) {
            return "";
        }
        return nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    private void quitarSaltoFinal(StyledDocument documento) throws BadLocationException {
        int ultimoCaracter = documento.getLength() - 1;
        if (ultimoCaracter >= 0
                && documento.getText(ultimoCaracter, 1).equals("\n")) {
            documento.remove(ultimoCaracter, 1);
        }
    }
}
