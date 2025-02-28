﻿/// <reference path="../Resources/dynamsoft.webtwain.intellisense.js" />

var _iLeft, _iTop, _iRight, _iBottom; //These variables are used to remember the selected area

function pageonload() {
	HideLoadImageForLinux();
	//InitMessageBody();
	initMessageBox(false);  //Messagebox
	InitBtnGroupBtm(false);
	//InitDWTdivMsg(false);
	initCustomScan();       //CustomScan

	var twainsource = document.getElementById("source");
	if (twainsource) {
		twainsource.options.length = 0;
		twainsource.options.add(new Option("Looking for devices.Please wait.", 0));
		twainsource.options[0].selected = true;
	}

	initiateInputs();
}

function HideLoadImageForLinux()
{
	var o = document.getElementById("liLoadImage");
	if(o){
		if (Dynamsoft.Lib.env.bLinux)
			o.style.display = "none";
		else
			o.style.display = "";
	}
}

function InitMessageBody() {

	var MessageBody = document.getElementById("divNoteMessage");
	if (MessageBody) {
		var ObjString = "<div><p>Platform & Browser Support: </p>Internet Explorer 8 or above (32 bit/64 bit), any version of Chrome (32 bit/64 bit), any version of Firefox on Windows; Safari, Chrome and Firefox on Mac OS X 10.6 or later; Chrome and Firefox v27 or above (64 bit) on Ubuntu 10-18, Debian 8-9, or Fedora 19+";
		ObjString += ".</div>";

		MessageBody.style.display = "";
		MessageBody.innerHTML = ObjString;
	}
}
function InitBtnGroupBtm(bNeebBack) {

	var btnGroupBtm = document.getElementById("btnGroupBtm");
	if (btnGroupBtm) {
		var objString = "";
		objString += "<div class='ct-lt'>Page: ";
		objString += "<input id='DW_btnFirstImage' onclick='btnFirstImage_onclick()' type='button' value=' |&lt; '/>&nbsp;";
		objString += "<input id='DW_btnPreImage' onclick='btnPreImage_onclick()' type='button' value=' &lt; '/>&nbsp;&nbsp;";
		objString += "<input type='text' size='2' id='DW_CurrentImage' readonly='readonly'/> / ";
		objString += "<input type='text' size='2' id='DW_TotalImage' readonly='readonly'/>&nbsp;&nbsp;";
		objString += "<input id='DW_btnNextImage' onclick='btnNextImage_onclick()' type='button' value=' &gt; '/>&nbsp;";
		objString += "<input id='DW_btnLastImage' onclick='btnLastImage_onclick()' type='button' value=' &gt;| '/></div>";
		objString += "<div class='ct-rt'>Preview Mode: ";
		objString += "<select size='1' id='DW_PreviewMode' onchange ='setlPreviewMode();'>";
		objString += "    <option value='0'>1X1</option>";
		objString += "</select><br /></div>";
		if (bNeebBack) {
			objString += "<div class='removeImage'><input id='DW_btnRemoveCurrentImage' onclick='btnRemoveCurrentImage_onclick()' type='button' value='Remove Selected Images'/>";
			objString += "<input id='DW_btnRemoveAllImages' onclick='btnRemoveAllImages_onclick()' type='button' value='Remove All Images'/></div>";
		}

		// btnGroupBtm.style.display = "";
		btnGroupBtm.innerHTML = objString;

		// Fill the init data for preview mode selection
		var varPreviewMode = document.getElementById("DW_PreviewMode");
		varPreviewMode.options.length = 0;
		varPreviewMode.options.add(new Option("1X1", 0));
		varPreviewMode.options.add(new Option("2X2", 1));
		varPreviewMode.options.add(new Option("3X3", 2));
		varPreviewMode.options.add(new Option("4X4", 3));
		varPreviewMode.options.add(new Option("5X5", 4));
		varPreviewMode.selectedIndex = 0;

	}
}
function InitDWTdivMsg(bNeebBack) {
	var DWTemessageContainer = document.getElementById("DWTemessageContainer");
	if (DWTemessageContainer) {
		var objString = "";
		// The container for the error message
		if (bNeebBack) {
			objString += "<p class='backToDemoList'><a class='d-btn bgOrange' href =\"online_demo_list.aspx\">Back</a></p>";
		}
		objString += "<div id='DWTdivMsg' class='clearfix'>";
		objString += "Message:<br/>"
		objString += "<div id='DWTemessage'>";
		objString += "</div></div>";

		var DWTemessageContainer = document.getElementById("DWTemessageContainer");
		DWTemessageContainer.innerHTML = objString;

		var _divMessageContainer = document.getElementById("DWTemessage");
		_divMessageContainer.ondblclick = function() {
			this.innerHTML = "";
			_strTempStr = "";
		}
	}

}

// split this function
function initMessageBox(bNeebBack) {
	var objString = "";

	// The container for navigator, view mode and remove button
	objString += "<div style='text-align:center; width:400px; background-color:#FFFFFF;display:block'>";
	objString += "<span id='DW_btnFirstImage' onclick='btnFirstImage_onclick()' type='button' class='icone-comandos btn btn-default edit-icon'><i class=\"fa fa-fast-backward\" aria-hidden=\"true\"></i></span>&nbsp;";
	objString += "<span id='DW_btnPreImage' onclick='btnPreImage_onclick()' type='button' value=' &lt; ' class='icone-comandos btn btn-default edit-icon'><i class='fa fa-step-backward' aria-hidden='true'></i></span>&nbsp;&nbsp;";
	objString += "<input type='text' size='2' id='DW_CurrentImage' readonly='readonly' class='form-control botao'/>";
	objString += "<span style='font-size: 22px; vertical-align: middle; padding: 0 2px;'>/</span>";
	objString += "<input type='text' size='2' id='DW_TotalImage' readonly='readonly' class='form-control botao'/>&nbsp;&nbsp;";
	objString += "<span id='DW_btnNextImage' onclick='btnNextImage_onclick()' type='button' value=' &gt; ' class='icone-comandos btn btn-default edit-icon'><i class='fa fa-step-forward' aria-hidden='true'></i></span>&nbsp;";
	objString += "<span id='DW_btnLastImage' onclick='btnLastImage_onclick()' type='button' value=' &gt;| ' class='icone-comandos btn btn-default edit-icon'><i class='fa fa-fast-forward' aria-hidden='true'></i></span>";
	objString += "<select size='1' id='DW_PreviewMode' onchange ='setlPreviewMode();' class='form-control botao' style='margin-left: 10px;'>";
	objString += "    <option value='0'>1X1</option>";
	objString += "</select><br />";
	/*objString += "<div><input id='DW_btnRemoveCurrentImage' onclick='btnRemoveCurrentImage_onclick()' type='button' value='Remove Selected Images'/>";
	if (bNeebBack) {
		objString += "<input id='DW_btnRemoveAllImages' onclick='btnRemoveAllImages_onclick()' type='button' value='Remove All Images'/><br /><br />";
		objString += "<span style=\"font-size:larger\"><a href =\"online_demo_list.aspx\"><b>Back</b></a></span><br /></div>";
	}
	else {
		objString += "<input id='DW_btnRemoveAllImages' onclick='btnRemoveAllImages_onclick()' type='button' value='Remove All Images'/><br /></div>";
	}
	objString += "</div>";

	// The container for the error message
	objString += "<div id='DWTdivMsg' style='width:580px;display:inline;'>";
	objString += "Message:<br/>"
	objString += "<div id='DWTemessage' style='width:560px; padding:30px 0 0 3px; height:80px; margin-top:5px; overflow:auto; background-color:#ffffff; border:1px #303030; border-style:solid; text-align:left; position:relative' >";
	objString += "</div></div>";*/

	var DWTemessageContainer = document.getElementById("DWTemessageContainer");
	DWTemessageContainer.innerHTML = objString;

	// Fill the init data for preview mode selection
	var varPreviewMode = document.getElementById("DW_PreviewMode");
	varPreviewMode.options.length = 0;
	varPreviewMode.options.add(new Option("1X1", 0));
	varPreviewMode.options.add(new Option("2X2", 1));
	varPreviewMode.options.add(new Option("3X3", 2));
	varPreviewMode.options.add(new Option("4X4", 3));
	varPreviewMode.options.add(new Option("5X5", 4));
	varPreviewMode.selectedIndex = 0;

	var _divMessageContainer = document.getElementById("DWTemessage");
	_divMessageContainer.ondblclick = function() {
		this.innerHTML = "";
		_strTempStr = "";
	}

}



function initCustomScan() {
	var ObjString = "";
	var ObjString2 = "";
	//TODO ObjString += "<ul id='divTwainType'> ";
	//TODO ObjString += "<li>";
	//TODO ObjString += "<label id ='lblShowUI' for = 'ShowUI'><input type='checkbox' id='ShowUI' />Show UI&nbsp;</label>";
	//TODO ObjString += "<label for = 'ADF'><input type='checkbox' id='ADF' />AutoFeeder&nbsp;</label>";
	ObjString += "<td><label for='Duplex'>Escanear:</label></td><td><select id='Duplex' class='form-control'><option value='Duplex' selected>Frente e verso</option><option value='Frente'>Apenas frente</option></select></td>";
	ObjString2 += "<td><label for='EliminarBlank'>Eliminar em Branco:</label></td><td><select id='EliminarBlank' class='form-control'><option value='EliminarBlankS' selected>Sim</option><option value='EliminarBlankN'>Não</option></td>";
	//TODO ObjString += "<li>Pixel Type:";
	//TODO ObjString += "<label for='BW' style='margin-left:5px;'><input type='radio' id='BW' name='PixelType'/>B&amp;W </label>";
	//TODO ObjString += "<label for='Gray'><input type='radio' id='Gray' name='PixelType'/>Gray</label>";
	//TODO ObjString += "<label for='RGB'><input type='radio' id='RGB' name='PixelType'/>Color</label></li>";
	//TODO ObjString += "<li>";
	//TODO ObjString += "<span>Resolution:</span><select size='1' id='Resolution'><option value = ''></option></select></li>";
	//TODO ObjString += "</ul>";

	if (document.getElementById("divProductDetail"))
		document.getElementById("divProductDetail").innerHTML = ObjString;

	if (document.getElementById("divProductDetail2"))
		document.getElementById("divProductDetail2").innerHTML = ObjString2
		;

	var vResolution = document.getElementById("Resolution");
	if (vResolution) {
		vResolution.options.length = 0;
		vResolution.options.add(new Option("100", 100));
		vResolution.options.add(new Option("150", 150));
		vResolution.options.add(new Option("200", 200));
		vResolution.options.add(new Option("300", 300));

		vResolution.options[3].selected = true;
	}

}

function initiateInputs() {

	var allinputs = document.getElementsByTagName("input");
	for (var i = 0; i < allinputs.length; i++) {
		if (allinputs[i].type == "checkbox") {
			allinputs[i].checked = false;
		}
		else if (allinputs[i].type == "text") {
			allinputs[i].value = "";
		}
	}


	if (Dynamsoft.Lib.env.bIE == true && Dynamsoft.Lib.env.bWin64 == true) {
		var o = document.getElementById("samplesource64bit");
		if(o)
			o.style.display = "inline";

		o = document.getElementById("samplesource32bit");
		if(o)
			o.style.display = "none";
	}
}



function initDllForChangeImageSize() {

	var vInterpolationMethod = document.getElementById("InterpolationMethod");
	vInterpolationMethod.options.length = 0;
	vInterpolationMethod.options.add(new Option("NearestNeighbor", 1));
	vInterpolationMethod.options.add(new Option("Bilinear", 2));
	vInterpolationMethod.options.add(new Option("Bicubic", 3));

}

function setDefaultValue() {
	var vGray = document.getElementById("Gray");
	if(vGray)
		vGray.checked = true;

	var varImgTypepng2 = document.getElementById("imgTypepng2");
	if (varImgTypepng2)
		varImgTypepng2.checked = true;
	var varImgTypepng = document.getElementById("imgTypepng");
	if (varImgTypepng)
		varImgTypepng.checked = true;

	var _strDefaultSaveImageName = "WebTWAINImage";
	var _txtFileNameforSave = document.getElementById("txt_fileNameforSave");
	if(_txtFileNameforSave)
		_txtFileNameforSave.value = _strDefaultSaveImageName;

	var _txtFileName = document.getElementById("txt_fileName");
	if(_txtFileName)
		_txtFileName.value = _strDefaultSaveImageName;

	var _chkMultiPageTIFF_save = document.getElementById("MultiPageTIFF_save");
	if(_chkMultiPageTIFF_save)
		_chkMultiPageTIFF_save.disabled = true;
	var _chkMultiPagePDF_save = document.getElementById("MultiPagePDF_save");
	if(_chkMultiPagePDF_save)
		_chkMultiPagePDF_save.disabled = true;
	var _chkMultiPageTIFF = document.getElementById("MultiPageTIFF");
	if(_chkMultiPageTIFF)
		_chkMultiPageTIFF.disabled = true;
	var _chkMultiPagePDF = document.getElementById("MultiPagePDF");
	if(_chkMultiPagePDF)
		_chkMultiPagePDF.disabled = true;
}


var DWObject;            // The DWT Object
var DWTSourceCount = 0;

// Check if the control is fully loaded.
function Dynamsoft_OnReady() {

	var liNoScanner = document.getElementById("pNoScanner");
	// If the ErrorCode is 0, it means everything is fine for the control. It is fully loaded.
	DWObject = Dynamsoft.WebTwainEnv.GetWebTwain('dwtcontrolContainer');
	if (DWObject) {
		if (DWObject.ErrorCode == 0) {
			$('#DWTNonInstallContainerID').hide();

			DWObject.LogLevel = 0;
			DWObject.IfAllowLocalCache = true;
			DWObject.ImageCaptureDriverType = 4;
			setDefaultValue();

			DWObject.RegisterEvent("OnTopImageInTheViewChanged", Dynamsoft_OnTopImageInTheViewChanged);
			DWObject.RegisterEvent("OnMouseClick", Dynamsoft_OnMouseClick);

			var twainsource = document.getElementById("source");
			if (!twainsource)
			{
				twainsource = document.getElementById("webcamsource");
			}

			var vCount = DWObject.SourceCount;
			DWTSourceCount = vCount;

			if (twainsource) {
				twainsource.options.length = 0;
				for (var i = 0; i < vCount; i++) {
					twainsource.options.add(new Option(DWObject.GetSourceNameItems(i), i));
				}
			}

			// If source list need to be displayed, fill in the source items.
			if (vCount == 0) {
				if (liNoScanner) {
					if (Dynamsoft.Lib.env.bWin) {

						liNoScanner.style.display = "block";
						liNoScanner.style.textAlign = "center";
					}
					else
						liNoScanner.style.display = "none";
				}
			}

			if (vCount > 0) {
				source_onchange(false);
			}

			if (Dynamsoft.Lib.env.bWin)
				DWObject.MouseShape = false;

			var btnScan = document.getElementById("btnScan");
			if(btnScan)
			{
				if (vCount == 0)
					document.getElementById("btnScan").disabled = true;
				else {
					document.getElementById("btnScan").disabled = false;
					document.getElementById("btnScan").style.color = "#fff";
					document.getElementById("btnScan").style.backgroundColor = "#50a8e1";
					document.getElementById("btnScan").style.cursor = "pointer";
				}
			}

			if (!Dynamsoft.Lib.env.bWin && vCount > 0) {
				if (document.getElementById("lblShowUI"))
					document.getElementById("lblShowUI").style.display = "none";
				if (document.getElementById("ShowUI"))
					document.getElementById("ShowUI").style.display = "none";
			}
			else {
				if(document.getElementById("lblShowUI"))
					document.getElementById("lblShowUI").style.display = "";
				if (document.getElementById("ShowUI"))
					document.getElementById("ShowUI").style.display = "";
			}

			try {
				initDllForChangeImageSize();
			}
			catch (e) {
				console.log(e);
			}

			if(document.getElementById("ddl_barcodeFormat"))
			{
				for (var index = 0; index < BarcodeInfo.length; index ++)
					document.getElementById("ddl_barcodeFormat").options.add(new Option(BarcodeInfo[index].desc, index));
				document.getElementById("ddl_barcodeFormat").selectedIndex = 0;
			}

			re = /^\d+$/;
			strre = /^[\s\w]+$/;
			refloat = /^\d+\.*\d*$/i;

			_iLeft = 0;
			_iTop = 0;
			_iRight = 0;
			_iBottom = 0;

			for (var i = 0; i < document.links.length; i++) {
				if (document.links[i].className == "ShowtblLoadImage") {
					document.links[i].onclick = showtblLoadImage_onclick;
				}
				if (document.links[i].className == "ClosetblLoadImage") {
					document.links[i].onclick = closetblLoadImage_onclick;
				}
			}
			if (vCount == 0) {
				if (Dynamsoft.Lib.env.bWin) {

					if (document.getElementById("aNoScanner") && window['bDWTOnlineDemo'])
					{
						if(document.getElementById("div_ScanImage").style.display == "")
							showtblLoadImage_onclick();
					}
					if(document.getElementById("Resolution"))
						document.getElementById("Resolution").style.display = "none";

				}

			}
			else
			{
				var divBlank = document.getElementById("divBlank");
				if(divBlank)
					divBlank.style.display = "none";
			}

			updatePageInfo();
			ua = (navigator.userAgent.toLowerCase());
			if (!ua.indexOf("msie 6.0")) {
				ShowSiteTour();
			}

			DWObject.RegisterEvent("OnPostTransfer", Dynamsoft_OnPostTransfer);
			DWObject.RegisterEvent("OnPostLoad", Dynamsoft_OnPostLoadfunction);
			DWObject.RegisterEvent("OnPostAllTransfers", Dynamsoft_OnPostAllTransfers);
			DWObject.RegisterEvent("OnImageAreaSelected", Dynamsoft_OnImageAreaSelected);
			DWObject.RegisterEvent("OnImageAreaDeSelected", Dynamsoft_OnImageAreaDeselected);
			DWObject.RegisterEvent("OnGetFilePath", Dynamsoft_OnGetFilePath);
			DWObject.RegisterEvent('OnMouseDoubleClick', onDoubleClick);

			var bAddInstallRecord = false;
			if (document.cookie.match("InstallDialog") && document.cookie.split("InstallDialog")[1].split("=")[1].split(";")[0] == "1") {
				bAddInstallRecord = true;
				delCookieForAddInstallRecord();
			}
			if (bAddInstallRecord == false) {
				if (window["localStorage"] && window["localStorage"]["InstallDialog"] == 1) {
					bAddInstallRecord = true;
					delCookieForAddInstallRecord();
				}
			}
		}
	}

	if (typeof (window['start_init_dcs']) == 'function')
	{
		window['start_init_dcs']();
	}
}


function delCookieForAddInstallRecord() {
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	document.cookie = "InstallDialog=0;expires=" + exp.toGMTString();
	window["localStorage"]["InstallDialog"] = 0;

	AddInstallRecord(false);
}


function showtblLoadImage_onclick() {
	switch (document.getElementById("tblLoadImage").style.visibility) {
		case "hidden": document.getElementById("tblLoadImage").style.visibility = "visible";
			document.getElementById("Resolution").style.visibility = "hidden";
			break;
		case "visible":
			document.getElementById("tblLoadImage").style.visibility = "hidden";
			document.getElementById("Resolution").style.visibility = "visible";
			break;
		default: break;
	}
	if(document.getElementById("pNoScanner"))
	{
		//document.getElementById("tblLoadImage").style.top = ds_gettop(document.getElementById("pNoScanner")) + pNoScanner.offsetHeight + "px";
		//document.getElementById("tblLoadImage").style.left = ds_getleft(document.getElementById("pNoScanner")) + 0 + "px";
	}
	return false;
}

function closetblLoadImage_onclick() {
	document.getElementById("tblLoadImage").style.visibility = "hidden";
	document.getElementById("Resolution").style.visibility = "visible";
	return false;
}

//--------------------------------------------------------------------------------------
//************************** Used a lot *****************************
//--------------------------------------------------------------------------------------
function updatePageInfo() {
	if(document.getElementById("DW_TotalImage"))
		document.getElementById("DW_TotalImage").value = DWObject.HowManyImagesInBuffer;
	if(document.getElementById("DW_CurrentImage"))
		document.getElementById("DW_CurrentImage").value = DWObject.CurrentImageIndexInBuffer + 1;

	if($('#EliminarBlank').val() == "EliminarBlankS")
	{
		DWObject.BlankImageMaxStdDev = 1;
		if (DWObject.IsBlankImageExpress(DWObject.CurrentImageIndexInBuffer)) {
			DWObject.RemoveImage(DWObject.CurrentImageIndexInBuffer);
		}
	}
}


var _strTempStr = "";       // Store the temp string for display
function appendMessage(strMessage) {
	_strTempStr += strMessage;
	var _divMessageContainer = document.getElementById("DWTemessage");
	if (_divMessageContainer) {
		_divMessageContainer.innerHTML = _strTempStr;
		_divMessageContainer.scrollTop = _divMessageContainer.scrollHeight;
	}
}

function checkIfImagesInBuffer() {
	if (DWObject.HowManyImagesInBuffer == 0) {
		appendMessage("There is no image in buffer.<br />")
		return false;
	}
	else
		return true;
}

function checkErrorString() {
	return checkErrorStringWithErrorCode(DWObject.ErrorCode, DWObject.ErrorString);
}

function checkErrorStringWithErrorCode(errorCode, errorString, responseString) {
	if (errorCode == 0) {
		appendMessage("<span style='color:#cE5E04'><b>" + errorString + "</b></span><br />");

		return true;
	}
	if (errorCode == -2115) //Cancel file dialog
		return true;
	else {
		if (errorCode == -2003) {
			var ErrorMessageWin = window.open("", "ErrorMessage", "height=500,width=750,top=0,left=0,toolbar=no,menubar=no,scrollbars=no, resizable=no,location=no, status=no");
			ErrorMessageWin.document.writeln(responseString); //DWObject.HTTPPostResponseString);
		}
		appendMessage("<span style='color:#cE5E04'><b>" + errorString + "</b></span><br />");
		return false;
	}
}


//--------------------------------------------------------------------------------------
//************************** Used a lot *****************************
//--------------------------------------------------------------------------------------
function ds_getleft(el) {
	var tmp = el.offsetLeft;
	el = el.offsetParent
	while (el) {
		tmp += el.offsetLeft;
		el = el.offsetParent;
	}
	return tmp;
}
function ds_gettop(el) {
	var tmp = el.offsetTop;
	el = el.offsetParent
	while (el) {
		tmp += el.offsetTop;
		el = el.offsetParent;
	}
	return tmp;
}

function Over_Out_DemoImage(obj, url) {
	obj.src = url;
}

function getImgs() {

	var imgs = '';

	for (var i = 0; i < DWObject.SelectedImagesCount; i++) {

		var idx = DWObject.GetSelectedImageIndex(i);
		if(idx >= 0) {
			imgs += (idx + 1) + ', ';
		}
	}

	return imgs.substring(0, imgs.lastIndexOf(','));
}

function getLastIndex() {

	var lastIndex = 0;

	for (var i = 0; i < DWObject.SelectedImagesCount; i++) {

		var idx = DWObject.GetSelectedImageIndex(i);
		lastIndex = idx > lastIndex ? idx : lastIndex;
	}

	return lastIndex;
}

function atualizarPreview() {

	var imgs = DWObject.HowManyImagesInBuffer;
	var op = 0;

	if(imgs > 16) {
		op = 5;
	} else if(imgs > 9) {
		op = 4;
	} else if(imgs > 4) {
		op = 3;
	} else if(imgs >= 2) {
		op = 2;
	} else {
		op = 1;
	}

	var varPreviewMode = document.getElementById("DW_PreviewMode");
	varPreviewMode.selectedIndex = op - 1;
	slPreviewMode();
}

function marcarImagens(docId) {

	var imgs = getImgs();

	if(imgs.length > 0) {

		var imgDoc = $('#img_doc_' + docId);
		var imgs2 = imgDoc.val();
		if(imgs2) {
			imgs = imgs2 + ', ' + imgs;
		}

		$('#docsSelecionados_' + docId).html(imgs);
		imgDoc.val(imgs);
		$('#desmarcarImagens_' + docId).show();

		lastIndex = getLastIndex();
		DWObject.CurrentImageIndexInBuffer = lastIndex + 1;
		updatePageInfo();
		atualizarPreview();
	}
	else {
		alert("Selecione as imagens que deseja vincular a este documento");
	}
}

function desmarcarImagens(docId) {

	$('#docsSelecionados_' + docId).html('');
	$('#img_doc_' + docId).val('');
	$('#desmarcarImagens_'+ docId).hide();
}

function slPreviewMode() {
	var varPreviewMode = document.getElementById("DW_PreviewMode");
	DWObject.SetViewMode(parseInt(varPreviewMode.selectedIndex + 1), parseInt(varPreviewMode.selectedIndex + 1));
}


function enviarImagens2(savePath, chave, btnConfirmacaoId, permiteTipificacao) {

	if (!checkIfImagesInBuffer()) {
		return;
	}
	var i, strHTTPServer, strActionPage, strImageType;

	//DWObject.MaxInternetTransferThreads = 5;
	strHTTPServer = location.hostname;
	DWObject.IfSSL = Dynamsoft.Lib.detect.ssl;
	var _strPort = location.port == "" ? 80 : location.port;
	if (Dynamsoft.Lib.detect.ssl == true)
		_strPort = location.port == "" ? 443 : location.port;
	DWObject.HTTPPort = _strPort;

	strActionPage = savePath + (chave ? "?chave=" + chave : ""); //the ActionPage's file path , Online Demo:"SaveToDB.aspx" ;Sample: "SaveToFile.aspx";

	strImageType = 1;//JPEG
	//strImageType = 2;//TIFF
	//strImageType = 3;//PNG
	//strImageType = 4;//PDF

	var uploadfilename = 'img-';

	var OnSuccess = function(httpResponse) {};

	var OnFailure = function(errorCode, errorString, httpResponse) {
		checkErrorStringWithErrorCode(errorCode, errorString, httpResponse);
	};

	if (strImageType == 2 && document.getElementById("MultiPageTIFF").checked) {
		if ((DWObject.SelectedImagesCount == 1) || (DWObject.SelectedImagesCount == DWObject.HowManyImagesInBuffer)) {
			DWObject.HTTPUploadAllThroughPostAsMultiPageTIFF(
				strHTTPServer,
				strActionPage,
				uploadfilename,
				OnSuccess, OnFailure
			);
		}
		else {
			DWObject.HTTPUploadThroughPostAsMultiPageTIFF(
				strHTTPServer,
				strActionPage,
				uploadfilename,
				OnSuccess, OnFailure
			);
		}
	}
	else if (strImageType == 4 && document.getElementById("MultiPagePDF").checked) {
		if ((DWObject.SelectedImagesCount == 1) || (DWObject.SelectedImagesCount == DWObject.HowManyImagesInBuffer)) {
			DWObject.HTTPUploadAllThroughPostAsPDF(
				strHTTPServer,
				strActionPage,
				uploadfilename,
				OnSuccess, OnFailure
			);
		}
		else {
			DWObject.HTTPUploadThroughPostAsMultiPagePDF(
				strHTTPServer,
				strActionPage,
				uploadfilename,
				OnSuccess, OnFailure
			);
		}
	}
	else {
		var percent = '';
		var index = 0;
		var totalImgs = DWObject.HowManyImagesInBuffer;

		var enviar = function(errorCode, errorString, httpResponse) {

			percent = index / totalImgs * 100;
			percent = "" + percent;
			var dotIndex = percent.lastIndexOf(".");
			if(dotIndex > 0) {
				percent = percent.substring(0, dotIndex);
			}

			appendMessage('<span>Enviando imagens... <b>' + percent + '%</b></span><br />');

			if(index < totalImgs) {
				DWObject.HTTPUploadThroughPostEx(
					strHTTPServer,
					index,
					strActionPage,
					uploadfilename + (index + 1),
					strImageType,
					enviar, OnFailure
				);

				index++;
			}
			else {
				appendMessage('<b>Enviando: </b>');
				checkErrorStringWithErrorCode(0, "Sucesso.");

				var btnConfirmacao = document.getElementById(btnConfirmacaoId);
				$(btnConfirmacao).click();

				closeModal('digitalizar-modal');

				exitFullscreen();
			}
		};

		var idx = '';
		$('.campoIdxImgs').each(function() {
			idx += ' ' + $(this).val() + ' ';
		});
		idx = idx.replace(/,/g, ' ');

		/*var idx2 = idx.replace(/ /g, '');
		if(!idx2) {
			$('.btn-associacao').css('border', '3px #cc092f solid');
			alert('Você precisa associar as imagens aos documentos.')
			return;
		}*/

		var idxPerdidos = '';
		for (i = 1; i <= totalImgs; i++) {
			if(idx.indexOf(' ' + i + ' ') == -1) {
				idxPerdidos += i + ', ';
			}
		}
		$('#img_doc_perdidos').val(idxPerdidos);

		idxPerdidos = idxPerdidos.replace(/, $/, '');
        if(idxPerdidos && permiteTipificacao == 'false') {
			if(confirm('A(s) imagem(ns) ' + idxPerdidos + ' não está(ão) associada(s) a nenhum documento. Enviar?')) {
			enviar();
			}
		}
		else {
			enviar();
		}
	}
}

function onDoubleClick() {

	var varPreviewMode = document.getElementById("DW_PreviewMode");
	if(varPreviewMode.selectedIndex == 0) {
		atualizarPreview();
	}
	else {

		var index = 0;
		for (var i = 0; i < DWObject.SelectedImagesCount; i++) {

			index = DWObject.GetSelectedImageIndex(i);
		}

		DWObject.SetViewMode(1, 1);
		varPreviewMode.selectedIndex = 0;

		DWObject.CurrentImageIndexInBuffer = index;
	}
}