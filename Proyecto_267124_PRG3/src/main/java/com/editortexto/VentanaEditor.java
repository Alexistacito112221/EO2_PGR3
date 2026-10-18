package com.editortexto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class VentanaEditor extends JFrame {

    private final JTextPane editor = new JTextPane();
    private final ArchivoDAO archivoDAO = new ArchivoDAO();
    private final JSpinner tamano = new JSpinner(new SpinnerNumberModel(14, 1, 200, 1));
    private final JLabel estado = new JLabel("Documento nuevo");
    private final FileNameExtensionFilter filtroTxt =
            new FileNameExtensionFilter("Archivo TXT", "txt");
    private final FileNameExtensionFilter filtroRtf =
            new FileNameExtensionFilter("Archivo RTF", "rtf");
    private File archivoActual;
    private boolean modificado;
    private boolean cargando;

    public VentanaEditor() {
        setTitle("Editor de texto");
        setSize(760, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        editor.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        editor.setMargin(new Insets(10, 10, 10, 10));
        instalarDocumento(editor.getStyledDocument());
        crearInterfaz();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evento) {
                cerrarEditor();
            }
        });
    }

    private void crearInterfaz() {
        setLayout(new BorderLayout());
        setJMenuBar(crearMenu());
        add(crearBarraHerramientas(), BorderLayout.NORTH);
        add(new JScrollPane(editor), BorderLayout.CENTER);
        JPanel panelEstado = new JPanel(new BorderLayout());
        panelEstado.add(estado, BorderLayout.WEST);
        add(panelEstado, BorderLayout.SOUTH);
    }

    private JMenuBar crearMenu() {
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenu menuEditar = new JMenu("Editar");

        agregarOpcion(menuArchivo, "Nuevo", KeyEvent.VK_N, this::nuevoDocumento);
        agregarOpcion(menuArchivo, "Abrir", KeyEvent.VK_O, this::abrirDocumento);
        agregarOpcion(menuArchivo, "Guardar", KeyEvent.VK_S, this::guardarDocumento);
        menuArchivo.addSeparator();
        agregarOpcion(menuArchivo, "Guardar como", -1, this::guardarComo);
        menuArchivo.addSeparator();
        agregarOpcion(menuArchivo, "Salir", -1, this::cerrarEditor);

        agregarOpcion(menuEditar, "Negrita", KeyEvent.VK_B, this::alternarNegrita);
        agregarOpcion(menuEditar, "Italica", KeyEvent.VK_I, this::alternarItalica);
        agregarOpcion(menuEditar, "Color de letra", -1, this::cambiarColor);

        barraMenu.add(menuArchivo);
        barraMenu.add(menuEditar);
        return barraMenu;
    }

    private void agregarOpcion(JMenu menu, String texto, int tecla, Runnable accion) {
        JMenuItem opcion = new JMenuItem(texto);
        if (tecla >= 0) {
            opcion.setAccelerator(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK));
        }
        opcion.addActionListener(evento -> accion.run());
        menu.add(opcion);
    }

    private JPanel crearBarraHerramientas() {
        JPanel barra = new JPanel();
        JButton negrita = new JButton("B");
        JButton italica = new JButton("I");
        JButton color = new JButton("Color");

        negrita.setFont(negrita.getFont().deriveFont(Font.BOLD));
        italica.setFont(italica.getFont().deriveFont(Font.ITALIC));
        negrita.addActionListener(evento -> alternarNegrita());
        italica.addActionListener(evento -> alternarItalica());
        color.addActionListener(evento -> cambiarColor());
        tamano.addChangeListener(evento -> cambiarTamano());

        barra.add(negrita);
        barra.add(italica);
        barra.add(new JLabel("Tamaño:"));
        barra.add(tamano);
        barra.add(color);
        return barra;
    }

    private void alternarNegrita() {
        boolean activa = StyleConstants.isBold(editor.getCharacterAttributes());
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setBold(atributos, !activa);
        editor.setCharacterAttributes(atributos, false);
    }

    private void alternarItalica() {
        boolean activa = StyleConstants.isItalic(editor.getCharacterAttributes());
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setItalic(atributos, !activa);
        editor.setCharacterAttributes(atributos, false);
    }

    private void cambiarTamano() {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontSize(atributos, (Integer) tamano.getValue());
        editor.setCharacterAttributes(atributos, false);
    }

    private void cambiarColor() {
        Color colorActual = StyleConstants.getForeground(editor.getCharacterAttributes());
        Color nuevoColor = javax.swing.JColorChooser.showDialog(
                this, "Selecciona el color de la letra", colorActual);
        if (nuevoColor != null) {
            SimpleAttributeSet atributos = new SimpleAttributeSet();
            StyleConstants.setForeground(atributos, nuevoColor);
            editor.setCharacterAttributes(atributos, false);
        }
    }

    private void nuevoDocumento() {
        if (!confirmarCambios()) {
            return;
        }
        cargando = true;
        instalarDocumento(new javax.swing.text.DefaultStyledDocument());
        cargando = false;
        archivoActual = null;
        modificado = false;
        actualizarEstado();
    }

    private void abrirDocumento() {
        if (!confirmarCambios()) {
            return;
        }

        JFileChooser selector = crearSelector();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        try {
            StyledDocument documento = archivoDAO.abrir(archivo);
            cargando = true;
            instalarDocumento(documento);
            cargando = false;
            archivoActual = archivo;
            modificado = false;
            actualizarEstado();
        } catch (IOException | BadLocationException error) {
            cargando = false;
            mostrarError("No se pudo abrir el archivo.", error);
        }
    }

    private void guardarDocumento() {
        if (archivoActual == null) {
            guardarComo();
            return;
        }

        guardarEnArchivo(archivoActual);
    }

    private void guardarComo() {
        JFileChooser selector = crearSelector();
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        String extension = extension(archivo);
        if (!extension.equals("txt") && !extension.equals("rtf")) {
            String nuevaExtension = selector.getFileFilter() == filtroTxt ? ".txt" : ".rtf";
            archivo = new File(archivo.getPath() + nuevaExtension);
        }

        if (archivo.exists()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. Quieres reemplazarlo?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (guardarEnArchivo(archivo)) {
            archivoActual = archivo;
            actualizarEstado();
        }
    }

    private boolean guardarEnArchivo(File archivo) {
        try {
            archivoDAO.guardar(archivo, editor.getStyledDocument());
            modificado = false;
            actualizarEstado();
            return true;
        } catch (IOException | BadLocationException error) {
            mostrarError("No se pudo guardar el archivo.", error);
            return false;
        }
    }

    private JFileChooser crearSelector() {
        JFileChooser selector = new JFileChooser();
        selector.setAcceptAllFileFilterUsed(false);
        selector.addChoosableFileFilter(filtroTxt);
        selector.addChoosableFileFilter(filtroRtf);
        selector.setFileFilter(filtroRtf);
        return selector;
    }

    private String extension(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    private boolean confirmarCambios() {
        if (!modificado) {
            return true;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "Hay cambios sin guardar. Quieres guardarlos?",
                "Cambios sin guardar",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (respuesta == JOptionPane.CANCEL_OPTION
                || respuesta == JOptionPane.CLOSED_OPTION) {
            return false;
        }
        return respuesta != JOptionPane.YES_OPTION || guardarYConfirmar();
    }

    private boolean guardarYConfirmar() {
        guardarDocumento();
        return !modificado;
    }

    private void cerrarEditor() {
        if (confirmarCambios()) {
            dispose();
        }
    }

    private void instalarDocumento(StyledDocument documento) {
        editor.setDocument(documento);
        documento.addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent evento) {
                marcarModificado();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent evento) {
                marcarModificado();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent evento) {
                marcarModificado();
            }
        });
    }

    private void marcarModificado() {
        if (!cargando) {
            modificado = true;
            actualizarEstado();
        }
    }

    private void actualizarEstado() {
        String nombre = archivoActual == null ? "Documento nuevo" : archivoActual.getName();
        estado.setText(nombre + (modificado ? " *" : ""));
        setTitle(nombre + (modificado ? " *" : "") + " - Editor de texto");
    }

    private void mostrarError(String mensaje, Exception error) {
        JOptionPane.showMessageDialog(
                this,
                mensaje + "\n" + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
