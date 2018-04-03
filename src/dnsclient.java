

import es.uvigo.det.ro.simpledns.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.io.PrintWriter;
import java.util.Date;



import java.util.Iterator;

public class dnsclient
{
	static boolean ok=false;
	static boolean consultado=false;
	static boolean fallo=false;
	static // Variables:
	String busqueda;
	static String registro;
	static String url;
	static String aux;
	static String protocolo;
	static String ip_dns_raiz;
	static InetAddress ip;
	static Map<String, String[]> Cache = new TreeMap<String, String[]>();
	static ArrayList<String> Busquedas = new ArrayList<String>();
	static FileWriter fichero = null;
	static PrintWriter pw = null;


	public static void main(String[] args) throws Exception
	{
		byte[] asd = Utils.int16toByteArray(12);
		try
		{
			fichero = new FileWriter("C:\\Users\\david\\Documents\\UNI\\Redes de Ordenadores\\dns-java-ro8-199-master\\src\\prueba.txt",false); //Archivo log.
			pw = new PrintWriter(fichero);

		} catch (Exception e)
		{

			e.printStackTrace();
		}
		//Fecha introducida
		java.util.Date fecha = new Date();
		pw.println(fecha);


		// LEEMOS LA ENTRADA ESTANDAR.
		pw.println(">LEEMOS LA ENTRADA ESTANDAR");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				Busquedas.add(line);
			}
		}
		for (int x = 0; x < Busquedas.size(); x++)
		{
			String[] busque = Busquedas.get(x).trim().split(" ");
			String[] consultas = { busque[0], busque[1], args[0], args[1] };
			pw.println("-"+(x+1)+" de "+Busquedas.size()+" : "+consultas[0]+" |"+consultas[1]+" |"+consultas[2]+" |"+consultas[3]);
			// Preparamos los campos para crear un mensaje:
			busqueda=busque[0];
			boolean correcto=Preparar_mensaje(consultas, 0);
			if(!correcto)
			{
				if(!fallo)
				{
					pw.println("-->No hubo resultados satisfactorios para la busqueda " + url);
					System.out.println("-->No hubo resultados satisfactorios para la busqueda " + url);
					fallo=false;
				}
			}
			System.out.println("--------------------------------------------------------------------");
			consultado=false;
			ok=false;
		}
		if (null != fichero)
			fichero.close();
	}

	private static boolean Preparar_mensaje(String[] args, int boleano) throws Exception
	{
		registro = args[0];
		if (boleano == 0)
		{
			url = args[1];
			ip_dns_raiz=args[3];
		}
		if(boleano==2)
		{
			registro=args[0];
			aux=url;//almacenamos la url de la busqueda original.
			url=args[1];
		}
		String protocolo = args[2];
		// InetAddress:
		ip = InetAddress.getByName(args[3]);
		int puerto = 53;// puerto dns

		// Creamos el RRtype:
		RRType tipo = Generar_RRType(registro);

		// Creamos el message:
		DomainName dominios = new DomainName(url);
		Message question = new Message(dominios, tipo, false);
		byte[] Question_ByteArray = question.toByteArray();

		switch (protocolo)
		{
			case "-t":// TCP
						// IMPLEMENTAR
						System.out.println("-->Este programa no acepta conexiones TCP, seguiremos la consulta con UDP");
						protocolo="-u";
			case "-u":// UDP
				if(!consultado)
				{
						String cache[]=url.trim().split("\\.");
						String pregunta[]=new String[cache.length];
						for(int i=cache.length-1;i>=0;i--)
						{
							String aux="";
							for(int a=i;a<cache.length;a++)
							{
								aux+=cache[a];
								if(a!=cache.length-1)
								{
									aux+=".";
								}
							}
							pregunta[i]=aux;
						}
						for(int i=0;i<pregunta.length;i++)
						{
							if (Cache.containsKey(pregunta[i]))
							{
								if(i==0)
								{
									System.out.println("Q:CACHE" + " " + args[3] + " " + args[0] + " " + url);
									String[] imprimir = Cache.get(pregunta[i]);
									System.out.println(imprimir[0] + " " + imprimir[1] + " " + imprimir[2] + " " + imprimir[3] + " " + imprimir[4]);
									ok=true;
									break;
								}
								else
								{
									System.out.println("Q:CACHE" + " " + args[3] + " " + args[0] + " " + url);
									String[] imprimir = Cache.get(pregunta[i]);
									System.out.println(imprimir[0] + " " + imprimir[1] + " " + imprimir[2] + " " + imprimir[3] + " " + imprimir[5]);
									System.out.println(imprimir[0] + " " + imprimir[1] + " " + imprimir[2] + " " + imprimir[3] + " " + imprimir[4]);

									// Generamos el nuevo mensaje

									ip = InetAddress.getByName(imprimir[3]);
									puerto = 53;// puerto dns

									// Creamos el RRtype:
									tipo = Generar_RRType(registro);

									// Creamos el message:
									dominios = new DomainName(url);
									question = new Message(dominios, tipo, false);
									Question_ByteArray = question.toByteArray();

									//Cambiamos los argumentos:
									args[3]=imprimir[4];
									args[0]=imprimir[2];
							}
						}
						}
						consultado=true;

					}
					if(!ok)
					{
						pw.println("Q:UDP" + " " + args[3] + " " + args[0] + " " + url);
						System.out.println("Q:UDP" + " " + args[3] + " " + args[0] + " " + url);
						if(boleano==2)
						{
							url=aux;//devolvemos el valor orignial de la busqueda
						}
						try
						{
							if (Procesar_respuestas(udp(Question_ByteArray, ip, puerto)) == true)
							{
								return true;
							}
						} catch (Exception e)
						{
							pw.println("-->No hubo resultados satisfactorios para la busqueda " + url);
							System.out.println("-->No hubo resultados satisfactorios para la busqueda " + url);
						}
					}
					else
					{
						return true;
					}
					break;
				}
		return false;

	}

	public static RRType Generar_RRType(String registro)
	{

		RRType tipo = RRType.A;
		switch (registro) {
		case "A":
			tipo = RRType.A;
			break;
		case "NS":
			tipo = RRType.NS;
			break;
		case "CNAME":
			tipo = RRType.CNAME;
			break;
		case "SOA":
			tipo = RRType.SOA;
			break;
		case "PTR":
			tipo = RRType.PTR;
			break;
		case "HINFO":
			tipo = RRType.HINFO;
			break;
		case "MX":
			tipo = RRType.MX;
			break;
		case "TXT":
			tipo = RRType.TXT;
			break;
		case "AAAA":
			tipo = RRType.AAAA;
			break;
		default:
			tipo = null;
			break;
		}
		return tipo;
	}

	private static Message udp(byte[] question, InetAddress ip, int puerto) throws Exception
	{
		pw.println("> udp");
		DatagramSocket dgSocket = new DatagramSocket();

		// enviamos el datagrama
		DatagramPacket datagram_peticion = new DatagramPacket(question, question.length, ip, puerto);
		dgSocket.send(datagram_peticion);

		// variables para la respuesta del servidor
		byte[] bufer = new byte[5000];
		DatagramPacket datagram_respuesta = new DatagramPacket(bufer, bufer.length);
		// esperamos que nos llegue respuesta desde el servidor
		dgSocket.receive(datagram_respuesta);

		// ha llegado un datagrama, para ver los datos se utiliza un constructor
		// inverso de la clase Message
		Message respuesta_recibida = new Message(bufer);

		// cerramos el socket UDP
		dgSocket.close();

		// retornamos la respuesta
		return respuesta_recibida;
	}

	private static boolean Procesar_respuestas(Message respuesta) throws Exception
	{
		if (respuesta.getAnswers().size() != 0)
		{
			pw.println("> Answer>0 , tipo: "+respuesta.getAnswers().get(0).getRRType().toString()+" ,busqueda: "+busqueda);
			if (respuesta.getAnswers().get(0).getRRType().toString().equals(busqueda))
				{
					String cache[] = { "A", respuesta.getAnswers().get(0).getRRType().toString(),
							Procesar_ip(respuesta.getAnswers().get(0).getRRData()),
							Integer.toString(respuesta.getAnswers().get(0).getTTL()),
							respuesta.getAnswers().get(0).getDomain().toString() };

					Cache.put(respuesta.getAnswers().get(0).getDomain().toString().substring(0,
							respuesta.getAnswers().get(0).getDomain().toString().length() - 1), cache);//Le quitamos el punto que incluye la respuesta del dominio.
					System.out.println("A" + " " + ip.toString().replaceAll("/", "") + " "
							+ respuesta.getAnswers().get(0).getRRType() + " " + respuesta.getAnswers().get(0).getTTL()
							+ " " + Procesar_ip(respuesta.getAnswers().get(0).getRRData()));
					pw.println("A" + " " + ip.toString().replaceAll("/", "") + " "
							+ respuesta.getAnswers().get(0).getRRType() + " " + respuesta.getAnswers().get(0).getTTL()
							+ " " + Procesar_ip(respuesta.getAnswers().get(0).getRRData()));
					return true;
				}
				else
				{
					if(busqueda.equals("AAAA"))
					{
						pw.println("-->El programa no encontro Answers del tipo: "+busqueda);
						System.out.println("-->El programa no encontro Answers del tipo: "+busqueda);
						fallo=true;
					}
					else
					{
						pw.println("-->El programa no resuelve Answers del tipo: "+respuesta.getAnswers().get(0).getRRType().toString());
						System.out.println("-->El programa no resuelve Answers del tipo: "+respuesta.getAnswers().get(0).getRRType().toString());
						fallo=true;
					}
					return false;
				}
		}
		else
		{
			// Leeremos todas las respuestas: Dividiendolas en ipv4 y
			// ipv6. Almacenandolas.
			if (respuesta.getAdditonalRecords().size() != 0)
			{
				boolean encontrado=false;
				int contador=0;
				do
				{
					switch (respuesta.getAdditonalRecords().get(contador).getRRType().toString())
					{
								case "AAAA":// IPV6 NO FUNCIONA EN GRAN PARTE DE LAS
											// CONEXIONES NI EN LOS SOCKETS.
								break;

								case "A":// IPV4
									if (respuesta.getQuestionType().toString().equals("A") || respuesta.getQuestionType().toString().equals("NS") ||respuesta.getQuestionType().toString().equals("AAAA"))
									{
										encontrado=true;
										String[] array = { respuesta.getAdditonalRecords().get(contador).getRRType().toString(),
												respuesta.getAdditonalRecords().get(contador).getDomain().toString(), "-u",
												Procesar_ip(respuesta.getAdditonalRecords().get(contador).getRRData()), };
										System.out.println("A" + " " + ip.toString().replaceAll("/", "") + " "
												+ respuesta.getNameServers().get(contador).getRRType().toString() + " "
												+ respuesta.getAdditonalRecords().get(contador).getTTL() + " "
												+ respuesta.getAdditonalRecords().get(contador).getDomain());
										System.out.println("A" + " " + ip.toString().replaceAll("/", "") + " " + array[0] + " "
												+ respuesta.getAdditonalRecords().get(contador).getTTL() + " " + array[3]);
										pw.println("A" + " " + ip.toString().replaceAll("/", "") + " "
												+ respuesta.getNameServers().get(contador).getRRType().toString() + " "
												+ respuesta.getAdditonalRecords().get(contador).getTTL() + " "
												+ respuesta.getAdditonalRecords().get(contador).getDomain());
										pw.println("A" + " " + ip.toString().replaceAll("/", "") + " " + array[0] + " "
												+ respuesta.getAdditonalRecords().get(contador).getTTL() + " " + array[3]);

										if (respuesta.getNameServers().size() > 0)// Guardamos en la cache las busquedas realizadas.
										{
											String cache[] = { "A", ip.toString().replaceAll("/", ""),
													respuesta.getNameServers().get(contador).getRRType().toString(),
													Integer.toString(respuesta.getAdditonalRecords().get(contador).getTTL()),
													array[3],
													respuesta.getAdditonalRecords().get(contador).getDomain().toString() };
											Cache.put(
													respuesta.getNameServers().get(contador).getDomain().toString().substring(0,
															respuesta.getNameServers().get(contador).getDomain().toString().length() - 1),
													cache);// Le quitamos el . que incluye la respuesta en el dominio
										}
										if (Preparar_mensaje(array, 1) == true)
										{
											return true;
										}
									}
									break;

									default :
									break;
					}
					if(encontrado)
					{
						break;
					}
					contador++;
				}
				while(contador<respuesta.getAdditonalRecords().size());
			}
			else
			{
				if(respuesta.getNameServers().size() !=0)
				{
					String direccion=ByteToString(respuesta.getNameServers().get(0).getRRData());
					if(direccion.trim()!=null && direccion.trim()!="" && direccion.contains("."))
					{
						String[] array = { respuesta.getNameServers().get(0).getRRType().toString(),
								direccion, "-u",
								ip_dns_raiz, };
						System.out.println("A" + " " + ip.toString().replaceAll("/", "") + " "
										+ respuesta.getNameServers().get(0).getRRType().toString() + " "
										+ respuesta.getNameServers().get(0).getTTL() + " "
										+ respuesta.getNameServers().get(0).getDomain());
						System.out.println("A" + " " + ip.toString().replaceAll("/", "") + " " + array[0] + " "
										+ respuesta.getNameServers().get(0).getTTL() + " " + direccion );
						if (Preparar_mensaje(array, 2) == true)
						{
							return true;
						}
					}
					else
					{
						System.out.println("-->El programa no resuelve Answers del tipo: "+respuesta.getNameServers().get(0).getRRType().toString());
						return false;
					}
				}
			}
			if(respuesta.getAdditonalRecords().size() != 0 && respuesta.getAnswers().size() != 0)
			{
					System.out.println("No hay registro tipo " + respuesta.getQuestionType().toString()
						+ " en sección ADDITIONAL para " + respuesta.getQuestion().toString());
				return false;
			}
			else
			{
				return false;
			}
		}
	}

	private static String Procesar_ip(byte[] bytes)
	{
		String ip = "";
		if (bytes.length == 4)
		{
			ip = Byte.toUnsignedInt(bytes[0]) + "." + Byte.toUnsignedInt(bytes[1]) + "." + Byte.toUnsignedInt(bytes[2])
					+ "." + Byte.toUnsignedInt(bytes[3]);
		}
		if (bytes.length == 16)
		{
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			String cadena;
			int leer = input.read();
			while (leer != -1)
			{
				cadena = Integer.toHexString(leer);
				if (cadena.length() < 2)
				{
					ip += "0";
				}
				ip += cadena;
				leer = input.read();
			}
			String ip_final = "";
			while (ip.length() != 0)
			{
				ip_final = ip_final + ip.substring(0, 4) + ":";
				ip = ip.substring(4, ip.length());
			}
			ip = ip_final.substring(0, ip_final.length() - 1);

		}
		return ip;
	}
	static private String ByteToString (byte[] bs)
	{
		String file_string="";
		String fin="";
		String end=" ";
		for(int i=0;i<bs.length;i++)
		{
			if(bs[i]<44)
			{
				bs[i]=46;
			}
			file_string +=(char)bs[i];
		}
		if(validar(file_string))
		{
			char[]cad=file_string.toCharArray();
			for(int i=1;i<cad.length-1;i++)
			{
				if(i>0 && i<cad.length-1)//Para quitar los puntos del principio y del final.
				{
					fin +=cad[i];
				}
			}
			end=fin;
		}
		return end;
	}
	static private boolean validar(String doc)
	{
		boolean paso=false;
		char[] cadena=doc.toCharArray();
		int tam=cadena.length;
		if(cadena[0]==46 && cadena[tam-1]==46 && cadena[tam-2]!=46)
		{
			paso=true;
		}
		return paso;
	}
}
