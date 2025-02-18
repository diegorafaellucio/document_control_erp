package net.wasys.util.process;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import net.wasys.util.process.WinProcessUtils.Priority;

public class ProcessUtils {

	private static String SO = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (SO.indexOf("win") >= 0);
	}

	public static BigDecimal getCpuUsage() {
		if(isWindows()) {
			return WinProcessUtils.getCpuUsage();
		} else {
			return LinuxProcessUtils.getCpuUsage();
		}
	}

	public static Map<String, Long> getEspacoUtilizadoDiscoPercent(List<String> diskNames) {
		if(isWindows()) {
			throw new RuntimeException("método não implementado para SO " + SO);
		} else {
			return LinuxProcessUtils.getEspacoUtilizadoDiscoPercent(diskNames);
		}
	}

	public static Map<String, Long> getEspacoUtilizadoDisco(List<String> diskNames) {
		if(isWindows()) {
			throw new RuntimeException("método não implementado para SO " + SO);
		} else {
			return LinuxProcessUtils.getEspacoUtilizadoDisco(diskNames);
		}
	}

	public static Map<String, Long> getEspacoDisponivelDisco(List<String> diskNames) {
		if(isWindows()) {
			return WinProcessUtils.getEspacoDisponivelDisco(diskNames);
		} else {
			return LinuxProcessUtils.getEspacoDisponivelDisco(diskNames);
		}
	}

	public static List<ProcessVO> findProcess(String processName) {
		if(isWindows()) {
			return WinProcessUtils.findProcess(processName);
		} else {
			//return LinuxProcessUtils.findProcess(processName);
			throw new RuntimeException("método não implementado para SO " + SO);
		}
	}

	public static void killProcess(String processId) {
		if(isWindows()) {
			WinProcessUtils.killProcess(processId);
		} else {
			//LinuxProcessUtils.killProcess(processId);
			throw new RuntimeException("método não implementado para SO " + SO);
		}
	}

	public static Integer getProcessId(Process process) {
		if(isWindows()) {
			return WinProcessUtils.getProcessId(process);
		} else {
			//return LinuxProcessUtils.getProcessId(process);
			throw new RuntimeException("método não implementado para SO " + SO);
		}
	}

	public static void setPriority(String processId, Priority priority) {
		if(isWindows()) {
			WinProcessUtils.setPriority(processId, priority);
		} else {
			//return LinuxProcessUtils.getProcessId(process);
			throw new RuntimeException("método não implementado para SO " + SO);
		}
	}

	public static BigDecimal getLoad() {
		if(isWindows()) {
			throw new RuntimeException("método não implementado para SO " + SO);
		} else {
			return LinuxProcessUtils.getLoad();
		}
	}
}
