package net.wasys.util.process;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.wasys.util.DummyUtils;
import org.apache.commons.io.IOUtils;

import net.wasys.util.process.WinProcessUtils.Priority;

import static net.wasys.util.DummyUtils.systraceThread;

public class ProcessExecutor {

	private List<String> command;
	private Process process;
	private InputStream ers;
	private InputStream is;
	private List<Thread> threads = new ArrayList<>();
	private StringBuffer outputBufferError = new StringBuffer();
	private StringBuffer outputBuffer = new StringBuffer();
	private BlockingQueue<Boolean> finishQueue = new LinkedBlockingQueue<>();
	private BlockingQueue<Boolean> finishErrorQueue = new LinkedBlockingQueue<>();
	private boolean sysout;
	private Priority priority;

	public ProcessExecutor(boolean sysout) {
		this.sysout = sysout;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

	public void close() {
		if(process != null) {
			process.destroyForcibly();
		}
		if(ers != null) {
			IOUtils.closeQuietly(ers);
		}
		if(is != null) {
			IOUtils.closeQuietly(is);
		}
		for (Thread thread : threads) {
			thread.interrupt();
		}
	}

	public void execute(boolean waitFirstOutput) throws Exception, InterruptedException {

		BlockingQueue<Boolean> firstOutputQueue = new LinkedBlockingQueue<>();

		String[] exec2 = command.toArray(new String[command.size()]);
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(exec2);

		if(priority != null) {
			Integer processId = ProcessUtils.getProcessId(process);
			if(processId != null) {
				ProcessUtils.setPriority(processId.toString(), priority);
			} else {
				systraceThread("processId não encontrado, impossível setar prioridade");
			}
		}

		ers = process.getErrorStream();
		is = process.getInputStream();

		Thread thread1 = new Thread(() -> sysoutProcess(true, ers, firstOutputQueue));
		Thread thread2 = new Thread(() -> sysoutProcess(false, is, firstOutputQueue));
		thread1.start();
		thread2.start();
		threads.add(thread1);
		threads.add(thread2);

		if(waitFirstOutput) {
			firstOutputQueue.take();
		}
	}

	private void sysoutProcess(boolean error, InputStream is, BlockingQueue<Boolean> firstOutputQueue) {

		String commandStr = command.toString();
		String prefix = (commandStr.contains(".jar") && commandStr.contains(".jar")) ? "[jar]" : "";
		prefix +=  (error ? "error" : "") + " -> ";

		boolean added = false;
		try {
			InputStreamReader isr = new InputStreamReader(is, "ISO-8859-1");
			BufferedReader br = new BufferedReader(isr);
			for(String line; (line = br.readLine()) != null; ) {

				if(sysout) {
					systraceThread(prefix + line);
				}

				if(error) {
					outputBufferError.append(line).append("\n");
				} else {
					outputBuffer.append(line).append("\n");
				}
				if(!added) {
					firstOutputQueue.add(error);
					added = true;
				}
			}

			if(error) {
				finishErrorQueue.add(true);
			} else {
				finishQueue.add(true);
			}
		}
		catch (Exception e) {
			e.getMessage();
		}
	}

	public StringBuffer getOutputBuffer() {
		return outputBuffer;
	}

	public StringBuffer getOutputBufferError() {
		return outputBufferError;
	}

	public void waitFinish() {

		try {
			finishErrorQueue.take();
			finishQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}
}
