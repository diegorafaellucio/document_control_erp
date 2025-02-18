package net.wasys.getdoc.bean.cliente;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.ProcessoNovoBean;
import net.wasys.getdoc.domain.entity.CampoAbstract;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.TipoDocumentoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.CriacaoProcessoVO;
import net.wasys.getdoc.domain.vo.DigitalizacaoVO;
import net.wasys.getdoc.domain.vo.FileVO;

@ManagedBean
@ViewScoped
public class ProcessoClienteNovoBean extends ProcessoNovoBean {

	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private UsuarioService usuarioService;

	private String tipo;
	private DigitalizacaoVO digitalizacaoVO;
	private Map<TipoDocumento, DigitalizacaoVO> mapDigitalizacao = new LinkedHashMap<>();
	private List<FileVO> arquivos = new ArrayList<>();
	private String radioBox;

	protected void initBean() {

		List<PermissaoTP> list = Arrays.asList(PermissaoTP.CLIENTE);
		tiposProcessos = tipoProcessoService.findAtivos(list);
	}

	public void avancarPasso2() {
		Long tipoProcessoId = tipoProcesso.getId();
		redirect("/cliente/nova-requisicao/" + tipoProcessoId);
	}

	public void buscarPorContrato() {}

	public void salvar() {

		try {
			Set<CampoAbstract> valoresCampos = getValoresCampos();
			Usuario usuario = usuarioService.getUsuarioCliente();

			CriacaoProcessoVO vo = new CriacaoProcessoVO();
			vo.setAcao(AcaoProcesso.CRIACAO_CLIENTE);
			vo.setTipoProcesso(tipoProcesso);
			vo.setUsuario(usuario);
			vo.setValoresCampos(valoresCampos);
			vo.setDigitalizacao(mapDigitalizacao);

			Set<TipoDocumento> keySet = mapDigitalizacao.keySet();
			for (TipoDocumento tipoDocumento : keySet) {
				DigitalizacaoVO digitalizacaoVO2 = mapDigitalizacao.get(tipoDocumento);
				if (digitalizacaoVO2.getArquivos().isEmpty()) {
					addMessageWarn("arquivosUploadObrigatorios.label");
					return;
				}
			}

			Processo processo = processoService.criaProcesso(vo);

			addMessage("processoCadastrado.sucesso");

			Long processoId = processo.getId();
			setFlashAttribute("processoId", processoId);
			redirect("/cliente/requisicoes/");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void selecionaTipoProcesso(ValueChangeEvent event) {	

		super.selecionaTipoProcesso(event);

		mapDigitalizacao.clear();

		if(tipoProcesso != null) {

			Long tipoProcessoId = tipoProcesso.getId();
			List<TipoDocumento> tiposDocumentos = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
			for (TipoDocumento tipoDocumento : tiposDocumentos) {

				boolean obrigatorio = tipoDocumento.getObrigatorio();
				if(obrigatorio) {

					DigitalizacaoVO vo = new DigitalizacaoVO();
					vo.setTipoDocumento(tipoDocumento);
					mapDigitalizacao.put(tipoDocumento, vo);
				}
			}
		}
	}

	public List<TipoCampoGrupo> getGrupos() {
		if(tiposCampos == null) {
			return null;
		}
		return new ArrayList<>(tiposCampos.keySet());
	}

	public List<CampoAbstract> getCampos(TipoCampoGrupo grupo) {
		List<CampoAbstract> list = tiposCampos.get(grupo);
		return list;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public List<DigitalizacaoVO> getDocumentos() {
		Collection<DigitalizacaoVO> values = mapDigitalizacao.values();
		return new ArrayList<>(values);
	}

	public void criarDigitalizacao(DigitalizacaoVO digitalizacaoVO) {
		this.digitalizacaoVO = digitalizacaoVO;
		this.arquivos = new ArrayList<>();
	}

	public void removerAnexo(FileVO fileVO) {

		this.arquivos.remove(fileVO);

		File file = fileVO.getFile();
		DummyUtils.deleteFile(file);
	}

	public void salvarDigitalizacao() {

		try {
			digitalizacaoVO.setArquivos(arquivos);

			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void uploadAnexo(FileUploadEvent event) {

		if(arquivos.size() >= 5) {
			addMessageError("numeroMaximoArquivos.error");
			return;
		}

		UploadedFile uploadedFile = event.getFile();

		File tmpFile = getFile(uploadedFile);

		if(tmpFile != null) {

			String fileName = uploadedFile.getFileName();

			FileVO anexo = digitalizacaoVO.criarAnexo(fileName, tmpFile);
			arquivos.add(anexo);

			addMessage("arquivoCarregado.sucesso");
		}
	}

	private File getFile(UploadedFile uploadedFile) {

		try {

			File tmpFile = File.createTempFile("anexo-proc-", ".tmp");
			DummyUtils.deleteOnExitFile(tmpFile);

			InputStream is = uploadedFile.getInputStream();
			FileUtils.copyInputStreamToFile(is, tmpFile);

			return tmpFile;
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}

		return null;
	}

	public DigitalizacaoVO getDigitalizacaoVO() {
		return digitalizacaoVO;
	}

	public void setDigitalizacaoVO(DigitalizacaoVO digitalizacaoVO) {
		this.digitalizacaoVO = digitalizacaoVO;
	}

	public List<FileVO> getArquivos() {
		return arquivos;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getRadioBox() {
		return radioBox;
	}

	public void setRadioBox(String radioBox) {
		this.radioBox = radioBox;
	}
}
