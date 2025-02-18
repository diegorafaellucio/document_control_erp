package net.wasys.getdoc.mb.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.mb.model.UploadResultModel;

@Controller
@RequestMapping("/file")
public class FileController extends MobileController {

	@Autowired private ResourceService resourceService;
	
	@RequestMapping(value="/upload")
	public ResponseEntity<UploadResultModel> upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
		UploadResultModel uploadModel = new UploadResultModel();
		if (CollectionUtils.isNotEmpty(files)) {
			String path = resourceService.getValue(ResourceService.TMP_MOBILE_PATH);
			for (MultipartFile multipart : files) {
				byte[] bytes = multipart.getBytes();
				String fileName = multipart.getOriginalFilename();
				String extension = FilenameUtils.getExtension(fileName);
				File file = create(path, extension);
				uploadModel.add(file.getName());
				uploadModel.setFile(file);
				uploadModel.setPath(file.getPath());
				uploadModel.setFileName(fileName);
				FileUtils.writeByteArrayToFile(file, bytes);
			}
		}
		return new ResponseEntity<UploadResultModel>(uploadModel, HttpStatus.OK);
	}
	
	@RequestMapping(value="/load/{name:.+}")
	public ResponseEntity<byte[]> load(@PathVariable String name) {
		String path = resourceService.getValue(ResourceService.TMP_MOBILE_PATH);
		File file = new File(path, name);
		ResponseEntity<byte[]> responseEntity = null;
		if (file.exists()) {
			try {
				byte[] bytes = FileUtils.readFileToByteArray(file);
				String contentType = URLConnection.guessContentTypeFromName(file.getName());
				HttpHeaders headers = new HttpHeaders();
				MediaType mediaType = MediaType.valueOf(contentType);
			    headers.setContentType(mediaType);
				responseEntity = new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (responseEntity == null) {
			responseEntity = new ResponseEntity<byte[]>(HttpStatus.OK);
		}
		return responseEntity;
	}
	
	
	private static File create(String path, String extension) {
		File parent = new File(path);
		if (!parent.exists()) {
			parent.mkdirs();
		}
		String name = getRandomName(extension);
		File file = new File(parent, name);
		while (file.exists()) {
			name = getRandomName(extension);
			file = new File(parent, name);
		}
		return file;
	}
	
	private static String getRandomName(String extension) {
		return getRandomName(extension, 18);
	}
	
	private static String getRandomName(String extension, int length) {
		return String.format("%1$s.%2$s", RandomStringUtils.random(length, true, true), extension);
	}
}
