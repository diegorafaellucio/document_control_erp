package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.RelatorioGeralCampos;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.other.ExcelCsvWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class RelatorioCandidatoService {

	private final String SUFIX_DESC_CAMPO_BASE_INTERNA = " Desc.";

	@Autowired private ProcessoService processoService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private MessageService messageService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private RelatorioGeralService relatorioGeralService;

	private LinkedMultiValueMap<String, String> renderCabecalho(ExcelCsvWriter ew, boolean carregarCamposDinamicos) {

		ew.escrever(RelatorioGeralCampos.ColunasEnum.PROCESSO_ID.getNome(), 3000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.MOTIVO_DA_REQUISICAO.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.NOME.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.NOME_SOCIAL.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.CPF.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PASSAPORTE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.IDENTIDADE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ORGAO_EMISSOR.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_EMISSAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.MAE.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PAI.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_DE_CRIACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO.getNome(), 8000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.STATUS.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRIORIDADE.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ORIGEM.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.MODALIDADE.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR_LOGIN.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR_PERFIL.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA_LOGIN.getNome());

		if(carregarCamposDinamicos) {
			LinkedMultiValueMap<String, String> ordenados = tipoCampoService.findListByGrupoAndNome(null);
			for (String grupo : ordenados.keySet()) {
				List<String> campos = ordenados.get(grupo);
				for (String campo : campos) {
					ew.escrever(grupo + " - " + campo, 8000);
				}
			}

			return ordenados;
		}
		return null;
	}

	public File render(ProcessoFiltro filtro, Usuario usuario) {

		try {

			String fileOrigemNome =  "relatorio-candidato.xlsx";

			String extensao = DummyUtils.getExtensao(fileOrigemNome);

			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/" + fileOrigemNome);

			File file = File.createTempFile("relatorio-candidato-", "." + extensao);
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);

			ExcelCsvWriter ecw = new ExcelCsvWriter();

			ExcelWriter ew = new ExcelWriter();
			ew.criarArquivo(extensao);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);
			ew.criaPlanilha("Processos");
			ecw.setWriter(ew);

			ecw.criaLinha(0);

			List<Long> processosIds = processoService.findIdsByFiltro(filtro, null, null);
			if(processosIds.size() > 0) {
				renderRowsProcessos(ecw, usuario, processosIds);
			}

			DummyUtils.deleteFile(file);
			File fileDestino = File.createTempFile("relatorio-candidato", ".xlsx");
			DummyUtils.deleteOnExitFile(fileDestino);

			FileOutputStream fos = new FileOutputStream(fileDestino);
			workbook.write(fos);
			workbook.close();

			return fileDestino;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRowsProcessos(ExcelCsvWriter ew, Usuario usuario, List<Long> ids) {

		int total = ids.size();
		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		int rowNum = 1;
		LinkedMultiValueMap<String, String> headerDinamicoOrdenado = null;
		Map<Long, Map<String, RegistroValorVO>> basesInternasMap = relatorioGeralService.getBasesInternasMap();
		do {
			List<Long> ids2 = DummyUtils.removeItens(ids, 200);
			String login = usuario.getLogin();

			List<Processo> list = processoService.findByIds(ids2);
			for (int i = 0; i < list.size(); i++) {

				Processo processo = list.get(i);

				if(rowNum == 1) {
					renderCabecalho(ew, false);
				}
				ew.criaLinha(rowNum++);
				renderBody(ew, processo, false, basesInternasMap, headerDinamicoOrdenado);

				if(rowNum % 100 == 0) {
					long fim = System.currentTimeMillis();
					systraceThread(rowNum + " de " + total + ". usuário: " + login + ". " + (fim - inicio) + "ms.");
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderBody(ExcelCsvWriter ew, Processo processo, boolean carregarCamposDinamicos, Map<Long, Map<String, RegistroValorVO>> basesInternasMap, LinkedMultiValueMap<String, String> headerDinamicoOrdenado) {

		Long processoId = processo.getId();
		String formaIngresso = "";
		String camposDinamicos = relatorioGeralService.getCamposDinamicosJson(basesInternasMap, processoId);

		ew.escrever(processoId);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		String tipoProcessoNome = tipoProcesso == null ? "": tipoProcesso.getNome();
		ew.escrever(tipoProcessoNome);

		Aluno aluno = processo.getAluno();
		String nome = aluno.getNome();
		ew.escrever(nome);

		String nomeSocial = aluno.getNomeSocial();
		ew.escrever(nomeSocial);

		String cpf = aluno.getCpf();
		ew.escrever(cpf);

		String passaporte = aluno.getPassaporte();
		ew.escrever(passaporte);

		String identidade = aluno.getIdentidade();
		ew.escrever(identidade);

		String orgaoEmissor = aluno.getOrgaoEmissor();
		ew.escrever(orgaoEmissor);

		Date dataEmissao = aluno.getDataEmissao();
		String dataEmissaoStr = DummyUtils.formatDate(dataEmissao);
		ew.escrever(dataEmissaoStr);

		String mae = aluno.getMae();
		ew.escrever(mae);

		String pai = aluno.getPai();
		ew.escrever(pai);

		Date dataCriacao = processo.getDataCriacao();
		String dataCriacaoStr = DummyUtils.formatDate(dataCriacao);
		ew.escrever(dataCriacaoStr);

		Situacao situacao = processo.getSituacao();
		String situacaoNome = situacao != null ? situacao.getNome() : "";
		ew.escrever(situacaoNome);

		StatusProcesso status = processo.getStatus();
		String statusStr = messageService.getValue("StatusProcesso." + status.name() + ".label");
		ew.escrever(statusStr);

		Integer nivelPrioridade = processo.getNivelPrioridade();
		ew.escrever(nivelPrioridade);

		String origem = processo.getOrigem() != null ? processo.getOrigem().name() : "";
		ew.escrever(origem);

		String modalidade = processoService.getModalidade(processoId);
		ew.escrever(modalidade);

		Usuario autor = processo.getAutor();
		String autorNome = autor != null ? autor.getNome() : "";
		ew.escrever(autorNome);

		String autorLogin = autor != null ? autor.getLogin() : "";
		ew.escrever(autorLogin);

		String autorRole = autor != null ? messageService.getValue("RoleGD." + autor.getRoleGD() + ".label") : "";
		ew.escrever(autorRole);

		Usuario analista = processo.getAnalista();
		String analistaNome = analista != null ? analista.getNome() : "";
		ew.escrever(analistaNome);

		String analistaLogin = analista != null ? analista.getLogin() : "";
		ew.escrever(analistaLogin);

		if (carregarCamposDinamicos) {
			if(headerDinamicoOrdenado != null && camposDinamicos != null) {
				Map<String, Map<String, String>> mapCamposDinamicos = (Map<String, Map<String, String>>) DummyUtils.jsonStringToMap(camposDinamicos);
				for (String grupo : headerDinamicoOrdenado.keySet()) {
					List<String> campos = headerDinamicoOrdenado.get(grupo);
					for (String campo : campos) {
						Map<String, String> camposValores = mapCamposDinamicos.get(grupo);
						campo = StringUtils.upperCase(campo);
						String valor = camposValores != null ? camposValores.get(campo) : "";
						String campoDesc = campo + SUFIX_DESC_CAMPO_BASE_INTERNA;
						if(camposValores != null && camposValores.containsKey(campoDesc)) {
							valor = camposValores.get(campoDesc);
						}
						ew.escrever(valor);
					}
				}
			}
		}
	}
}
