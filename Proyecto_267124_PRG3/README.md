# Editor de texto

Editor sencillo hecho con Java Swing. Permite crear documentos, abrir archivos y guardar en formato TXT o RTF.

## Funciones

- Crear un documento nuevo, abrirlo, guardarlo y guardarlo con otro nombre.
- Aplicar negrita, italica, tamano y color a las letras.
- Guardar texto plano en TXT.
- Guardar formato de texto en RTF. Los archivos TXT no conservan negrita, italica, tamano ni color.
- Avisar antes de cerrar o abrir otro documento si hay cambios sin guardar.

## Clases

- `EditorTexto`: inicia el programa.
- `VentanaEditor`: muestra la ventana y controla los botones y menus.
- `ArchivoDAO`: lee y guarda archivos TXT y RTF.

## Abrir en NetBeans

1. Abre NetBeans y selecciona **File > Open Project**.
2. Selecciona la carpeta que contiene este `pom.xml`.
3. Ejecuta el proyecto. La clase principal es `com.editortexto.EditorTexto`.

Se necesita Java 11 o posterior.
