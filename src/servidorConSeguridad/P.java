package servidorConSeguridad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class P {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */
	private static int conexionesPerdidas;
	private static int conexionesNoPerdidas;

	public static String antes = "% Antes";
	public static String durante = "% Durante";
	public static String despues = "% Despues";
	public static String tiempo = "Tiempo de transaccion (ms)";
	public static int size;
	public static double getSystemCpuLoad() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });
		if (list.isEmpty()) return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value = (Double)att.getValue();
		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
	}
	
	/**
	 * Genera un CSV Con la información guardada de la prueba realizada
	 * @throws FileNotFoundException - Si no encuentra el archivo
	 */
	private static void generarCSV() throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter("./docs/logsConSeguridad/resultadosConSeguridad ("+ (new Date()).toString().replaceAll(":", ".") + ").csv");
		String perdidas = "Conexiones perdidas" + ";" + (size - conexionesNoPerdidas) ;
		pw.println(tiempo);
		pw.println(antes);
		pw.println(durante);
		pw.println(despues);
		pw.println(perdidas);
		
		pw.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		int ip = Integer.parseInt(br.readLine());
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		
		// Crea el archivo de log
		conexionesPerdidas = 0;
		conexionesNoPerdidas = 0;
		System.out.println("Ingrese el tamano del pool de threads");
		int cant = Integer.parseInt(br.readLine());
		System.out.println("Ingrese la carga:");
		size = Integer.parseInt(br.readLine());
		File file = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		String ruta = "./docs/logsConSeguridad/resultados.txt";
//		String rutalog = "./logs.txt";

        file = new File(ruta);
        if (!file.exists()) {
            file.createNewFile();
        }
//        FileWriter fw = new FileWriter(file);
//        fw.close();
        
        D.init(certSer, keyPairServidor, file);
        
		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");
		ExecutorService pool = Executors.newFixedThreadPool(cant);
		D.init(certSer, keyPairServidor, file);

		for (int i=0;i<size;i++) {
			try { 
				Socket sc = ss.accept();
				System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
				D d = new D(sc,i);
				pool.execute(d);
				conexionesNoPerdidas++;
//				d.start();
			} catch (IOException e) {
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				e.printStackTrace();
			}
		}
		
		pool.shutdown();
		while (!pool.awaitTermination(15, TimeUnit.SECONDS)) {}
		System.out.println(MAESTRO + "Guardando información en archivo");
		generarCSV();
		System.out.println(MAESTRO + "Información guardada éxitosamente");
		br.close();
	}

	public static synchronized void registrarConexionesPerdidas() {
		conexionesPerdidas++;
	}

	public static synchronized void registrarAntes(double systemCpuLoad) {
		antes += ";" + systemCpuLoad;
	}
	public static synchronized void registrarDurante(double systemCpuLoad) {
		durante += ";" + systemCpuLoad;
	}
	public static synchronized void registrarDespues(double systemCpuLoad) {
		despues += ";" + systemCpuLoad;
	}

	public static synchronized void registrarTiempo(long time) {
		tiempo += ";" + time;
	}
	
}
