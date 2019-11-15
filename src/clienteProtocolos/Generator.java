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
	
	private static int numberOfTasks=5;
	
	/**
	 * Constructs a new Generator
	 */
	public Generator(int pProtocol) 
	{
		protocolo = pProtocol;
		Task work = createTask();
		
		 int gapBetweenTasks = 100;
		generator = new LoadGenerator("Client - Server Load Test", numberOfTasks, work, gapBetweenTasks);
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
			@SuppressWarnings("unused")
			Generator gen = new Generator(option);

		}
		catch(Exception e)
		{
			System.err.println("ALGO FALLO EN LA LECTURA DEL PROTOCOLO ESCOGIDO");
		}

	}
	public static int getNumberOfTasks() {
		return numberOfTasks;
	}
	public static void setNumberOfTasks(int numberOfTasks) {
		Generator.numberOfTasks = numberOfTasks;
	}

}
