package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.RendimentoComposicaoFamiliarVO;
import net.wasys.getdoc.domain.vo.RendimentoMembroFamiliarVO;
import net.wasys.getdoc.domain.vo.SalarioMinimoVO;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static net.wasys.util.DummyUtils.*;
import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class CalculoRendaService {

	@Autowired private ProcessoLogService processoLogService;

	public void atualizarCalculos(RendimentoComposicaoFamiliarVO vo) {

		atualizarMedias(vo);
		atualizarTotais(vo);
		atualizarValidador(vo);
	}

	public List<SalarioMinimoVO> mapSalariosMinimos(List<RegistroValorVO> valoresBaseInternaSalarioMinimo) {
		return valoresBaseInternaSalarioMinimo.stream()
				.map(RegistroValorVO::getMapColunaRegistroValor)
				.map(this::criarSalarioMinimoVO)
				.sorted((o1, o2) -> DummyUtils.parseDate3(o2.getVigencia()).compareTo(DummyUtils.parseDate3(o1.getVigencia())))
				.collect(Collectors.toList());
	}

	private void atualizarValidador(RendimentoComposicaoFamiliarVO vo) {

		BigDecimal salarioMaximoPermitido = getValorMoeda(vo.getSalarioMaximoPermitido());
		vo.setValidador(false);

		if (salarioMaximoPermitido != null) {

			BigDecimal rendaPerCapta = getValorMoeda(vo.getRendaPerCapta());
			vo.setValidador(rendaPerCapta != null && rendaPerCapta.compareTo(salarioMaximoPermitido) <= 0);
		}
	}

	private void atualizarTotais(RendimentoComposicaoFamiliarVO vo) {

		BigDecimal rendaTotal = null;

		for (RendimentoMembroFamiliarVO rendimento : vo.getRendimentos()) {

			Optional<BigDecimal> media = Optional.ofNullable(getValorMoeda(rendimento.getMedia()));
			if (media.isPresent()) {
				rendaTotal = rendaTotal == null ? media.get() : rendaTotal.add(media.get());
			}
		}

		int qtdeMembrosFamiliares = vo.getRendimentos().size();

		String rendaTotalCurrency = formatCurrency(rendaTotal);
		vo.setRendaTotal(rendaTotalCurrency);

		BigDecimal rendaPerCapta = rendaTotal == null ? null : rendaTotal.divide(BigDecimal.valueOf(qtdeMembrosFamiliares), RoundingMode.HALF_EVEN);

		String rendaPerCaptaCurrency = formatCurrency(rendaPerCapta);
		vo.setRendaPerCapta(rendaPerCaptaCurrency);

		String salarioMinimoMensal = vo.getSalarioMinimoMensal();
		BigDecimal salarioMinimoBD = getValorMoeda(salarioMinimoMensal);
		if (salarioMinimoBD != null && salarioMinimoBD.compareTo(BigDecimal.ZERO) > 0) {

			if (rendaPerCapta != null) {
				BigDecimal qtdeSalariosMinimos = rendaPerCapta.divide(salarioMinimoBD, 2, RoundingMode.HALF_EVEN);
				vo.setQtdeSalariosMinimos(qtdeSalariosMinimos.toString());
			}
			else {
				vo.setQtdeSalariosMinimos(null);
			}
		}
	}

	private void atualizarMedias(RendimentoComposicaoFamiliarVO vo) {

		List<BigDecimal> mediasHoleries = new ArrayList<>();

		for (RendimentoMembroFamiliarVO rendimento : vo.getRendimentos()) {

			Optional<BigDecimal> rendimento1 = Optional.ofNullable(getValorMoeda(rendimento.getRendimento1()));
			Optional<BigDecimal> rendimento2 = Optional.ofNullable(getValorMoeda(rendimento.getRendimento2()));
			Optional<BigDecimal> rendimento3 = Optional.ofNullable(getValorMoeda(rendimento.getRendimento3()));
			Optional<BigDecimal> rendimento4 = Optional.ofNullable(getValorMoeda(rendimento.getRendimento4()));
			Optional<BigDecimal> rendimento5 = Optional.ofNullable(getValorMoeda(rendimento.getRendimento5()));
			Optional<BigDecimal> rendimento6 = Optional.ofNullable(getValorMoeda(rendimento.getRendimento6()));
			List<Optional<BigDecimal>> optionals = Arrays.asList(rendimento1, rendimento2, rendimento3, rendimento4, rendimento5, rendimento6);
			List<BigDecimal> valoresRendimentos = optionals.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

			BigDecimal media = calcularMedia(valoresRendimentos);
			mediasHoleries.add(media);

			String mediaCurrency = formatCurrency(media);

			rendimento.setMedia(mediaCurrency);
		}
	}

	private SalarioMinimoVO criarSalarioMinimoVO(Map<String, BaseRegistroValor> valorParaColuna) {

		String vigencia = valorParaColuna.get(BaseRegistro.SALARIO_MINIMO_VIGENCIA).getValor();
		String multiplicador = valorParaColuna.get(BaseRegistro.SALARIO_MINIMO_MULTIPLICADOR).getValor();
		String valorMensal = valorParaColuna.get(BaseRegistro.SALARIO_MINIMO_VALOR_MENSAL).getValor();
		valorMensal = valorMensal.replace(",", "").replace(".", ",").replace("R$", "").trim();

		BigDecimal salarioMensalBD = DummyUtils.getValorMoeda(valorMensal);

		String salarioMaximoCurrency = null;
		if (salarioMensalBD != null && isNotBlank(multiplicador)) {

			double multiplicadorDouble = Double.parseDouble(multiplicador);
			BigDecimal multiplicadorBD = BigDecimal.valueOf(multiplicadorDouble);

			BigDecimal salarioMaximoBD = salarioMensalBD.multiply(multiplicadorBD);
			salarioMaximoCurrency = formatCurrency(salarioMaximoBD);
		}

		return new SalarioMinimoVO(vigencia, multiplicador, valorMensal, salarioMaximoCurrency);
	}

	public RendimentoComposicaoFamiliarVO findUltimoCalculo(Long processoId) {

		ProcessoLog logUltimoCalculo = processoLogService.findLastLogByProcessoAndAcao(processoId, AcaoProcesso.CALCULO_SALARIO_MINIMO);

		if (logUltimoCalculo != null) {

			String observacao = logUltimoCalculo.getObservacao();
			if (isNotBlank(observacao)) {
				try {
					return jsonToObject(observacao, RendimentoComposicaoFamiliarVO.class);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private BigDecimal calcularMedia(List<BigDecimal> valores) {

		int qntNull = 0;

		BigDecimal soma = BigDecimal.ZERO;
		for (BigDecimal valor : valores) {
			if (valor != null) {
				soma = soma.add(valor);
			}
			else {
				qntNull++;
			}
		}

		int divisor = valores.size() - qntNull;
		return soma.compareTo(BigDecimal.ZERO) > 0 ? soma.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_EVEN) : null;
	}
}