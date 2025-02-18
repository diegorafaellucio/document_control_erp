package net.wasys.getdoc.domain.service.webservice.aluno;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.vo.AlunoConsultaVO;
import net.wasys.getdoc.rest.response.vo.AlunoDocumentosDigitalizadosList;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetDocAlunoService {

	@Autowired private GetDocAlunoDocumentosReaproveiteisService getDocAlunoDocumentosReaproveiteisService;
	@Autowired private GetDocAlunoDocumentosDigitalizadosService getDocAlunoDocumentosDigitalizadosService;
	@Autowired private GetDocAlunoConsultaMatriculaService getDocAlunoConsultaMatriculaService;
	@Autowired private EmailSmtpService emailSmtpService;

	public Map<String, List<String>> consultarDocumentosReaproveitaveis(AlunoConsultaVO vo) {

		ConsultaExterna ce = getDocAlunoDocumentosReaproveiteisService.consultar(vo);

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			String method = DummyUtils.getCurrentMethodName();
			String stackTrace = ce.getStackTrace();
			emailSmtpService.enviarEmailException(method, stackTrace, mensagem);
			return new HashMap<>();
		}

		String resultado = ce.getResultado();
		Map<?, ?> result = DummyUtils.jsonStringToMap(resultado);

		return (Map<String, List<String>>) result.get("documentosPaths");
	}

	public AlunoDocumentosDigitalizadosList consultarDocumentosDigitalizados(int maxDeDocumentos) {
		AlunoConsultaVO alunoConsultaVO = new AlunoConsultaVO(TipoConsultaExterna.DOCUMENTOS_DIGITALIZADOS_GETDOC_ALUNO);
		alunoConsultaVO.setQuantidade(maxDeDocumentos);
		ConsultaExterna ce = getDocAlunoDocumentosDigitalizadosService.consultar(alunoConsultaVO);
		if(ce == null) {
			return null;
		}

		StatusConsultaExterna status = ce.getStatus();
		if(StatusConsultaExterna.ERRO.equals(status)) {
			String mensagem = ce.getMensagem();
			String method = DummyUtils.getCurrentMethodName();
			String stackTrace = ce.getStackTrace();
			emailSmtpService.enviarEmailException(method, stackTrace, mensagem);
			return null;
		}

		String resultadoJson = ce.getResultado();

		ObjectMapper om = new ObjectMapper();
		return om.readValue(resultadoJson, AlunoDocumentosDigitalizadosList.class);
	}

	public ConsultaExterna consultarMatriculaGetDocAluno(String matricula) {
		AlunoConsultaVO alunoConsultaVO = new AlunoConsultaVO(TipoConsultaExterna.CONSULTA_MATRICULA_GETDOC_ALUNO);
		alunoConsultaVO.setMatricula(matricula);
		ConsultaExterna ce = getDocAlunoConsultaMatriculaService.consultar(alunoConsultaVO);
		return ce;
	}
}
