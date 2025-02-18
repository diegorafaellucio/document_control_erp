package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.mb.enumerator.DeviceSO;
import net.wasys.getdoc.mb.model.LinkModel;
import net.wasys.getdoc.mb.model.PushModel;
import net.wasys.getdoc.mb.model.PushModel.Type;
import net.wasys.util.DummyUtils;

@Service
public class PushService {

	@Autowired private MessageService messageService;
	@Autowired private ResourceService resourceService;

	public void send(AcaoProcesso acao, Processo processo) {

		Usuario autor = processo.getAutor();
		if(autor == null) {
			return;
		}

		DeviceSO deviceSO = autor.getDeviceSO();
		String devicePushToken = autor.getDevicePushToken();
		if (deviceSO != null && StringUtils.isNotBlank(devicePushToken)) {
			String message = null;
			if (AcaoProcesso.ENVIO_PENDENCIA.equals(acao)) {
				message = messageService.getValue("requisicaoPendente.message");
			}
			if (message != null) {
				PushModel pushModel = new PushModel(Type.LINK, deviceSO, devicePushToken);
				pushModel.setTitle(messageService.getValue("processo.label"));
				pushModel.setMessage(message);
				LinkModel linkModel = new LinkModel();
				linkModel.name = pushModel.getTitle();
				linkModel.href = resourceService.getValue(ResourceService.PUSH_REQUISICAO_EDICAO_HREF, String.valueOf(processo.getId()));
				pushModel.setData(linkModel);
				send(pushModel);
			}
		}
	}

	public void send(PushModel model) {
		try {
			String password = resourceService.getValue(ResourceService.PUSH_IOS_CER_PWD);
			String pathname = resourceService.getValue(ResourceService.PUSH_IOS_CER_PATH);
			ApnsServiceBuilder apnsServiceBuilder = APNS.newService()
					.withCert(pathname, password);
			if (DummyUtils.isDev()) {
				apnsServiceBuilder.withSandboxDestination();
			} else {
				apnsServiceBuilder.withProductionDestination();
			}
			ApnsService service = apnsServiceBuilder
					.build();
			String type = StringUtils.lowerCase(model.getType().name());
			PayloadBuilder payloadBuilder = APNS.newPayload()
					.sound("default")
					.customField("type", type)
					.clearBadge();
			String title = model.getTitle();
			if (StringUtils.isNotBlank(title)) {
				payloadBuilder.alertTitle(title);
			}
			String message = model.getMessage();
			if (StringUtils.isNotBlank(message)) {
				payloadBuilder.alertBody(message);
			}
			Object data = model.getData();
			if (data != null) {
				payloadBuilder.customField("data", data);
			}
			String token = model.getToken();
			String payload = payloadBuilder.build();
			service.push(token, payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}