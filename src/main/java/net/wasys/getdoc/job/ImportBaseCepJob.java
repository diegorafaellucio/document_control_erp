package net.wasys.getdoc.job;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import net.wasys.getdoc.domain.entity.EnderecoCep;
import net.wasys.getdoc.domain.service.EnderecoCepService;

import static net.wasys.util.DummyUtils.systraceThread;

@Controller
public class ImportBaseCepJob {

	@Autowired private ApplicationContext applicationContext;

	//@Scheduled(cron="0 51 12 * * ?")
	public void execute() {

		String arquivoCSV = "C:\\Users\\Daniel\\Desktop\\fileshare.ro_CEPs.csv";
		BufferedReader br = null;
		String linha = "";
		String csvDivisor = ";";

		int cepSize = 0;
		int tipoLogradouroSize = 0;
		int logradouroSize = 0;
		int complementoSize = 0;
		int localSize = 0;
		int bairroSize = 0;
		int cidadeSize = 0;
		int ufSize = 0;
		int estadoSize = 0;
		int count = 0;
		List<EnderecoCep> list = new ArrayList<>(1000);

		try {

			long inicio = System.currentTimeMillis();
			FileInputStream fis = new FileInputStream(arquivoCSV);
			InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
			br = new BufferedReader(isr);
			while ((linha = br.readLine()) != null) {

				if(count == 0) {
					count++;
					continue;
				}

				String[] campos = linha.split(csvDivisor);
				for (int i = 0; i < campos.length; i++) {

					String campo = campos[i];
					campo = StringUtils.trim(campo);
					campo = campo.replaceFirst("^\"", "");
					campo = campo.replaceFirst("\"$", "");
					campo = campo.replace("#", ";");
					campo = StringUtils.trim(campo);
					campos[i] = campo;
				}

				String cep = campos[0];
				String tipoLogradouro = campos[1];
				String logradouro = campos[2];
				String complemento = campos[3];
				String local = campos[4];
				String bairro = campos[5];
				String cidade = campos[6];
				String uf = campos[7];
				String estado = campos[8];

				while(cep.length() < 8) {
					cep = "0" + cep;
				}

				cepSize = Math.max(cepSize, cep.length());
				tipoLogradouroSize = Math.max(tipoLogradouroSize, tipoLogradouro.length());
				logradouroSize = Math.max(logradouroSize, logradouro.length());
				complementoSize = Math.max(complementoSize, complemento.length());
				localSize = Math.max(localSize, local.length());
				bairroSize = Math.max(bairroSize, bairro.length());
				cidadeSize = Math.max(cidadeSize, cidade.length());
				ufSize = Math.max(ufSize, uf.length());
				estadoSize = Math.max(estadoSize, estado.length());

				EnderecoCep enderecoCep = new EnderecoCep();
				enderecoCep.setCep(cep);
				enderecoCep.setBairro(bairro);
				enderecoCep.setCidade(cidade);
				enderecoCep.setComplemento(complemento);
				enderecoCep.setLocal(local);
				enderecoCep.setLogradouro(logradouro);
				enderecoCep.setTipoLogradouro(tipoLogradouro);
				enderecoCep.setUf(uf);

				list.add(enderecoCep);

				if(count % 1000 == 0) {

					ImportBaseCepJob2 importBaseCepJob2 = applicationContext.getBean(ImportBaseCepJob2.class);
					importBaseCepJob2.setEnderecos(list);
					importBaseCepJob2.run();

					list.clear();

					long tempoMil = System.currentTimeMillis() - inicio;
					inicio = System.currentTimeMillis();
					systraceThread(count + ". " + tempoMil + "ms.");
				}

				count++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {

			systraceThread("cepSize = " + cepSize);
			systraceThread("tipoLogradouroSize = " + tipoLogradouroSize);
			systraceThread("logradouroSize = " + logradouroSize);
			systraceThread("complementoSize = " + complementoSize);
			systraceThread("localSize = " + localSize);
			systraceThread("bairroSize = " + bairroSize);
			systraceThread("cidadeSize = " + cidadeSize);
			systraceThread("ufSize = " + ufSize);
			systraceThread("estadoSize = " + estadoSize);
			systraceThread("count = " + count);

			systraceThread(String.valueOf(new Date()));

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Controller
	@Scope("prototype")
	public static class ImportBaseCepJob2 extends TransactionWrapperJob {

		@Autowired private EnderecoCepService enderecoCepService;
		@Autowired private SessionFactory sessionFactory;

		private List<EnderecoCep> list;

		@Override
		public void execute() throws Exception {

			for (EnderecoCep enderecoCep : list) {
				enderecoCepService.saveOrUpdateWithoutFlush(enderecoCep);
			}

			Session currentSession = sessionFactory.getCurrentSession();
			currentSession.flush();
		}

		public void setEnderecos(List<EnderecoCep> list) {
			this.list = list;
		}
	}
}