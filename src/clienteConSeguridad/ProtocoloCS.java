package clienteConSeguridad;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;


import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class ProtocoloCS {

	public static void procedimiento(BufferedReader consoleReader, PrintWriter clientWriter, BufferedReader clientReader, Cliente c) {

		try {
			//ETAPA 1

			String protocolLine;			

			clientWriter.println("HOLA");
			protocolLine = clientReader.readLine();

			if(protocolLine.equals("OK")) 
			{

				String symmetricAlgorithm;

				int secretKeySize = 128;

				//Se selecciona algoritmo simetrico
				symmetricAlgorithm = Cliente.AES;

				//			Random rand = new Random();
				//			  int randomNum = rand.nextInt((2 - 1) + 1) + 1;
				//			switch (randomNum) {
				//			case 1:
				//				symmetricAlgorithm = Cliente.AES;
				//				break;
				//			case 2:
				//				symmetricAlgorithm = Cliente.BLOWFISH;
				//				break;
				//			default:
				//				symmetricAlgorithm = Cliente.AES;
				//				break;
				//			}

				String asymmetricAlgorithm = Cliente.RSA;

				//seleccion de algoritmo HMAC
				String macOption = Cliente.HMACSHA512;

				//			randomNum = rand.nextInt((4 - 1) + 1) + 1;
				//			switch (randomNum) {
				//			case 1:
				//				macOption = Cliente.HMACSHA1;
				//				break;
				//			case 2:
				//				macOption = Cliente.HMACSHA256;
				//
				//				break;
				//			case 3:
				//				macOption = Cliente.HMACSHA384;
				//				break;
				//			case 4:
				//				macOption = Cliente.HMACSHA512;
				//				break;
				//			default :
				//				authOption = Cliente.HMACSHA512;
				//			}

				String algorithms = "ALGORITMOS:"+symmetricAlgorithm+":"+asymmetricAlgorithm+":"+macOption;

				clientWriter.println(algorithms);
				protocolLine = clientReader.readLine();

				//ETAPA 2
				if(!protocolLine.equals(Cliente.ERROR))
				{

					String serverCertificate = "";
					protocolLine = clientReader.readLine();
					if(protocolLine != null)
					{
						serverCertificate = protocolLine;
					}

					//Obtener llave publica servidor
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					byte[] certificadoServerEnBytes = parserBase64Binary(serverCertificate);
					InputStream in = new ByteArrayInputStream(certificadoServerEnBytes);
					X509Certificate certificadoServer = (X509Certificate) cf.generateCertificate(in);	

					//Se obtiene llave publica del servidor	
					PublicKey llavePubServer = certificadoServer.getPublicKey();

					//Se crea la llave simetrica
					KeyGenerator keygen = KeyGenerator.getInstance(symmetricAlgorithm);
					keygen.init(secretKeySize);
					SecretKey symmetricKey = keygen.generateKey();

					//Cifrado respecto a la llave publica del servidor
					Cipher cifrador = Cipher.getInstance(asymmetricAlgorithm);
					byte[] llaveSecretaEnBytes = symmetricKey.getEncoded();
					cifrador.init(Cipher.ENCRYPT_MODE, llavePubServer);
					byte[] byteCifradoLlaveSimetrica = cifrador.doFinal(llaveSecretaEnBytes);

					//envio de llave simetrica cifrada con publica del servidor
					clientWriter.println(printBase64Binary(byteCifradoLlaveSimetrica));
					String reto = Cliente.setClave();
					clientWriter.println(reto);

					//Mensaje que envia el servidor cifrado con llave simetrica
					protocolLine = clientReader.readLine();

					//Se desencripta el reto que envia de vuelta el servidor para ver si es la misma llave simetrica que generamos
					cifrador = Cipher.getInstance(symmetricAlgorithm);
					cifrador.init(Cipher.DECRYPT_MODE, symmetricKey);
					byte[] descifrado = parserBase64Binary(protocolLine);
					byte[] retoEnByte  = cifrador.doFinal(descifrado);
					String retoServidor = printBase64Binary(retoEnByte);

					//verificamos igualdad entre el reto generado por cliente y el reto que envia el servidor

					boolean continuar = false;	
					if(retoServidor.equals(reto))
					{
						clientWriter.println("OK");
						continuar = true;
					}
					else
					{
						System.out.println("Error. No se intercambio bien la llave simetrica");
						clientWriter.println("ERROR");
					}

					if(continuar) 
					{
						//Generamos los datos
//						String datos = Cliente.getCedula();
//						String clave = Cliente.getClave();
						
						byte[] datosEnBytes = parserBase64Binary(c.getCedula());
						byte[] claveEnBytes = parserBase64Binary(c.getClave());

						cifrador = Cipher.getInstance(symmetricAlgorithm);
						cifrador.init(Cipher.ENCRYPT_MODE, symmetricKey);
						byteCifradoLlaveSimetrica = cifrador.doFinal(datosEnBytes);
						clientWriter.println(printBase64Binary(byteCifradoLlaveSimetrica));
						
						cifrador = Cipher.getInstance(symmetricAlgorithm);
						cifrador.init(Cipher.ENCRYPT_MODE, symmetricKey);
						byteCifradoLlaveSimetrica = cifrador.doFinal(claveEnBytes);
						clientWriter.println(printBase64Binary(byteCifradoLlaveSimetrica));
						
						//Valor de monto ahorro que responde el servidor
						protocolLine = clientReader.readLine();
						cifrador = Cipher.getInstance(symmetricAlgorithm);
						cifrador.init(Cipher.DECRYPT_MODE, symmetricKey);
						descifrado = parserBase64Binary(protocolLine);
						byte[] montoEnByte  = cifrador.doFinal(descifrado);
						String montoCliente = printBase64Binary(montoEnByte);
						System.out.println("El monto es de " + montoCliente + " COP");
						
						//Se descifra con la llave publica del servidor
						protocolLine = clientReader.readLine();
						cifrador = Cipher.getInstance(asymmetricAlgorithm);
						cifrador.init(Cipher.DECRYPT_MODE, llavePubServer);
						descifrado = parserBase64Binary(protocolLine);
						byte[] hMacCifradoEnBytes  = cifrador.doFinal(descifrado);
						String hMac = printBase64Binary(hMacCifradoEnBytes);
						
						//HMAC de los datos
//						long tiempoInicial = System.currentTimeMillis();
						Mac mac = Mac.getInstance(macOption);
						mac.init(symmetricKey);

						byte[] bytesHMacEncrypt = mac.doFinal(montoEnByte);
//						long tiempoFinal = System.currentTimeMillis();
//						System.out.println("Tiempo de cifrado mac es: " + (tiempoFinal - tiempoInicial) + "ms");
						String hashCifradoenString = printBase64Binary(bytesHMacEncrypt);

						if(hMac.equals(hashCifradoenString))
						{
							clientWriter.println(Cliente.OK);
							System.out.println("Consulta finalizada.");
						}
						else
						{
							System.out.println("Error. El hmac calculado no es igual al que se recibio");
							clientWriter.println(Cliente.ERROR);
						}
					}
					else
					{
						System.err.println(Cliente.ERROR);
					}
				}
				else
				{
					System.out.println("Error al enviar los algoritmos al servidor");
				}
			}
			else
			{
				System.out.println("Error. El servidor no respondio de manera correcta.");
			}
		}
		catch (Exception e)
		{
			e.getMessage();
		}
	}

	public static String printBase64Binary(byte[] certificadoEnBytes) 
	{
		return DatatypeConverter.printBase64Binary(certificadoEnBytes);
	}

	public static byte[] parserBase64Binary(String certificadoEnBytes) 
	{
		return DatatypeConverter.parseBase64Binary(certificadoEnBytes);
	}

	public static java.security.cert.X509Certificate generarCertificado(KeyPair kp) 
	{
		try
		{

			Provider pro = new BouncyCastleProvider();
			Security.addProvider(pro);
			
			//Fecha inicial de expedicion del certificado
			long fechaActual = System.currentTimeMillis();
			BigInteger sn = new BigInteger(Long.toString(fechaActual));
			Date fechaDefinitiva = new Date(fechaActual);

			X500Name name = new X500Name("CN=localhost");

			Date fechaFinal = new Date(System.currentTimeMillis());
			
			//Firma que se hace sobre el certificado
			ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(kp.getPrivate());
			
			//Lo genero con mi llave publica para que el server lo pueda leer y sea correcto
			JcaX509v3CertificateBuilder constructorCertificador = new JcaX509v3CertificateBuilder(name, sn, fechaDefinitiva, fechaFinal, name, kp.getPublic());

			constructorCertificador.addExtension(new ASN1ObjectIdentifier("10.23.19"), true, new BasicConstraints(true));

			//Certificado finalizado
			X509Certificate certificado = new JcaX509CertificateConverter().setProvider(pro).getCertificate(constructorCertificador.build(signer));

			return certificado;

		}
		catch(OperatorCreationException  | CertificateException ce)
		{
			ce.printStackTrace();
			return null;
		} 
		catch (CertIOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
