package caso2_mc.londono_cf.infante;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Cliente extends Thread{

	public static final int PUERTO = 8000;
	private String cedula;
	private String clave;

	// Authentication codes for algorithms
	public static final String AES = "AES";
	public static final String BLOWFISH ="Blowfish";
	public static final String RSA = "RSA"; 
	public static final String HMACSHA1 = "HMACSHA1"; 
	public static final String HMACSHA256 = "HMACSHA256";
	public static final String HMACSHA384 = "HMACSHA384";
	public static final String HMACSHA512 = "HMACSHA512";

	public static final String ERROR = "ERROR";
	public static final String OK = "OK";


	public String getClave() {
		return clave;
	}
	
	public String getCedula() {
		return cedula;
	}
	
	public static String setClave() 
	{ 
		Random rand = new Random();
		int n = rand.nextInt((5 - 1) + 10) * 4;

		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
				+ "0123456789"
				+ "abcdefghijklmnopqrstuvxyz"; 

		StringBuilder sb = new StringBuilder(n); 

		for (int i = 0; i < n; i++)
		{ 
			int index = (int)(AlphaNumericString.length() * Math.random()); 

			sb.append(AlphaNumericString.charAt(index)); 
		} 

		return sb.toString(); 
	} 

	public static String setCedula() 
	{ 
		int n = 12;

		String AlphaNumericString = "0123456789";  
		StringBuilder sb = new StringBuilder(n); 

		for (int i = 0; i < n; i++) { 

			int index = (int)(AlphaNumericString.length() * Math.random()); 

			sb.append(AlphaNumericString.charAt(index)); 
		} 

		return sb.toString(); 
	} 

	@Override
	public void run() 
	{
		try 
		{
			iniciar(this);
		} 
		catch(Exception e) 
		{
			System.err.println(e.getMessage());
		}
	}

	public void iniciar(Cliente c) {
		try {

			Socket socket = new Socket("localhost", PUERTO);
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader lectorC = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("Introduzca su cedula:");
			String pcedula = lectorC.readLine();
			if(pcedula.length() % 4 == 0) {
				cedula = pcedula;
			}else if(pcedula.length() % 4 == 3) {
				cedula = pcedula+="0";
			}else if(pcedula.length() % 4 == 2) {
				cedula = pcedula+="00";
			}else if(pcedula.length() % 4 == 1) {
				cedula = pcedula+="000";
			}
			
			System.out.println("Introduzca su clave:");
			String pclave = lectorC.readLine();
			if(pclave.length() % 4 == 0) {
				clave = pclave;
			}else if(pclave.length() % 4 == 3) {
				clave = pclave+="0";
			}else if(pclave.length() % 4 == 2) {
				clave = pclave+="00";
			}else if(pclave.length() % 4 == 1) {
				clave = pclave+="000";
			}
			
			Protocolo.procedimiento(lectorC, pw, bf, c);
			
			lectorC.close();
			bf.close();
			pw.close();
			socket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Cliente c = new Cliente();
		c.start();
	}

}
