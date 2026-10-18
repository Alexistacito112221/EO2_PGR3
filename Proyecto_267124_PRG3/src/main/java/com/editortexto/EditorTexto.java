package com.editortexto;

import javax.swing.SwingUtilities;

public class EditorTexto {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaEditor().setVisible(true));
    }
}
