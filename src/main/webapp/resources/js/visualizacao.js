
function selecionarDocumento(documentoId, section) {

	var mapa = documentosMap;
	var documentoVO = mapa[documentoId];
	var versaoAtual = documentoVO.versaoAtual;
	selecionarDocumento2(documentoId, section, versaoAtual);
}

function selecionarDocumento2(documentoId, section, versao) {

	var mapa = documentosMap;

	var documentoVO = mapa[documentoId];
	var versaoAtual = documentoVO.versaoAtual;

	$('#versaoSelect' + section).html('');

	for (i = 1 ; i <= versaoAtual ; i++) {
		$('#versaoSelect' + section).append($('<option>', {
			value: i,
			text : "Versão " + i
		}));
	}

	$('#documentoSelect' + section).val(documentoId);
	$('#versaoSelect' + section).val(versao);

	var imgs = documentoVO.imagens[versao];
	var imgsLength = imgs ? imgs.length : 0;

	var caminho = contextPath + "/resources/images/nao-digitalizado.png";
	var imagemId = 'null';
	var paginacao = '0 de ' + imgsLength;
	if(imgsLength > 0) {
		caminho = imgs[0] ? imgs[0].caminho : caminho;
		imagemId = imgs[0] ? imgs[0].id : '';
		paginacao = '1 de ' + imgsLength;

		var rotacao = imgs[0].rotacao;
		rotacao = rotacao ? rotacao : 0;
		$('#img' + section).css('transform', 'rotate(' + rotacao + 'deg)');
	}

	atualizarImagem('img' + section, caminho)

	$('#input-pagina' + section).val(paginacao);
	$('#img' + section).attr('id-img', imagemId);
	$('#img' + section).attr('id-doc', documentoId);

	if(section == 1) {
		idxPaginaAtual1 = 0;
	} else {
		idxPaginaAtual2 = 0;
	}

	if(section == 1) {

		$('.docs').removeClass('doc-selecionado');
		$('.boxIrregularidade').hide(300);
		$('#div-doc-' + documentoId).addClass('doc-selecionado');
		$('#boxIrregularidade-' + documentoId).show(300);
		$('#info-doc-1').html(documentoVO.nome);
		$('#info-doc-1').attr('title', documentoVO.nome);

		var aprovavel = versaoAtual == versao && documentoVO.aprovavel;
		var rejeitavel = versaoAtual == versao && documentoVO.rejeitavel;
		var podeCopiar = versaoAtual == versao && documentoVO.podeCopiar;
		var podeMover = versaoAtual == versao && documentoVO.podeMover;
		var podeExcluir = versaoAtual == versao && documentoVO.podeExcluir;
		var justificavel = versaoAtual == versao && documentoVO.justificavel;

		$('#btn-aprovar').css('display', aprovavel ? '' : 'none');
		$('#btn-justificar').css('display', justificavel ? '' : 'none');
		$('#btn-rejeitar').css('display', rejeitavel ? '' : 'none');
		$('.btn-cop-doc').css('display', podeCopiar ? '' : 'none');
		$('.btn-move-doc').css('display', podeMover ? '' : 'none');
		$('.btn-exclude').css('display', podeExcluir ? '' : 'none');
	}
}

function atualizarImagem(imgTagId, caminho) {

	$('#section-' + imgTagId).css('background-image', 'url(' + contextPath + '/resources/images/loader_black.gif)');
	var isPdf = caminho.endsWith(".pdf");

	var section = imgTagId.substring(imgTagId.length -1, imgTagId.length);
	var img = null;
	if(isPdf) {
		img = $('#' + imgTagId + 'Obj');
		img.parent().show();
		$('#' + imgTagId).parent().hide();
		$('#zoomIn' + section).hide();
		$('#reset' + section).hide();
		$('#zoomOut' + section).hide();
		$('#girarEsq' + section).hide();
		$('#girarDir' + section).hide();
		$('#salvarRotac' + section).hide();
	} else {
		img = $('#' + imgTagId);
		img.parent().show();
		$('#' + imgTagId + 'Obj').parent().hide();
		$('#zoomIn' + section).show();
		$('#reset' + section).show();
		$('#zoomOut' + section).show();
		$('#girarEsq' + section).show();
		$('#girarDir' + section).show();
		$('#salvarRotac' + section).show();
	}

	$(img).on('load error', function() {
		$('#section-' + imgTagId).css('background-image', '');
	});

	if(isPdf) {
		img.attr('data', '');
		img.attr('data', caminho);
		img.parent().removeClass('panzoom');
	}
	else {
		img.attr('src', '');
		img.attr('src', caminho);
		img.parent().addClass('panzoom');
	}
}

function paginaAnterior(section) {
	mudarPagina(false, section);
}

function proximaPagina(section) {
	mudarPagina(true, section);
}

function paginaAnterior(section) {
	mudarPagina(false, section);
}

function proximaPagina(section) {
	mudarPagina(true, section);
}

function mudarPagina(frente, section, prox) {

	var docAtual = $('#documentoSelect' + section).val();
	if(!docAtual) {
		return;
	}

	var verAtual = $('#versaoSelect' + section).val();

	var mapa = documentosMap;
	var documentoVO = mapa[docAtual];
	var imagensMap = documentoVO.imagens;
	var imgs = imagensMap[verAtual];
	var imgsLength = imgs.length;

	if(!prox) {
		if(section == 1) {
			prox = frente ? idxPaginaAtual1 + 1 : idxPaginaAtual1 - 1;
		} else {
			prox = frente ? idxPaginaAtual2 + 1 : idxPaginaAtual2 - 1;
		}
	}

	if(prox < imgsLength && prox >= 0) {

		atualizarImagem('img' + section, imgs[prox].caminho)

		var inputPg = $('#input-pagina' + section);
		inputPg.val((prox + 1) + ' de ' + imgsLength);
		$('#img' + section).attr('id-img', imgs[prox].id);
		$('#img' + section).attr('id-doc', docAtual);

		if(section == 1) {
			idxPaginaAtual1 = prox;
		} else {
			idxPaginaAtual2 = prox;
		}

		girar(null, section);
	}
}

function ocultarBarraLateral() {

	$('.coluna-esq').hide(300);
	$('.documentoSelect').show(300);

	$(".colina-dir-row").animate({'padding-left' : 0}, 400);
	$('.colina-dir-row').animate({'padding-left': '15px'}, 400);

	setTimeout("$('.btn-open-direita').show();", 300);

}

function mostrarBarraLateral() {

	$('.documentoSelect').hide(300);
	$(".colina-dir-row").animate({'padding-left' : '340px'}, 250);
	$('.coluna-esq').show(500);

	$('.btn-open-direita').hide(300);

	$('.colina-dir-row').css('padding-left', '0');
}

function girar(direita, section) {

	var verAtual = $('#versaoSelect' + section).val();

	var img = $('#img' + section);
	var imgId = img.attr('id-img');
	var docId = img.attr('id-doc');

	var mapa = documentosMap;
	var documentoVO = mapa[docId];
	var imagensMap = documentoVO.imagens;
	var imgs = imagensMap[verAtual];

	for(idx in imgs) {
		if(imgs[idx].id == imgId) {

			var rotacao = imgs[idx].rotacao;
			rotacao= rotacao ? rotacao : 0;

			if(direita != null) {
				rotacao = direita ? (rotacao + 90) : (rotacao - 90);
			}
			if(rotacao == 310 || rotacao == -310 ) {
				rotacao = 0;
			}
			if(rotacao < 0) {
				rotacao = 360 - (rotacao * -1);
			}

			img.css('transform', 'rotate(' + rotacao + 'deg)');
			imgs[idx].rotacao = rotacao;
		}
	}
}

function salvarRotacao(img) {

	var verAtual = $('#versaoSelect1').val();

	var img = $('#img1');
	var imgId = img.attr('id-img');
	var docId = img.attr('id-doc');

	var mapa = documentosMap;
	var documentoVO = mapa[docId];
	var imagensMap = documentoVO.imagens;
	var imgs = imagensMap[verAtual];

	for(idx in imgs) {

		if(imgs[idx].id == imgId) {

			var rotacao = imgs[idx].rotacao;
			rotacao = rotacao ? rotacao : 0;

			//aqui chama <o:commandScript name="salvarRotacao2" action="#{processoEditBean.salvarRotacao}"/> que está em processo-edit-visualizar.xhtml
			salvarRotacao2({ imagemId: imgId, rotacao: rotacao });
		}
	}
}

function aprovarDocumento(img) {

	var docId = img.attr('id-doc');
	var documentoAtual2 = $('#documentoSelect2').val();

	//aqui chama <o:commandScript name="aprovarDocumento2" action="#{processoEditBean.aprovarDocumento}"/> que está em processo-edit-visualizar.xhtml
	aprovarDocumento2({ docId: docId, doc2Id: documentoAtual2, doc2Index: idxPaginaAtual2 });
}

function reprocessarOcr(img) {

	var docId = img.attr('id-doc');
	//alert(docId);
	//aqui chama <o:commandScript name="reprocessarOcr2" action="#{processoEditBean.reprocessarOcr}"/> que está em processo-edit-visualizar.xhtml
	reprocessarOcr2({ docId: docId });
}

function rejeitarDocumento() {

	var docAtual = $('#documentoSelect1').val();
	var irregularidadeId = $('#irregularidadeId').val();

	//aqui chama <o:commandScript name="rejeitarDocumento2" action="#{processoEditBean.rejeitarDocumento}"/> que está em processo-edit-visualizar.xhtml
	rejeitarDocumento2({ docId: docAtual, irregularidadeId: irregularidadeId });
}

function justificarDocumento() {

	var docAtual = $('#documentoSelect1').val();

	//aqui chama <o:commandScript name="justificarDocumento2" action="#{processoEditBean.justificarDocumento}"/> que está em processo-edit-visualizar.xhtml
	justificarDocumento2({ docId: docAtual });
}

function abrirJustificar1(img) {

	var docId = img.attr('id-doc');
	var documentoVO = documentosMap[docId];
	var documentoNome = documentoVO.nome;

	abrirJustificar2(docId, documentoNome);
}

function abrirJustificar2(docId, documentoNome) {

	var modal = $('.justificar-modal');
	var campoDocId = $('#docId', modal);
	campoDocId.val(docId);
	var campoNomeDocumento = $('#nomeDocumento', modal);
	campoNomeDocumento.html(documentoNome);

	openModal('justificar-modal')
}

function copiarImagem(img, doc) {

	var imgId = img.attr('id-img');
	var docId = img.attr('id-doc');
	var destinoId = doc.val();
	var verAtual = $('#versaoSelect1').val();

	var mover = $('[name="mover"]', doc.parent().parent());

	var documentoVO = documentosMap[docId];
	var imagensMap = documentoVO.imagens;
	var imgs = imagensMap[verAtual];

	for(idx in imgs) {

		if(imgs[idx].id == imgId) {

			//aqui chama <o:commandScript name="copiarImagem2" action="#{processoEditBean.copiarImagem}" render="growl-id"/> que está em processo-edit-visualizar.xhtml
			copiarImagem2({ imagemId: imgId, destinoId: destinoId, indiceImagem: idxPaginaAtual1, mover: mover });
		}
	}
}

function excluirImagem(img) {

	var imgId = img.attr('id-img');
	var docId = img.attr('id-doc');
	var verAtual = $('#versaoSelect1').val();

	var mapa = documentosMap;
	var documentoVO = mapa[docId];
	var imagensMap = documentoVO.imagens;
	var imgs = imagensMap[verAtual];

	for(idx in imgs) {

		if(imgs[idx].id == imgId) {

			//aqui chama <o:commandScript name="excluirImagem2" action="#{processoEditBean.excluirImagem}" render="growl-id"/> que está em processo-edit-visualizar.xhtml
			excluirImagem2({ imagemId: imgId, indiceImagem: idxPaginaAtual1 });
		}
	}
}

function pararComparacao() {

	$(".coluna-dir1").animate({'width' : '100%'}, 300);
	$(".coluna-dir2").animate({'width' : '0%'}, 300);

	$('#movComp').hide(300);
	$('#pararComp').hide(300);
	$('#initComp').show(300);

	setTimeout("$('.coluna-dir2').css('display', 'none');", 300);
}

function iniciarComparacao(animar) {

	var widthIni = (($('.coluna-dir1').width()) / 2) + 'px';

	$('#movComp').show(300);
	$('#pararComp').show(300);
	$('#initComp').hide(300);

	$('.coluna-dir2').css('display', '');

	if(animar) {
		$(".coluna-dir1").animate({'width' : '50%'}, 300);
		$(".coluna-dir2").animate({'width' : '50%'}, 300);
	}
	else {
		$(".coluna-dir1").css('width', '50%');
		$(".coluna-dir2").css('width', '50%');
	}

	moverParaDireita();
}

function moverParaDireita() {

	var documentoId = $('#documentoSelect1').val();

	selecionarDocumento(documentoId, 2);
	mudarPagina(null, 2, idxPaginaAtual1);
}