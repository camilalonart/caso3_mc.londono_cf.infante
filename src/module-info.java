module caso3_mc.londono_cf.infante {
	exports clienteProtocolos;
	exports servidorConSeguridad;
	exports servidorSinSeguridad;

	requires GLoad13;
	requires java.management;
	requires javax.xml.bind;
	requires org.bouncycastle.pkix;
	requires org.bouncycastle.provider;
}