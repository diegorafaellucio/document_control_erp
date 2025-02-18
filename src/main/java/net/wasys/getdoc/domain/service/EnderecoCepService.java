package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

import net.wasys.util.LogLevel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.EnderecoCep;
import net.wasys.getdoc.domain.repository.EnderecoCepRepository;
import net.wasys.util.correiows.AtendeClienteProxy;
import net.wasys.util.correiows.EnderecoERP;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EnderecoCepService {

	@Autowired private EnderecoCepRepository enderecoCepRepository;
	@Autowired private ResourceService resourceService;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdateWithoutFlush(EnderecoCep enderecoCep) {
		enderecoCepRepository.saveOrUpdateWithoutFlush(enderecoCep);
	}

	@Transactional(rollbackFor=Exception.class)
	public EnderecoCep getByCep(String cep) {

		if(StringUtils.isBlank(cep)) {
			return null;
		}

		cep = cep.replaceAll("[^\\d]", "");

		EnderecoCep enderecoCep = enderecoCepRepository.getByCep(cep);

		String logradouro = enderecoCep != null ? enderecoCep.getLogradouro() : null;
		if(enderecoCep == null || StringUtils.isBlank(logradouro)) {

			enderecoCep = buscaEnderecoCorreio(cep, enderecoCep);

			if(enderecoCep != null) {

				enderecoCepRepository.saveOrUpdate(enderecoCep);
			}
		}

		return enderecoCep;
	}

	public void buscaEnderecoCorreio(EnderecoCep enderecoCep) {

		String cep = enderecoCep.getCep();
		buscaEnderecoCorreio(cep, enderecoCep);
	}

	private EnderecoCep buscaEnderecoCorreio(String cep, EnderecoCep enderecoCep) {

		String endpoint = resourceService.getValue(ResourceService.CORREIO_ENDPOINT);

		AtendeClienteProxy proxy = new AtendeClienteProxy(endpoint);

		try {
			EnderecoERP enderecoERP = proxy.consultaCEP(cep);

			String bairro = enderecoERP.getBairro();
			String cidade = enderecoERP.getCidade();
			String complemento = enderecoERP.getComplemento();
			String complemento2 = enderecoERP.getComplemento2();
			complemento = StringUtils.isNotBlank(complemento) ? complemento : complemento2;
			String uf = enderecoERP.getUf();
			String logradouro = enderecoERP.getEnd();

			enderecoCep = enderecoCep != null ? enderecoCep : new EnderecoCep();
			enderecoCep.setBairro(bairro);
			enderecoCep.setCep(cep);
			enderecoCep.setCidade(cidade);
			enderecoCep.setComplemento(complemento);
			enderecoCep.setLogradouro(logradouro);
			enderecoCep.setUf(uf);
			enderecoCep.setDataAtualizacao(new Date());

			return enderecoCep;
		}
		catch (Exception e) {

			if("CEP NAO ENCONTRADO".equals(e.toString())) {
				systraceThread("CEP " + cep + " não encontrado.", LogLevel.ERROR);
			}
			else {
				systraceThread("Erro!! CEP " + cep + ": " + e.getMessage(), LogLevel.ERROR);
				e.printStackTrace();
			}
		}

		return null;
	}
}
