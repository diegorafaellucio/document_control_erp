$.fn.animateRotate = function(angleFron, angleTo, duration, easing, complete) {
	var args = $.speed(duration, easing, complete);
	var step = args.step;
	return this.each(function(i, e) {
		args.step = function(now) {
			$.style(e, 'transform', 'rotate(' + now + 'deg)');
			if (step) return step.apply(this, arguments);
		};

		$({deg: angleFron}).animate({deg: angleTo}, args);
	});
};

jQuery(function($) {

	$( '.centered' ).each(function( e ) {
		$(this).css('margin-top',  ($('#main-slider').height() - $(this).height())/2);
	});

	$(window).resize(function(){
		$( '.centered' ).each(function( e ) {
			$(this).css('margin-top',  ($('#main-slider').height() - $(this).height())/2);
		});
	});

	//goto top
	$('.gototop').click(function(event) {
		event.preventDefault();
		$('html, body').animate({
			scrollTop: $("body").offset().top
		}, 500);
	});
});

function configurarCopiar(local) {
	var valorCampo = local ? $('.valorCampo', local) : $('.valorCampo');
	valorCampo = valorCampo.not('[campotipo="TEXTO_LONGO"]');
	valorCampo.click(function(){copiarParaAreaDeTransferencia(this)});
	valorCampo.attr('title', 'Clique para copiar');
	valorCampo.css('cursor', 'pointer');
}

function copiarParaAreaDeTransferencia(elemento) {

	elemento = $(elemento);
	var texto = elemento.html();
	copyTextToClipboard(texto, elemento.parent());

	elemento.html("<span class='copyAlert'>texto copiado!</span>");

	$('.copyAlert').show(300);
	setTimeout(function() {

		setTimeout(function() {
			$('.copyAlert').hide();
			elemento.html(texto);
		}, 100);
	}, 1000);
}

function copyTextToClipboard(text, local) {

	local = local ? local : $(document.body);

	var textArea = document.createElement("textarea");

	//
	// *** This styling is an extra step which is likely not required. ***
	//
	// Why is it here? To ensure:
	// 1. the element is able to have focus and selection.
	// 2. if element was to flash render it has minimal visual impact.
	// 3. less flakyness with selection and copying which **might** occur if
	//the textarea element is not visible.
	//
	// The likelihood is the element won't even render, not even a flash,
	// so some of these are just precautions. However in IE the element
	// is visible whilst the popup box asking the user for permission for
	// the web page to copy to the clipboard.
	//

	// Place in top-left corner of screen regardless of scroll position.
	textArea.style.position = 'fixed';
	textArea.style.top = 0;
	textArea.style.left = 0;

	// Ensure it has a small width and height. Setting to 1px / 1em
	// doesn't work as this gives a negative w/h on some browsers.
	textArea.style.width = '2em';
	textArea.style.height = '2em';

	// We don't need padding, reducing the size if it does flash render.
	textArea.style.padding = 0;

	// Clean up any borders.
	textArea.style.border = 'none';
	textArea.style.outline = 'none';
	textArea.style.boxShadow = 'none';

	// Avoid flash of white box if rendered for any reason.
	textArea.style.background = 'transparent';

	textArea.value = text;

	local.append(textArea);

	textArea.select();

	try {
		var successful = document.execCommand('copy');
		//var msg = successful ? 'successful' : 'unsuccessful';
		//console.log('Copying text command was ' + msg);
	}
	catch (err) {
		console.log('Oops, unable to copy');
	}

	$(textArea).remove();
}

function configurarMascaras() {

	$('.mask-date').mask('00/00/0000', {clearIfNotMatch: true});
	$('.mask-time').mask('00:00', {clearIfNotMatch: true});
	$('.mask-cep').mask('00000-000', {clearIfNotMatch: true});
	$('.mask-cpf').mask('000.000.000-00', {clearIfNotMatch: true});
	$('.mask-cnpj').mask('00.000.000/0000-00', {clearIfNotMatch: true});
	$('.mask-money').mask("###.###.###.###.###.###.##0,00", {reverse: true, clearIfNotMatch: true});
	$('.mask-percent').mask('##0,00%', {reverse: true, clearIfNotMatch: true});

	$('.mask-number').each(function() {

		var thiz = $(this);
		var maxlength = thiz.attr('maxlength');
		var maskNum = '';
		if(maxlength) {
			for (i = 0; i < maxlength; i++) {
				maskNum += '0';
			}
		} else {
			maskNum = '000000000000000';
		}

		thiz.mask(maskNum);
		thiz.removeAttr('maxlength');
	});

	$('.mask-placa').mask('ZZZ-0000', {
		clearIfNotMatch: true,
		translation: {
			'Z': {
				pattern: /[a-zA-Z]/
			}
		}
	});

	var phoneMaskBehavior = function (val) {
			return val.replace(/\D/g, '').length === 11 ? '(00) 00000-0000' : '(00) 0000-00009';
		},
		phoneOptions = {
			onKeyPress: function(val, e, field, options) {
				field.mask(phoneMaskBehavior.apply({}, arguments), options);
			},
			clearIfNotMatch: true
		};
	$('.mask-phone').mask(phoneMaskBehavior, phoneOptions);

	$('.mask-cpf-cnpj').each(function() {

		var val = $(this).val();
		var mask = val ? (val.length > 14 ? '00.000.000/0000-00' : '000.000.000-009') : '00.000.000/0000-00';

		$(this).mask(mask, {
			clearIfNotMatch: true,
			onKeyPress: keyPressCpfCnpj
		});
	});
}

function keyPressCpfCnpj(val, e, field, options) {

	if(val.length > 14) {
		field.mask('00.000.000/0000-00', {onKeyPress: keyPressCpfCnpj, clearIfNotMatch: true});
	} else {
		field.mask('000.000.000-009', {onKeyPress: keyPressCpfCnpj, clearIfNotMatch: true});
	}
}

function openModal(modalClass, btnFocusId) {

	$('.' + modalClass).modal();
	if(btnFocusId) {
		var btnFocus = document.getElementById(btnFocusId);
		if(btnFocus) {
			btnFocus.focus();
		}
	}
}

function closeModal(modalClass) {

	$('.' + modalClass).modal('hide');
}

var slideUp = false;

function prepareToSlideUp() {
	slideUp = true;
}

function slide(panelId, imgId){

	if(slideUp) {
		hideSlideUp(panelId, imgId);
	}
}

function hideSlideUp(panelId, imgId){

	var panel = $('#' + panelId);

	panel.slideUp(300);

	var img = $('#' + imgId);
	img.animateRotate(180, 360, 300);
}

function showSlideDown(panelId, imgId){

	$('#' + panelId).slideDown(300);

	var img = $('#' + imgId);
	img.animateRotate(360, 180, 300);
}

function configurarDatePicker() {

	var parent = $('.mask-date').parent();
	parent.addClass('date');
	parent.datepicker({
		todayHighlight: true,
		autoclose: true,
		inline: false,
		format: 'dd/mm/yyyy',
		language: 'pt-BR'
	});
}

function bloquearCampos(podeEditar) {

	if(!podeEditar) {

		$("input[type='text'],select", "#content-pg-id").attr('disabled', 'disabled');
	}
}

function hideAlertInfo() {
	$('.alert-info').slideUp(200);
}

document.documentElement.requestFullscreen = (function() {
	return document.documentElement.requestFullscreen
		|| document.documentElement.webkitRequestFullscreen
		|| document.documentElement.mozRequestFullscreen
		|| document.documentElement.mozRequestFullScreen
		|| document.documentElement.msRequestFullscreen
		|| document.documentElement.oRequestFullscreen
})();

function exitFullscreen() {

	if (document.exitFullscreen) {
		document.exitFullscreen();
	} else if (document.webkitExitFullscreen) {
		document.webkitExitFullscreen();
	} else if (document.mozCancelFullScreen) {
		document.mozCancelFullScreen();
	} else if (document.msExitFullscreen) {
		document.msExitFullscreen();
	}
}

function getById(id) {
	return document.getElementById(id);
}

function monitorDownload() {

	PrimeFaces.monitorDownload(
		function(){
			$('#ajaxLoaderImg').show()
		},
		function(){
			$('#ajaxLoaderImg').hide()
		}
	);
}

function hideMessage(timeout) {

	if($('div', '.bf-messages').length > 0) {
		setTimeout(hideAlertInfo, timeout);
	}
}

function preenchimentoAutomatico() {

	var data = OmniFaces.Ajax.data;
	var campos = JSON.parse(data.campos);
	var grupoId = data.grupoId;

	for (campoJson of campos) {
		var campo = JSON.parse(campoJson);
		$(campo.tipo+'[campoNome=\''+campo.nome+'\']', '.row-grupo-' + grupoId).val(campo.valor);
	}
}

var blob;
var whereCreateImage = 'img_id';
var whereRemover = 'spanColeAqui';
var uploadPrintFunc;

function clickPrint(divPrintClass, uploadPrintFunc2) {

	if($('.' + divPrintClass).length == 0) {

		$(".ui-fileupload-files").append(
			"<tr><td>" +
			"	<div class='" + divPrintClass + "' id='div_print'>" +
			"		<img src='' id='img_id_" + divPrintClass + "' style='max-width: 240px; max-height: 100px;'>" +
			"		<span id='spanColeAqui'>Cole Aqui o Print Screen!</span>" +
			"	</div>" +
			"</td></tr>"
		);

		whereCreateImage = 'img_id_' + divPrintClass;
		uploadPrintFunc = uploadPrintFunc2;

		// We start by checking if the browser supports the
		// Clipboard object. If not, we need to create a
		// contenteditable element that catches all pasted data
		if (!window.Clipboard) {

			var pasteCatcher = document.createElement("div");
			pasteCatcher.setAttribute("id", "paste_ff");

			// Firefox allows images to be pasted into contenteditable elements
			pasteCatcher.setAttribute("contenteditable", "");

			// We can hide the element and append it to the body,
			pasteCatcher.style.opacity = 0;
			pasteCatcher.style.height = 0;
			document.body.appendChild(pasteCatcher);

			// as long as we make sure it is always in focus
			pasteCatcher.focus();
			$('.' + divPrintClass).click(function() {
				pasteCatcher.focus();
				whereCreateImage = 'img_id_' + divPrintClass;
				whereRemover = 'spanColeAqui';
			});
		}

		// Add the paste event listener
		window.removeEventListener("paste", pasteHandler);
		window.addEventListener("paste", pasteHandler);
	}
}

/* Handle paste events */
function pasteHandler(e) {

	// We need to check if event.clipboardData is supported (Chrome)
	if (e.clipboardData) {
		// Get the items from the clipboard
		var items = e.clipboardData.items;

		if (items) {
			// Loop through all items, looking for any kind of image
			for (var i = 0; i < items.length; i++) {
				if (items[i].type.indexOf("image") !== -1) {
					// We need to represent the image as a file,
					blob = items[i].getAsFile();
					// and use a URL or webkitURL (whichever is available to the browser)
					// to create a temporary URL to the object
					var URLObj = window.URL || window.webkitURL;
					var source = URLObj.createObjectURL(blob);

					// The URL can then be used as the source of an image
					createImage(source);
				}
			}
		}
		// If we can't handle clipboard data directly (Firefox),
		// we need to read what was pasted from the contenteditable element
	} else {
		// This is a cheap trick to make sure we read the data
		// AFTER it has been inserted.
		setTimeout(checkInput, 1);
	}
}

/* Creates a new image from a given source */
function createImage(source) {
	var pastedImage = new Image();
	pastedImage.onload = function() {

		$('#arquivo').val(event.target.href);
	}
	pastedImage.src = source;

	document.getElementById(whereCreateImage).src = source;
	$('#' + whereRemover).remove();

	upload();
}

function upload() {

	var reader = new FileReader();
	// this function is triggered once a call to readAsDataURL returns
	reader.onload = function(event){

		uploadPrintFunc({arquivoPrint: event.target.result});
	};
	// trigger the read from the reader...
	reader.readAsDataURL(blob);
}



var waitingDialog = waitingDialog || (function ($) {
	'use strict';

	// Creating modal dialog's DOM
	var $dialog = $(
		'<div class="modal fade" data-backdrop="static" data-keyboard="false" tabindex="-1" role="dialog" aria-hidden="true" style="padding-top:15%; overflow-y:visible;">' +
		'<div class="modal-dialog modal-m">' +
		'<div class="modal-content">' +
		'<div class="modal-header"><h3 style="margin:0;"></h3></div>' +
		'<div class="modal-body">' +
		'<div class="progress progress-striped active" style="margin-bottom:0;"><div class="progress-bar" style="width: 100%"></div></div>' +
		'</div>' +
		'</div></div></div>');

	return {
		/**
		 * Opens our dialog
		 * @param message Custom message
		 * @param options Custom options:
		 * 				  options.dialogSize - bootstrap postfix for dialog size, e.g. "sm", "m";
		 * 				  options.progressType - bootstrap postfix for progress bar type, e.g. "success", "warning".
		 */
		show: function (message, options) {
			// Assigning defaults
			if (typeof options === 'undefined') {
				options = {};
			}
			if (typeof message === 'undefined') {
				message = 'Loading';
			}
			var settings = $.extend({
				dialogSize: 'm',
				progressType: '',
				onHide: null // This callback runs after the dialog was hidden
			}, options);

			// Configuring dialog
			$dialog.find('.modal-dialog').attr('class', 'modal-dialog').addClass('modal-' + settings.dialogSize);
			$dialog.find('.progress-bar').attr('class', 'progress-bar');
			if (settings.progressType) {
				$dialog.find('.progress-bar').addClass('progress-bar-' + settings.progressType);
			}
			$dialog.find('h3').text(message);
			// Adding callbacks
			if (typeof settings.onHide === 'function') {
				$dialog.off('hidden.bs.modal').on('hidden.bs.modal', function (e) {
					settings.onHide.call($dialog);
				});
			}
			// Opening dialog
			$dialog.modal();
		},
		/**
		 * Closes dialog
		 */
		hide: function () {
			$dialog.modal('hide');
		}
	};

})(jQuery);

function handleDicaCampo(campoId) {

	var tooltipDica = $('.tooltipDica-' + campoId);
	var parent = tooltipDica.parent();
	var title = tooltipDica.attr('title')
	if(title) {
		parent.attr('title', title);
		parent.tooltip({
			"content": title
		});
	}
}

function getCamposJson(form){

	var array = {};

	$('input,select,textarea', form).each(function() {
		var gruponome = $(this).attr('gruponome');
		var camponome = $(this).attr('camponome');
		if(gruponome && camponome) {
			var value = $(this).val();
			array[gruponome + '-' + camponome] = value;
		}
	});

	$("input[type='radio']:checked", form).each(function() {
		var gruponome = $(this).attr('gruponome');
		var camponome = $(this).attr('camponome');
		if(gruponome && camponome) {
			var value = $(this).val();
			array[gruponome + '-' + camponome] = value;
		}
	});

	return array;
}

function mostrarMaisTodosGrupos() {
	$('.hide-grupo-todos').show();
	$('.show-grupo-todos').hide();

	$("[class*='hide-grupo-']").show();
	$("[class*='show-grupo-']").hide();
	$('div', "[class*='row-g row-grupo-']").not('.modal').show();
}

function mostrarMenosTodosGrupos() {
	$('.hide-grupo-todos').hide();
	$('.show-grupo-todos').show();

	$("[class*='hide-grupo-']").hide();
	$("[class*='show-grupo-']").show();
	$('div', "[class*='row-g row-grupo-']").not('.modal').hide();
}


function mostrarMenosGrupo(id) {
	$('.hide-grupo-' + id).hide();
	$('.show-grupo-' + id).show();
	$('.campos-grupo-' + id).not('.modal').hide(300);
}

function mostrarMaisGrupo(id) {
	$('.hide-grupo-' + id).show();
	$('.show-grupo-' + id).hide();
	$('.campos-grupo-' + id).not('.modal').show(300);
}

function setRemoteIpCookie(saveRemoteIpCookie) {
	if (saveRemoteIpCookie) {
		if (!document.cookie.split(';').some((item) => item.trim().startsWith('remoteIp='))) {
			$.getJSON('https://api.ipify.org?format=json', function(data) {
				if (data != null && data.ip != null && data.ip !== 'undefined') {
					document.cookie = "remoteIp="+data.ip+"; path=/";
				}
			});
		}
	}
}

function setGeoLocationCookie(saveGeoLocationCookie, saveRemoteIpCookie) {
	if (saveGeoLocationCookie) {
		if (!document.cookie.split(';').some((item) => item.trim().startsWith('latitude='))) {
			if (navigator.geolocation) {
				navigator.geolocation.getCurrentPosition(geoLocation, alternativeGeoLocation);
			}

			function geoLocation(pos) {
				document.cookie = "latitude="+pos.coords.latitude+"; path=/";
				document.cookie = "longitude="+pos.coords.longitude+"; path=/";
			}

			function alternativeGeoLocation() {
				$.getJSON('http://gd.geobytes.com/GetCityDetails?callback=?', function(data) {
					if (saveRemoteIpCookie) {
						if (data != null && data.geobytesremoteip != null && data.geobytesremoteip !== 'undefined') {
							document.cookie = "remoteIp="+data.geobytesremoteip+"; path=/";
						}
					}
					document.cookie = "latitude="+data.geobyteslatitude+"; path=/";
					document.cookie = "longitude="+data.geobyteslongitude+"; path=/";
				});
			}
		}
	}
}

function handleDicaCampoCliente(dica, appendDica, parent) {

	var html = dica.html();
	if(html) {
		appendDica.html('<i class="fa fa-question-circle" aria-hidden="true"></i>');
		var parent = appendDica.parent();
		parent.attr('title', html);
		parent.tooltip({
			"content": html
		});
	}
	else {
		var parent2 = parent.parent();
		parent.remove();
		var html = parent2.html();
		parent2.parent().append(html);
		parent2.remove();
	}
}


PrimeFaces.locales['pt_BR'] = {
	closeText: 'Fechar',
	prevText: 'Anterior',
	nextText: 'Próximo',
	currentText: 'Começo',
	monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
	monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun', 'Jul','Ago','Set','Out','Nov','Dez'],
	dayNames: ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'],
	dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
	dayNamesMin: ['D','S','T','Q','Q','S','S'],
	weekHeader: 'Semana',
	firstDay: 1,
	isRTL: false,
	showMonthAfterYear: false,
	yearSuffix: '',
	timeOnlyTitle: 'Só Horas',
	timeText: 'Tempo',
	hourText: 'Hora',
	minuteText: 'Minuto',
	secondText: 'Segundo',
	currentText: 'Data Atual',
	ampm: false,
	month: 'Mês',
	week: 'Semana',
	day: 'Dia',
	allDayText : 'Todo Dia'
};

function atualizarFile(){
	waitingDialog.hide();
	$('#ajaxLoaderImg').hide()

	const botoes = document.getElementsByClassName("ui-fileupload-cancel");
	for (var i = 0; i <= botoes.length; i++) {
		if(botoes[i]) {
			botoes[i].click();
		}
	}

}

function preencherEndereco() {

	var data = OmniFaces.Ajax.data;
	var endereco = data.endereco;
	var grupoId = data.grupoId;

	$('input[campoNome=\'LOGRADOURO\']', '.row-grupo-' + grupoId).val(endereco.logradouroOk);
	$('input[campoNome=\'ENDEREÇO\']', '.row-grupo-' + grupoId).val(endereco.logradouroOk);
	$('input[campoNome=\'ENDERECO\']', '.row-grupo-' + grupoId).val(endereco.logradouroOk);
	$('input[campoNome=\'ENDEREÇO RESIDENCIAL\']', '.row-grupo-' + grupoId).val(endereco.logradouroOk);
	$('input[campoNome=\'ENDEREÇO COMERCIAL\']', '.row-grupo-' + grupoId).val(endereco.logradouroOk);
	$('input[campoNome=\'BAIRRO\']', '.row-grupo-' + grupoId).val(endereco.bairro);
	$('input[campoNome=\'CIDADE\']', '.row-grupo-' + grupoId).val(endereco.cidade);
	$('select[campoNome=\'UF\']', '.row-grupo-' + grupoId).val(endereco.uf);
	$('select[campoNome=\'ESTADO\']', '.row-grupo-' + grupoId).val(endereco.uf);
}
