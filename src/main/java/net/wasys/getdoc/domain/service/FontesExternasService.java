package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.entity.FonteExterna;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.repository.FonteExternaRepository;
import net.wasys.getdoc.domain.service.webservice.brscan.BrScanService;
import net.wasys.getdoc.domain.service.webservice.credilink.CredilinkService;
import net.wasys.getdoc.domain.service.webservice.crivo.CrivoService;
import net.wasys.getdoc.domain.service.webservice.detranarn.DetranArnOutrosEstadosService;
import net.wasys.getdoc.domain.service.webservice.detranarn.DetranArnSaoPauloService;
import net.wasys.getdoc.domain.service.webservice.infocar.DecodeChassiInfoCarService;
import net.wasys.getdoc.domain.service.webservice.infocar.LeilaoInfoCarService;
import net.wasys.getdoc.domain.service.webservice.serpro.datavalid.DataValidBiometriaService;
import net.wasys.getdoc.domain.service.webservice.serpro.datavalid.DataValidService;
import net.wasys.getdoc.domain.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Map;

@Service
public class FontesExternasService {

	@Autowired private LeilaoInfoCarService leilaoInfoCarService;
	@Autowired private DecodeChassiInfoCarService decodeChassiInfoCarService;
	@Autowired private DetranArnSaoPauloService detranArnSaoPauloService;
	@Autowired private DetranArnOutrosEstadosService detranArnOutrosEstadosService;
	@Autowired private CrivoService crivoService;
	@Autowired private CredilinkService credilinkService;
	//@Autowired private NfeInteresseService nfeInteresseService;
	@Autowired private DataValidService dataValidService;
	@Autowired private DataValidBiometriaService dataValidBiometriaService;
	//@Autowired private RenavamIndicadoresChassiService renavamIndicadoresChassiService;
	@Autowired private BrScanService brScanService;

	@Autowired private FonteExternaRepository fonteExternaRepository;
	
	public ConsultaExterna executarNovoLeilaoInfocar(String placa, Processo processo, Usuario usuario) {
		LeilaoInfoCarRequestVO vo = criaLeilaoInfocar(placa, processo, usuario);
		return leilaoInfoCarService.consultarInvalidandoConsultaAnterior(vo);
	}

	public ConsultaExterna executarLeilaoInfocar(String placa, Processo processo, Usuario usuario) {
		LeilaoInfoCarRequestVO vo = criaLeilaoInfocar(placa, processo, usuario);
		return leilaoInfoCarService.consultar(vo);
	}

	private LeilaoInfoCarRequestVO criaLeilaoInfocar(String placa, Processo processo, Usuario usuario) {
		LeilaoInfoCarRequestVO vo = new LeilaoInfoCarRequestVO();
		vo.setPlaca(placa);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return vo;
	}
	
	public ConsultaExterna executarNovoDecodeInfocar(/*String placa, */String chassi, Processo processo, Usuario usuario) {
		DecodeInfoCarRequestVO vo = criaDecodeInfocarVO(chassi, processo, usuario);
		return decodeChassiInfoCarService.consultarInvalidandoConsultaAnterior(vo);
	}

	public ConsultaExterna executarDecodeInforcar(/*String placa, */String chassi, Processo processo, Usuario usuario) {
		DecodeInfoCarRequestVO vo = criaDecodeInfocarVO(chassi, processo, usuario);
		return decodeChassiInfoCarService.consultar(vo);
	}

	private DecodeInfoCarRequestVO criaDecodeInfocarVO(String chassi, Processo processo, Usuario usuario) {
		DecodeInfoCarRequestVO vo = new DecodeInfoCarRequestVO();
		//vo.setPlaca(placa);
		vo.setChassi(chassi);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return vo;
	}

	public ConsultaExterna executarNovoDetranArn(String placa, String chassi, String uf, Processo processo, Usuario usuario) {
		DetranArnRequestVO vo = criaDetraArnVO(placa, chassi, uf, processo, usuario);
		return executarDetranArn(uf, vo, true);
	}

		public ConsultaExterna executarDetranArn(String placa, String chassi, String uf, Processo processo, Usuario usuario) {
		DetranArnRequestVO vo = criaDetraArnVO(placa, chassi, uf, processo, usuario);
		return executarDetranArn(uf, vo, false);
	}

	private ConsultaExterna executarDetranArn(String uf, DetranArnRequestVO vo, boolean novo) {
		if("SP".equals(uf)) {
			if(novo) {
				return detranArnSaoPauloService.consultarInvalidandoConsultaAnterior(vo);
			} else {
				return detranArnSaoPauloService.consultar(vo);
			}
		} else {
			if(novo) {
				return detranArnOutrosEstadosService.consultarInvalidandoConsultaAnterior(vo);
			} else {
				return detranArnOutrosEstadosService.consultar(vo);
			}
		}
	}

	private DetranArnRequestVO criaDetraArnVO(String placa, String chassi, String uf, Processo processo, Usuario usuario) {
		DetranArnRequestVO vo = new DetranArnRequestVO();
		vo.setPlaca(placa);
		vo.setChassi(chassi);
		vo.setUf(uf);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return vo;
	}

	public ConsultaExterna executarCrivo(Map<String, String> parametros, String politica, Processo processo, Usuario usuario) {
		CrivoConsultaVO vo = new CrivoConsultaVO();
		vo.setParametros(parametros);
		vo.setPolitica(politica);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return crivoService.consultar(vo);
	}

	public ConsultaExterna executarNovoCredilink(String cpfCnpj, String nome, String telefone, Processo processo, Usuario usuario) {
		CredilinkRequestVO vo = new CredilinkRequestVO();
		vo.setCpfCnpj(cpfCnpj);
		vo.setNome(nome);
		vo.setTelefone(telefone);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return credilinkService.consultarInvalidandoConsultaAnterior(vo);
	}

	public ConsultaExterna executarCredilink(String cpfCnpj, String nome, String telefone, Processo processo, Usuario usuario) {
		CredilinkRequestVO vo = new CredilinkRequestVO();
		vo.setCpfCnpj(cpfCnpj);
		vo.setNome(nome);
		vo.setTelefone(telefone);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return credilinkService.consultar(vo);
	}

	/*public ConsultaExterna executarNovoNfeInteresse(String chaveNfe, String ufAutor, Processo processo, Usuario usuario) {
		NfeInteresseRequestVO vo = new NfeInteresseRequestVO();
		vo.setChaveNfe(chaveNfe);
		vo.setUfAutor(ufAutor);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return nfeInteresseService.consultarInvalidandoConsultaAnterior(vo);
	}

	public ConsultaExterna executarNfeInteresse(String chaveNfe, String ufAutor, Processo processo, Usuario usuario) {
		NfeInteresseRequestVO vo = new NfeInteresseRequestVO();
		vo.setChaveNfe(chaveNfe);
		vo.setUfAutor(ufAutor);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return nfeInteresseService.consultar(vo);
	}*/

	public ConsultaExterna executarNovoDataValid(String cpf, Date dataValidadeCnh, String nome, Date dataNascimento, String nomeMae, Processo processo, Usuario usuario) {
		DataValidRequestVO vo = new DataValidRequestVO();
		vo.setCpf(cpf);
		vo.setDataValidadeCnh(dataValidadeCnh);
		vo.setNome(nome);
		vo.setDataNascimento(dataNascimento);
		vo.setNomeMae(nomeMae);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return dataValidService.consultarInvalidandoConsultaAnterior(vo);
	}

	public ConsultaExterna executarDataValid(String cpf, Date dataValidadeCnh, String nome, Date dataNascimento, String nomeMae, Processo processo, Usuario usuario) {
		DataValidRequestVO vo = new DataValidRequestVO();
		vo.setCpf(cpf);
		vo.setDataValidadeCnh(dataValidadeCnh);
		vo.setNome(nome);
		vo.setDataNascimento(dataNascimento);
		vo.setNomeMae(nomeMae);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		ConsultaExterna ce = dataValidService.consultar(vo);
		return ce;
	}

	public ConsultaExterna executarNovoDataValidBiometria(File foto, String cpf, Date dataValidadeCnh, String nomeFinanciado, Date dataNascimento, String nomeMaeFinanciado, Processo processo, Usuario usuario) {
		DataValidBiometriaRequestVO vo = new DataValidBiometriaRequestVO();
		vo.setFoto(foto);
		vo.setCpf(cpf);
		vo.setDataValidadeCnh(dataValidadeCnh);
		vo.setNome(nomeFinanciado);
		vo.setDataNascimento(dataNascimento);
		vo.setNomeMae(nomeMaeFinanciado);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return dataValidBiometriaService.consultarInvalidandoConsultaAnterior(vo);
	}

	public ConsultaExterna executarDataValidBiometria(File foto, String cpf, Date dataValidadeCnh, String nome, Date dataNascimento, String nomeMae, Processo processo, Usuario usuario) {
		DataValidBiometriaRequestVO vo = new DataValidBiometriaRequestVO();
		vo.setFoto(foto);
		vo.setCpf(cpf);
		vo.setDataValidadeCnh(dataValidadeCnh);
		vo.setNome(nome);
		vo.setDataNascimento(dataNascimento);
		vo.setNomeMae(nomeMae);
		vo.setProcesso(processo);
		vo.setUsuario(usuario);
		return dataValidBiometriaService.consultar(vo);
	}

//	public ConsultaExterna executarNovoRenavamIndicadoresChassi(String chassi, Processo processo, Usuario usuario) {
//		RenavamIndicadoresChassiRequestVO vo = new RenavamIndicadoresChassiRequestVO();
//		vo.setChassi(chassi);
//		vo.setProcesso(processo);
//		vo.setUsuario(usuario);
//		return renavamIndicadoresChassiService.consultarInvalidandoConsultaAnterior(vo);
//	}
//
//	public ConsultaExterna executarRenavamIndicadoresChassi(String chassi, Processo processo, Usuario usuario) {
//		RenavamIndicadoresChassiRequestVO vo = new RenavamIndicadoresChassiRequestVO();
//		vo.setChassi(chassi);
//		vo.setProcesso(processo);
//		vo.setUsuario(usuario);
//		return renavamIndicadoresChassiService.consultar(vo);
//	}

	public ConsultaExterna executarBrScan(String cpfCnpj, String documento, String selfie, Processo processo, Usuario usuario) {
		BrScanRequestVO vo = new BrScanRequestVO();
		vo.setCpf(cpfCnpj);
		vo.setDocumentoIdentificacao(documento);
		vo.setSelfie(selfie);
		return brScanService.consultar(vo);
	}

	public FonteExterna findByNome(TipoConsultaExterna tipoConsultaExterna) {
		return fonteExternaRepository.findByNome(tipoConsultaExterna);
	}
}
