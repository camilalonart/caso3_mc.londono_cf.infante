package clienteProtocolos;

import uniandes.gload.core.Task;

public class ClientServerTask extends Task
{
	private int protocoloCliente;
	
	public ClientServerTask(int pProto) 
	{
		protocoloCliente = pProto;
	}

	@Override
	public void fail() 
	{			
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() 
	{
		System.out.println(Task.OK_MESSAGE);
	}

	@Override
	public void execute() 
	{
		Cliente client;
		try 
		{
			client = new Cliente(protocoloCliente);
			client.run();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
