package clienteProtocolos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Cliente extends Thread{

	public static final int PUERTO = 8000;
	private static String cedula;
	private String clave;
	private int protocolo;

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
	
	public Cliente(int pProtocol){
		protocolo = pProtocol;
	}


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
		cedula = sb.toString();

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

			cedula = setCedula();
			clave = setClave();
			switch (protocolo) {
			case 1:		
				ProtocoloSS.procedimiento(lectorC, pw, bf, c);
				break;

			case 2:
				try
				{
					ProtocoloCS.procedimiento(lectorC, pw, bf, c);		
					break;

				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			ProtocoloCS.procedimiento(lectorC, pw, bf, c);
			
			lectorC.close();
			bf.close();
			pw.close();
			socket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}


}
