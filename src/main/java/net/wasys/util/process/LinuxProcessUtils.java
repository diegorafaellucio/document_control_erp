package net.wasys.util.process;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinuxProcessUtils {

	public static Map<String, Long> getEspacoUtilizadoDiscoPercent(List<String> diskNames) {

		StringBuilder commandStr = new StringBuilder();
		commandStr.append("df -l -B1 | awk '{print $6 \" \" $5}'");

		return getEspacoDisco(commandStr, diskNames);
	}

	public static Map<String, Long> getEspacoUtilizadoDisco(List<String> diskNames) {

		StringBuilder commandStr = new StringBuilder();
		commandStr.append("df -l -B1 | awk '{print $6 \" \" $3}'");

		return getEspacoDisco(commandStr, diskNames);
	}

	public static Map<String, Long> getEspacoDisponivelDisco(List<String> diskNames) {

		StringBuilder commandStr = new StringBuilder();
		commandStr.append("df -l -B1 | awk '{print $6 \" \" $4}'");

		return getEspacoDisco(commandStr, diskNames);
	}

	private static Map<String, Long> getEspacoDisco(StringBuilder commandStr, List<String> diskNames) {
		ProcessExecutor executor = new ProcessExecutor(false);
		List<String> command = new ArrayList<>();
		command.add("/bin/sh");
		command.add("-c");
		command.add(commandStr.toString());
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

			Map<String, Long> map = new LinkedHashMap<>();
			String[] split = outputStr.split("\n");
			for (String linha : split) {
				String[] split2 = linha.split(" ");
				String diskName = split2[0];
				if(diskNames == null || diskNames.contains(diskName)) {
					String espacoDisponivelStr = split2[1];
					espacoDisponivelStr = espacoDisponivelStr.replace("%", "");
					if(StringUtils.isNumeric(espacoDisponivelStr)) {
						Long espacoDisponivel = new Long(espacoDisponivelStr);
						map.put(diskName, espacoDisponivel);
					}
				}
			}

			return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}
	}

	public static BigDecimal getCpuUsage() {

		List<String> command = new ArrayList<>();
		command.add("/bin/sh");
		command.add("-c");
		command.add("top -bn1 | grep \"Cpu(s)\" | awk '{print $2, $4}'");

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
			valorStr = valorStr.replace("\n", "");
			valorStr = valorStr.replace("\n", "");
			String[] split1 = valorStr.split(" ");
			String cpuUsuario = split1[0];
			String cpuSistema = split1[1];

			return new BigDecimal(cpuUsuario).add(new BigDecimal(cpuSistema));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			executor.close();
		}
	}

	public static BigDecimal getLoad() {

		List<String> command = new ArrayList<>();
		command.add("/bin/sh");
		command.add("-c");
		command.add("cat /proc/loadavg | awk '{print $1}'");

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
			valorStr = StringUtils.trim(valorStr);
			return new BigDecimal(valorStr);
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
