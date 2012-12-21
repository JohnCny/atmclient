package cn.sh.ae;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

public class AtmcServerMain extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1030935824642612431L;
	static Logger logger = Logger.getLogger(AtmcServerMain.class.getName());

	private JLabel label;

	private AtmcServerMain() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("./aelogo2.jpg"));
		label = new JLabel("服务运行中...");
		this.setTitle("亚银自助管理系统客户端");
		this.setLayout(new FlowLayout());
		this.add(label);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
				stopServer();
				stopSendClient();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				stopServer();
				stopSendClient();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {

			}
		});
		this.setBounds(0, 0, 250, 80);
		this.setVisible(true);
	}

	private String atmIdParam;
	// 服务器信息
	private int serverPortParam;
	private int poolSizeParam;
	// 连接信息
	private String ipParam;
	private int portParam;
	private int timeoutParam;
	private int sleepTimeParam;
	private String statusFileParam;

	// 程序入口
	public static void main(String[] args) {
//		// 设备编号
//		String atmId = args[0];
//
//		// // 服务器信息
//		int serverPort = Integer.parseInt(args[1]);
//		int poolSize = Integer.parseInt(args[2]);
//		// // 连接信息
//		String ip = args[3];
//		int port = Integer.parseInt(args[4]);
//		int sleepTime = Integer.parseInt(args[5]);
//		String statusFile = args[6];
//		int timeout = Integer.parseInt(args[7]);

		 // ATM编号
		 String atmId = "71111111";
		 // 服务器信息
		 int serverPort = 2702;
		 int poolSize = 10;
		 // 连接信息
		 String ip = "32.221.32.65";
//		 String ip = "127.0.0.1";

		 int port = 2704 ;
		 int timeout = 10;
		 int sleepTime = 10;
		 String statusFile = "c:/Status_Result.txt";
		
		 String ATMlogFile = "c:/xBankTrans/Trace/Trans.jrn";
		AtmcServerMain asm = new AtmcServerMain();

		asm.setAtmIdParam(atmId);
		asm.setServerPortParam(serverPort);
		asm.setPoolSizeParam(poolSize);
		asm.setIpParam(ip);
		asm.setPortParam(port);
		asm.setTimeoutParam(timeout);
		asm.setSleepTimeParam(sleepTime);
		asm.setStatusFileParam(statusFile);

		asm.startSendClient();
		asm.startServer();

	}
	private void check_atm_status(){
		try{
			
		}
	}
	/**
	 * 启动连接
	 */
	private Socket clientSocket = null;
	private boolean connectFlag = false;
	private BufferedWriter writer = null;

	private void startSendClient() {
		logger.info("开始连接管理端！");
		try {
			clientSocket = new Socket();
			System.out.println(getIpParam());
			System.out.println(getPortParam());

			clientSocket.connect(new InetSocketAddress(getIpParam(),
					getPortParam()), getTimeoutParam() * 1000);
			connectFlag = true;

			logger.info("连接管理端成功！开始启动定时发送ATM状态任务");
			new Thread() {
				BufferedReader fileReader = null;

				public void run() {
					try {
						while (connectFlag) {
							sleep(getSleepTimeParam() * 1000);
							// 调用DLL获取ATM状态
							// IMyNative.imn.Java_com_width_MyNative_get();
							// 从文件中获取ATM状态
							fileReader = new BufferedReader(
									(new InputStreamReader(new FileInputStream(
											getStatusFileParam()))));
							String sendMsg = fileReader.readLine();
							if (sendMsg != null) {
								logger.info(sendMsg);
								writer = new BufferedWriter(
										new OutputStreamWriter(clientSocket
												.getOutputStream()));
								writer.write(getAtmIdParam() + "#" + sendMsg);
//								writer.write( sendMsg);

								writer.newLine();
								writer.flush();
							}
						}
					} catch (FileNotFoundException fnfe) {
						logger.error("没找到文件！");
					} catch (InterruptedException ie) {
					} catch (IOException ioe) {
						logger.error("服务器关闭，尝试重启连接！");
						resetClient();
					}
				}
			}.start();
		} catch (Exception e) {
			logger.info("连接错误，尝试再次连接！");
			resetClient();
		}

	}

	// 断开连接
	private boolean stopSendClient() {
		if (clientSocket != null) {
			try {
				clientSocket.close();
				connectFlag = false;
			} catch (IOException e) {
				logger.error("关闭连接错误");
				return false;
			}
		}
		return true;
	}

	// 断线重连
	private void resetClient() {
		stopSendClient();
		try {
			Thread.sleep(getSleepTimeParam() * 1000);
		} catch (InterruptedException e) {
			logger.error("等待失败");
		}
		startSendClient();
	}

	/**
	 * 启动服务
	 */
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private boolean isRun = true;
	private ExecutorService exec = null;

	private boolean startServer() {
		logger.info("执行启动！");
		logger.info("********************");
		try {
			logger.info("*工作端口启动=" + getServerPortParam());
			serverSocket = new ServerSocket(getServerPortParam());
			logger.info("*工作池启动=" + getPoolSizeParam());
			exec = Executors.newFixedThreadPool(getPoolSizeParam());
			isRun = true;
			logger.info("*********************");
			new Thread() {
				@Override
				public void run() {
					while (isRun) {
						try {
							logger.info("*开始等待任务");
							socket = serverSocket.accept();
							logger.info("*" + socket.getRemoteSocketAddress()
									+ "接入");
							exec.execute(new Work(socket));
						} catch (IOException e) {
							logger.error("启动等待连接服务错误!");
						}

					}
				}
			}.start();
			return true;
		} catch (IOException e) {
			logger.error("启动管理端口失败");
			return false;
		}
	}

	/**
	 * 停止服务
	 */
	private boolean stopServer() {
		logger.info("执行关闭！");
		logger.info("@@@@@@@@@@@@@@@@@");
		try {
			isRun = false;
			if (exec != null && !exec.isShutdown()) {
				exec.shutdown();
				logger.info("关闭线程池！");
			}
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				logger.info("关闭端口！");
			}
			return true;
		} catch (Exception e) {
			logger.error("关闭服务失败");
			return false;
		} finally {
			exec = null;
			serverSocket = null;
			logger.info("@@@@@@@@@@@@@@@@@");
		}
	}

	/** 重启服务 */
	private boolean resetServer(int port, int size) {
		logger.info("执行重启！");
		boolean flag = false;
		logger.info("#####################");
		flag = stopServer();
		flag = startServer();
		logger.info("#####################");
		return flag;
	}

	/***************************************************************************
	 * 
	 * 
	 */
	class Work implements Runnable {
		private Socket socket = null;

		public Work(Socket socket) {
			this.socket = socket;
		}

		private void close() {
			try {
				if (socket != null) {
					socket.close();
					logger.info("关闭连接！");
				}
			} catch (IOException e) {
				logger.error("关闭连接失败");
			}
		}

		/** 获取通讯任务提示 */
		private int getRequest() {
			DataInputStream socketIn = null;
			int type = 0;
			try {
				socketIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				type = socketIn.readInt();
			} catch (Exception e) {
				type = 0;
				logger.error("获取通讯任务失败");
			}
			return type;
		}

		/** 接收文字 */
		private String receiveMessage() {
			DataInputStream socketIn = null;
			String message = null;
			try {
				socketIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				message = socketIn.readUTF();
			} catch (Exception e) {
				message = null;
				logger.error("接受字符串通信失败");
			}
			return message;
		}

		/** 发送返回报文 */
		private void setResponse(int message) {
			DataOutputStream socketOut = null;
			try {
				socketOut = new DataOutputStream(new BufferedOutputStream(
						socket.getOutputStream()));
				socketOut.writeInt(message);
				socketOut.flush();
			} catch (Exception e) {
				logger.error("发送返回报文失败");
			}
		}

		/** 接收文件 */
		private void receiveFile() {
			DataInputStream socketIn = null;
			DataOutputStream fileOut = null;
			byte[] buffer = new byte[1024];
			try {

				socketIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				// 获取文件数
				int size = socketIn.readInt();
				logger.info("上传文件数量=" + size);
				for (int i = 0; i < size; i++) {
					// 获取文件全路径
					String path = socketIn.readUTF();
					logger.info("上传路径=" + path);
					// 获取文件大小
					long length = socketIn.readLong();
					logger.info("上传文件大小=" + length);
					// 建立文件输出流
					fileOut = new DataOutputStream(new BufferedOutputStream(
							new FileOutputStream(path)));
					int l = 0, s = 0;
					while ((l = socketIn.read(buffer)) != -1) {
						s += l;
						fileOut.write(buffer, 0, l);
						if (s == (int) length)
							break;
					}
					fileOut.flush();
					fileOut.close();
				}
			} catch (Exception e) {
				logger.error("接收文件失败");
			}
		}

		/** 发送文件 */
		public void sendFile(String path) {
			DataInputStream fileIn = null;
			File file = new File(path);
			DataOutputStream socketOut = null;
			byte[] buffer = new byte[1024];
			try {
				socketOut = new DataOutputStream(new BufferedOutputStream(
						socket.getOutputStream()));
				fileIn = new DataInputStream(new BufferedInputStream(
						new FileInputStream(file)));
				long length = file.length();
				socketOut.writeLong(file.length());
				int l = 0, s = 0;
				while ((l = fileIn.read(buffer)) != -1) {
					s += l;
					socketOut.write(buffer, 0, l);
					if (s == (int) length) {
						break;
					}
				}
				fileIn.close();
				socketOut.flush();
			} catch (Exception e) {
				logger.error("发送文件失败");
				logger.error(e.getLocalizedMessage(), e);
			}
		}

		/** 执行任务 */
		private void execCommand(String command) {
			Process exec = null;
			try {
				logger.info("command");
				exec = Runtime.getRuntime().exec(command);
			} catch (Exception e) {
				exec.destroy();
				logger.error("执行任务失败");
			}
		}

		/*截屏2
		public String snapShot_second_screen() {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			File f = null;
			try {
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gs = ge.getScreenDevices();
				GraphicsDevice gd;
				if (gs.length>1){
					gd=gs[1];
				}
				else{
					gd=gs[0];
				}
				// 拷贝屏幕到一个BufferedImage对象screenshot
				BufferedImage screenshot = (new Robot(gd))
						.createScreenCapture(new Rectangle(0, 0, (int) d
								.getWidth(), (int) d.getHeight()));
				// 根据文件前缀变量和文件格式变量，自动生成文件名

				f = new File("snapShot.png");
				logger.info("Save File " + f.getPath());
				// 将screenshot对象写入图像文件
				ImageIO.write(screenshot, "png", f);
				logger.info("Save snapShot " + f.getPath());
			} catch (Exception ex) {
				logger.error("截图失败");
			}
			return f.getPath();
		}*/
		
		/** 截屏 */
		public String snapShot() {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			File f = null;
			try {
				// 拷贝屏幕到一个BufferedImage对象screenshot
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gs = ge.getScreenDevices();
				GraphicsDevice gd;
				gd=gs[0];
				BufferedImage screenshot = (new Robot(gd))
						.createScreenCapture(new Rectangle(0, 0, (int) d
								.getWidth(), (int) d.getHeight()));
				// 根据文件前缀变量和文件格式变量，自动生成文件名

				f = new File("snapShot.png");
				logger.info("Save File " + f.getPath());
				// 将screenshot对象写入图像文件
				ImageIO.write(screenshot, "png", f);
				logger.info("Save snapShot " + f.getPath());
			} catch (Exception ex) {
				logger.error("截图失败");
			}
			return f.getPath();
		}

		/** 执行体 */
		private void execute(int type) {
			logger.info("*任务开始执行");
			setResponse(1);
			logger.info("*任务类型:" + type);
			if (type == 0) {
				close();
			} else {
				if (type == 1) {
					logger.info("*开始发送文件");
					// 获取下载文件路径
					String path = receiveMessage();
					// 传送文件到请求方
					sendFile(path);
				} else if (type == 2) {
					logger.info("*接收文件");
					receiveFile();
					setResponse(1);
				} else if (type == 3) {
					logger.info("*开始执行任务");
					String path = receiveMessage();
					execCommand(path);
					setResponse(1);
				} else if (type == 4) {
					logger.info("*开始获取屏幕状态");
					sendFile(snapShot());
				}
				execute(getRequest());
			}
		}

		@Override
		public void run() {
			execute(getRequest());
		}
	}

	public String getAtmIdParam() {
		return atmIdParam;
	}

	public void setAtmIdParam(String atmIdParam) {
		this.atmIdParam = atmIdParam;
	}

	public int getServerPortParam() {
		return serverPortParam;
	}

	public void setServerPortParam(int serverPortParam) {
		this.serverPortParam = serverPortParam;
	}

	public int getPoolSizeParam() {
		return poolSizeParam;
	}

	public void setPoolSizeParam(int poolSizeParam) {
		this.poolSizeParam = poolSizeParam;
	}

	public String getIpParam() {
		return ipParam;
	}

	public void setIpParam(String ipParam) {
		this.ipParam = ipParam;
	}

	public int getPortParam() {
		return portParam;
	}

	public void setPortParam(int portParam) {
		this.portParam = portParam;
	}

	public int getTimeoutParam() {
		return timeoutParam;
	}

	public void setTimeoutParam(int timeoutParam) {
		this.timeoutParam = timeoutParam;
	}

	public int getSleepTimeParam() {
		return sleepTimeParam;
	}

	public void setSleepTimeParam(int sleepTimeParam) {
		this.sleepTimeParam = sleepTimeParam;
	}

	public String getStatusFileParam() {
		return statusFileParam;
	}

	public void setStatusFileParam(String statusFileParam) {
		this.statusFileParam = statusFileParam;
	}

}
