package net.wasys.util.process;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import net.wasys.util.DummyUtils;

public class WinProcessUtils {

	public enum Priority {

		IDLE(64, "idle"),
		BELOW_NORMAL(16384, "below normal"),
		NORMAL(32, "normal"),
		ABOVE_NORMAL(32768, "above normal"),
		HIGH(128, "high priority"),
		REAL_TIME(256, "realtime");

		@SuppressWarnings("unused")
		private int cod;
		private String key;

		Priority(int cod, String key) {
			this.cod = cod;
			this.key = key;
		}
	}

	public static void main(String[] args) {

		Map<String, Long> espacoDisponivelDisco = getEspacoDisponivelDisco(Arrays.asList("C:", "D:"));
		for (String disco : espacoDisponivelDisco.keySet()) {

			Long val = espacoDisponivelDisco.get(disco);
			DummyUtils.systraceThread(disco + " " + DummyUtils.toFileSize(val));
		}

		BigDecimal cpuUsage = getCpuUsage();
		DummyUtils.systraceThread("cpuUsage: " + cpuUsage + "%");
	}

	public static BigDecimal getCpuUsage() {

		String comando = "wmic cpu get loadpercentage";
		String[] split = comando.split(" ");
		List<String> command = Arrays.asList(split);

		ProcessExecutor executor = new ProcessExecutor(false);
		executor.setCommand(command);

		try {
			executor.execute(true);

			StringBuffer outputBufferError = executor.getOutputBufferError();
			String errorStr = outputBufferError.toString();
			if(StringUtils.isNotBlank(errorStr)) {
				throw new RuntimeException(errorStr);
			}

			executor.waitFinish();

			StringBuffer outputBuffer = executor.getOutputBuffer();
			String valorStr = outputBuffer.toString();
			valorStr = valorStr.replace("LoadPercentage", "");

			String[] split1 = valorStr.split("\n");

			List<Integer> valores = new ArrayList<>();
			for (String val : split1) {
				val = StringUtils.trim(val);
				if(StringUtils.isNotBlank(val)) {
					valores.add((new Integer(val)));
				}
			}
			return DummyUtils.getMedia(valores, 2);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}
	}

	public static Map<String, Long> getEspacoDisponivelDisco(List<String> diskNames) {

		Map<String, Long> map = new LinkedHashMap<>();

		for (String diskName : diskNames) {
			Long ed = getEspacoDisponivelDisco(diskName);
			map.put(diskName, ed);
		}

		return map;
	}

	private static Long getEspacoDisponivelDisco(String diskName) {

		String comando = "fsutil volume diskfree " + diskName;
		String[] comandoSplit = comando.split(" ");
		List<String> command = Arrays.asList(comandoSplit);
		ProcessExecutor executor = new ProcessExecutor(false);
		executor.setCommand(command);

		try {
			executor.execute(true);

			StringBuffer outputBufferError = executor.getOutputBufferError();
			String errorStr = outputBufferError.toString();
			if(StringUtils.isNotBlank(errorStr)) {
				throw new RuntimeException(errorStr);
			}

			executor.waitFinish();

			StringBuffer outputBuffer = executor.getOutputBuffer();
			String outputStr = outputBuffer.toString();

			String[] split = outputStr.split("\n");
			for (String linha : split) {

				if(linha.matches(".*dispon.*") || linha.contains("avail")) {

					Pattern pattern = Pattern.compile("(\\d+)$");
					Matcher matcher = pattern.matcher(linha);
					if(matcher.find()) {
						String espacoLivreStr = matcher.group();
						Long espacoLivre = new Long(espacoLivreStr);
						return espacoLivre;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}

		return null;
	}

	public static List<ProcessVO> findProcess(String processName) {

		List<String> command = new ArrayList<>();
		command.add("wmic");
		command.add("process");
		if(StringUtils.isNotBlank(processName)) {
			command.add("where");
			command.add("name='" + processName + "'");
		}
		command.add("get");
		command.add("ProcessID,");
		command.add("ExecutablePath,");
		command.add("Name");
		command.add("/FORMAT:csv");

		ProcessExecutor executor = new ProcessExecutor(false);
		executor.setCommand(command);

		try {
			executor.execute(true);

			StringBuffer outputBufferError = executor.getOutputBufferError();
			String errorStr = outputBufferError.toString();
			if(StringUtils.isNotBlank(errorStr)) {
				String aux = errorStr.replace("\n", "");
				if(aux.matches("Nenhuma inst.{0,1}ncia dispon.{0,1}vel.*")) {
					return new ArrayList<>();
				}
				throw new RuntimeException(errorStr);
			}

			executor.waitFinish();

			List<ProcessVO> vos = new ArrayList<>();
			StringBuffer outputBuffer = executor.getOutputBuffer();

			String[] split = outputBuffer.toString().split("\n");
			boolean header = true;
			for (String line : split) {

				if(StringUtils.isNotBlank(line)) {

					if(header) {
						header = false;
						continue;
					}

					String[] columns = line.split(",");
					String node = columns[0];
					String path = columns[1];
					String name = columns[2];
					String processId = columns[3];

					ProcessVO vo = new ProcessVO();
					vo.setName(name);
					vo.setNode(node);
					vo.setPath(path);
					vo.setProcessId(processId);
					vos.add(vo);
				}
			}

			return vos;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}
	}

	public static void killProcess(String processId) {

		List<String> command = new ArrayList<>();
		command.add("taskkill");
		command.add("/PID");
		command.add(processId);
		command.add("/F");

		ProcessExecutor executor = new ProcessExecutor(false);
		executor.setCommand(command);

		try {
			executor.execute(true);

			executor.waitFinish();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}
	}

	public static Integer getProcessId(Process process) {

		Class<? extends Process> processClass = process.getClass();
		String processClassName = processClass.getName();

		if (processClassName.equals("java.lang.Win32Process") || processClassName.equals("java.lang.ProcessImpl")) {
			/* determine the pid on windows plattforms */
			try {
				Field f = process.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				long handl = f.getLong(process);

				WinNT.HANDLE handle = new WinNT.HANDLE();
				handle.setPointer(Pointer.createConstant(handl));

				Kernel32 kernel = Kernel32.INSTANCE;
				int processId = kernel.GetProcessId(handle);

				return processId;
			}
			catch (Throwable e) {
			}
		}

		if(processClassName.equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */
			try {
				Field f = processClass.getDeclaredField("pid");
				f.setAccessible(true);
				int pid = f.getInt(process);

				return pid;
			}
			catch (Throwable e) {
			}
		}

		return null;
	}

	public static void setPriority(String processId, Priority priority) {

		List<String> command = new ArrayList<>();
		command.add("wmic");
		command.add("process");
		command.add("where");
		command.add("ProcessID='" + processId + "'");
		command.add("CALL");
		command.add("setpriority");
		command.add(priority.key);

		ProcessExecutor executor = new ProcessExecutor(false);
		executor.setCommand(command);

		try {
			executor.execute(true);

			executor.waitFinish();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}
	}
}
