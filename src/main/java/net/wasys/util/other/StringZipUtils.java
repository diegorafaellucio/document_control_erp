package net.wasys.util.other;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import lombok.extern.slf4j.Slf4j;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;

import static net.wasys.util.DummyUtils.systraceThread;

public class StringZipUtils {

	public static byte[] compress(String str) {

		if (StringUtils.isBlank(str)) {
			return null;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			OutputStream out = new DeflaterOutputStream(baos);
			out.write(str.getBytes("UTF-8"));
			out.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] byteArray = baos.toByteArray();
		return byteArray;
	}

	public static String uncompress(byte[] bytes) {

		if (bytes == null || bytes.length == 0) {
			return null;
		}

		InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[8192];
			int len;
			while((len = in.read(buffer)) > 0) {
				baos.write(buffer, 0, len);
			}
			return new String(baos.toByteArray(), "UTF-8");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws IOException {

		String string = "Durante o período, os dois não poderão votar e ser votados. De acordo com a sentença, eles também terão que ressarcir os cofres públicos em R$ 165.979,44, acrescidos de correção monetária e multa de 1% ao mês. Rosinha e Ricardo Bruno também foram condenados a pagar uma multa civil de igual valor e arcar com as despesas processuais. Aplico as sanções de ressarcimento integral do dano, suspensão dos direitos políticos por apenas cinco anos e pagamento de multa civil de apenas um vez o valor do dano e, ainda, de proibição de contratar com o Poder Público ou receber benefícios ou incentivos fiscais ou creditícios, direta ou indiretamente, ainda que por intermédio de pessoa jurídica da qual sejam sócios majoritários, pelo prazo de cinco anos, diz a sentença A publicação de um informe publicitário deu origem à ação. Segundo o Ministério Público, em outubro de 2004, às vésperas do segundo turno das eleições municipais, o governo do Rio deflagrou diversos programas assistenciais em Campos, reduto eleitoral Rosinha. O governo promoveu o cadastramento e distribuição de benefícios do Cheque Cidadão (no valor de R$ 100) e do Morar Feliz (entrega de casas populares), além da distribuição extemporânea de material escolar.";
		systraceThread("after compress: " + string.getBytes().length);
		byte[] compress = StringZipUtils.compress(string);
		systraceThread("compressed size: " + compress.length);
		systraceThread(StringZipUtils.uncompress(compress));
	}
}
