package net.wasys.getdoc.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import net.wasys.getdoc.domain.vo.FileVO;

public class UploadSessionHandler {

	private static final String UPLOAD_SERVLET_CONTEXT_KEY = "upload_servlet_context_key";
	private static final String UPLOAD_SESSION_KEY = "upload_session_key";

	private Map<String, SessionVO> map = new HashMap<String, SessionVO>();

	public static UploadSessionHandler getIntance(ServletContext servletContext) {

		UploadSessionHandler uploadSessionVO = (UploadSessionHandler) servletContext.getAttribute(UPLOAD_SERVLET_CONTEXT_KEY);

		if(uploadSessionVO == null) {

			uploadSessionVO = new UploadSessionHandler();
			servletContext.setAttribute(UPLOAD_SERVLET_CONTEXT_KEY, uploadSessionVO);
		}

		return uploadSessionVO;
	}

	public Map<Integer, FileVO> getMap(final HttpSession session, Integer chave) {

		List<FileVO> fileList = getList(session, chave);

		Map<Integer, FileVO> map = new LinkedHashMap<>();

		for (FileVO vo : fileList) {

			Integer index = getIndex(vo);
			map.put(index, vo);
		}

		return map;
	}

	private Integer getIndex(FileVO vo) {

		String fileName = vo.getName();
		String indexStr = fileName.replace("img-", "");

		int lastIndexOf = indexStr.lastIndexOf(".");
		if(lastIndexOf > 0) {
			indexStr = indexStr.substring(0, lastIndexOf);
		}

		return new Integer(indexStr);
	}

	public List<FileVO> getList(final HttpSession session, Integer chave) {

		String sessionId = session.getId();
		SessionVO sessionVO = map.get(sessionId);
		if(sessionVO == null) {

			sessionVO = criarVO(session);
		}

		List<FileVO> fileList = sessionVO.getFileList(chave);
		return fileList;
	}

	private SessionVO criarVO(final HttpSession session) {

		String sessionId = session.getId();

		SessionVO sessionVO = new SessionVO();
		map.put(sessionId, sessionVO);

		Limpador limpador = new Limpador();
		limpador.setSession(session);

		Object attribute = session.getAttribute(UPLOAD_SESSION_KEY);
		if(attribute == null) {
			session.setAttribute(UPLOAD_SESSION_KEY, limpador);
		}

		return sessionVO;
	}

	public void clear(HttpSession session, Integer chave) {

		String sessionId = session.getId();

		if(chave != null) {

			SessionVO sessionVO = map.get(sessionId);
			if(sessionVO != null) {
				sessionVO.clear(chave);
			}
		}
		else {

			map.remove(sessionId);
		}
	}

	private class SessionVO {

		private Map<Integer, List<FileVO>> map = new HashMap<Integer, List<FileVO>>();

		public void clear(Integer chave) {
			map.remove(chave);
		}

		public List<FileVO> getFileList(Integer chave) {

			List<FileVO> fileList = map.get(chave);

			if(fileList == null) {
				fileList = new ArrayList<FileVO>();
				map.put(chave, fileList);
			}

			return fileList;
		}
	}

	public static class Limpador implements HttpSessionBindingListener, Serializable {

		private HttpSession session;

		@Override
		public void valueUnbound(HttpSessionBindingEvent arg0) {

			ServletContext servletContext = session.getServletContext();

			UploadSessionHandler uploadSessionVO = UploadSessionHandler.getIntance(servletContext);
			uploadSessionVO.clear(session, null);
		}

		@Override
		public void valueBound(HttpSessionBindingEvent arg0) {}

		public void setSession(HttpSession session) {
			this.session = session;
		}
	}
}
