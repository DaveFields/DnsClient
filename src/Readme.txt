
David Campos Rodriguez.
Sergio Simons Casasnovas.

Adicionalmente:

-->Punto 1.Implementación del mecanismo de caché.
-->Punto 3.Soporte para la resolución de consultas tipo AAAA desde la entrada estándar.

Notas:

Nosotros hemos trabajado con el programa de la siguiente forma:

>javac dnsclient.java
Dos opciones una consulta o varias por un archivo.

>echo "A www.uvigo.es" | java dnsclient -t 198.41.0.4 
>cat fichero.txt | java dnsclient -u 194.69.254.1

Ele este archivo ,fichero.txt,  tendrá que estar en el directorio scr.

Los caracteres "-------", indican el fin de una consulta.

Si no hay respuesta del dns enviaremos una respuesta "-->No hubo resultados satisfactorios para la busqueda" .

Si hay respuesta pero esta no es posible resolverla , se indicara que no resolvemos esas respuestas e indicaremos de que RRType es dicha Answer.