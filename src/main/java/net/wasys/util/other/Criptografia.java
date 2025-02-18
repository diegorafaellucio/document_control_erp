package net.wasys.util.other;

import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import static net.wasys.util.DummyUtils.systraceThread;

public class Criptografia {

	public static String FINAL = "%#$";
	private static final Encoder urlBase64Encoder = Base64.getUrlEncoder();
	private static final Decoder urlBase64Decoder = Base64.getUrlDecoder();

	public static final Chave SENHA = new Chave("mst0GwHnrXGLx7R9", "tS5hh6nJyNPQJ0cH");
	public static final Chave CERT_COD = new Chave("asd0GwHnKXGcxSR9", "lS5hh60JyNnQJycH");
	public static final Chave GERAL_ESTACIO = new Chave("rS55GsuairXHZ5BB", "wocnFx422Qxa96Af");
	private static boolean desabilitado;

	public static void desabilitar() {
		desabilitado = true;
	}

	public static boolean isHabilitada() {
		return !desabilitado;
	}

	public static class Chave {

		private Cipher encripta;
		private Cipher decripta;

		public Chave(String key, String iv) {
			try {
				SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

				encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
				encripta.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv.getBytes("UTF-8")));

				decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
				decripta.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv.getBytes("UTF-8")));
			}
			catch (Exception e) {
				e.printStackTrace();
				new RuntimeException(e);
			}
		}
	}

	public static String encrypt(Chave chave, String textopuro) {
		if(desabilitado) {
			return textopuro;
		}
		try {
			byte[] doFinal = chave.encripta.doFinal(textopuro.getBytes("UTF-8"));
			String encripted = encode(doFinal);
			return encripted + FINAL;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(Chave chave, String textoencriptado) {

		if(desabilitado) {
			return textoencriptado;
		}

		if(StringUtils.isBlank(textoencriptado)) {
			return textoencriptado;
		}

		try {
			String aux = textoencriptado.replaceAll("%#\\$$", "");
			byte[] decodeBase64 = decode(aux);
			byte[] doFinal = chave.decripta.doFinal(decodeBase64);
			String decripted = new String(doFinal, "UTF-8");
			return decripted;
		}
		catch (javax.crypto.IllegalBlockSizeException e) {
			systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			return textoencriptado;
		}
		catch (javax.crypto.BadPaddingException e) {
			systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			return textoencriptado;
		}
		catch (IllegalArgumentException e) {
			systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			return textoencriptado;
		}
		catch (Exception e) {
			systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			throw new RuntimeException(e);
		}
	}

	public static String encryptIfNot(Chave chave, String str) {
		if(desabilitado) {
			return str;
		}
		if(StringUtils.isBlank(str)) {
			return str;
		}
		if(str.endsWith(FINAL)) {
			return str;
		}
		return encrypt(chave, str);
	}

	private static String encode(byte[] bytes) {
		String encripted = urlBase64Encoder.encodeToString(bytes);
		return encripted;
	}

	private static byte[] decode(String aux) {
		byte[] decodeBase64 = urlBase64Decoder.decode(aux);
		return decodeBase64;
	}

	public static void main(String[] args) {

		String encrypt = Criptografia.encrypt(Criptografia.SENHA, "GM6n!GRBdjaJ");
		String decript = Criptografia.decrypt(Criptografia.SENHA, encrypt);
		DummyUtils.systraceThread(encrypt + " - " + decript);
		DummyUtils.systraceThread(Criptografia.decrypt(Criptografia.SENHA, "WNt04taeI5itV0iMOjXlkg==%#$"), LogLevel.ERROR);
	}
}
