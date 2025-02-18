package net.wasys.getdoc;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.wasys.getdoc.domain.enumeration.FaceRecognitionApi;

public interface GetdocConstants {

	Locale LOCALE_PT_BR = new Locale("pt", "BR");

	String DATE_FORMAT_4 = "ddMMyyyy";

	String EXTENSAO_DEFINICAO_DOCUMENTO = "fcdot";
	String EXTENSAO_DEFINICAO_IMAGEM = "jpg";
	String EXTENSAO_DEFINICAO_CSV = "csv";
	String EXTENSAO_DEFINICAO_TXT = "txt";
	String EXTENSAO_DEFINICAO_XLSX = "xlsx";
	String EXTENSAO_DEFINICAO_XLS = "xls";
	String EXTENSAO_DEFINICAO_PDF = "pdf";
	String EXTENSAO_DEFINICAO_ZIP = "zip";
	List<String> EXTENSAO_DEFINICAO_HTML = Arrays.asList("html", "htm");

	long FILE_SIZE_LIMIT = 10 * 1024 * 1024;
	List<String> IMAGEM_EXTENSOES = Arrays.asList("jpg", "jpeg", "png");
	List<String> IMAGEM_EXTENSOES_DOWNLOAD = Arrays.asList("pdf", "jpg", "jpeg", "png");
	int TAMANHO_MAXIMO_IMAGEM_KB = (int) (1.5 * 1024);//1.5 * 1024 = 1,5M

	FaceRecognitionApi FACE_RECOGNITION_API_DEFAULT = FaceRecognitionApi.BETAFACE;

	int NUMERO_TENTATIVAS_NOTIFICACAO_CANDIDATO = 7;
	int MINIMO_DOCUMENTACAO_ENVIO_ANALISE_FIES = 3;
	int MINIMO_DOCUMENTACAO_ENVIO_ANALISE_PROUNI = 4;

	int QUERY_TIMEOUT = 999999;
	int EXCEL_MAX_ROWS = 1048570;
	int TAMANHO_BUFFER = 4096;
	String LOGIN_AZURE = "login-azure";
	String ORIGINAL_URL = "ORIGINAL_URL";
	List<String> OCR_ACCEPTED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf");
}
