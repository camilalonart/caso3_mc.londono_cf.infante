package clienteProtocolos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class Generator 
{
	/**
	 * Load Generator Service
	 */
	private LoadGenerator generator;
	
	private int protocolo;
		
	/**
	 * Constructs a new Generator
	 */
	public Generator(int pProtocol, int taskNum, int gapTime) 
	{
		protocolo = pProtocol;
		Task work = createTask();
		
		generator = new LoadGenerator("Client - Server Load Test", taskNum, work, gapTime);
		generator.generate();
		
	}

	/**
	 * Helper that construct the task
	 * @return de clientserver task
	 */
	private Task createTask()
	{
		return new ClientServerTask(protocolo);
	}
	/**
	 * Starts the App
	 * @param args
	 */
	public static void main(String[] args) 
	{		
		BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Seleccione el tipo de protocolo :");
		System.out.println("1.Protocolo sin seguridad");
		System.out.println("2.Protocolo con seguridad");
		
		try
		{
			int option = Integer.parseInt(bf.readLine());
			System.out.println("Ingrese numero de transacciones a ejecutar");
			int taskNum = Integer.parseInt(bf.readLine());
			System.out.println("Ingrese tiempo gap (ms) entre cada transaccion");
			int gapTime = Integer.parseInt(bf.readLine());
			@SuppressWarnings("unused")
			Generator gen = new Generator(option, taskNum, gapTime);
			bf.close();
		}
		catch(Exception e)
		{
			System.err.println("ALGO FALLO EN LA LECTURA DEL PROTOCOLO ESCOGIDO");
		}

	}

}
