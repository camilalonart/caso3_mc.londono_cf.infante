package servidorConSeguridad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import clienteProtocolos.Generator;
import servidorConSeguridad.P;
import servidorConSeguridad.D;

public class P {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */
	private static int totalActions;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//Se inicia con el pool de threads para el servidor
		System.out.println("Numero de threads que desea usar: ");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		int threadsNum = Integer.parseInt(br.readLine());
		ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
		
		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		int ip = Integer.parseInt(br.readLine());
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		

		// Crea el archivo de log
		File file = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		String ruta = "./resultados.txt";
   
        file = new File(ruta);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        fw.close();
        
        D.init(certSer, keyPairServidor, file);
        
		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");
		
		totalActions = Integer.MAX_VALUE;
		for(int i = 0; i < totalActions; i++)
		{
			try 
			{
				Socket sc = ss.accept();
				executorService.execute(new Runnable() 
				{
					@Override
					public void run() 
					{	
						long idThread = Thread.currentThread().getId();
						System.out.println(MAESTRO + "Cliente " + idThread + " aceptado.");
						
						D d = new D(sc,(int)idThread);
						d.start();
						

							totalActions = Generator.getNumberOfTasks();
					}
				});


					
			} 
			catch (IOException e) 
			{
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				e.printStackTrace();
			}
		}
        
//		for (int i=0;true;i++) {
//			try { 
//				Socket sc = ss.accept();
//				System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
//				D d = new D(sc,i);
//				d.start();
//			} catch (IOException e) {
//				System.out.println(MAESTRO + "Error creando el socket cliente.");
//				e.printStackTrace();
//			}
//		}
	}
}
