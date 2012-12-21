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
		label = new JLabel("����������...");
		this.setTitle("������������ϵͳ�ͻ���");
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
	// ��������Ϣ
	private int serverPortParam;
	private int poolSizeParam;
	// ������Ϣ
	private String ipParam;
	private int portParam;
	private int timeoutParam;
	private int sleepTimeParam;
	private String statusFileParam;

	// �������
	public static void main(String[] args) {
//		// �豸���
//		String atmId = args[0];
//
//		// // ��������Ϣ
//		int serverPort = Integer.parseInt(args[1]);
//		int poolSize = Integer.parseInt(args[2]);
//		// // ������Ϣ
//		String ip = args[3];
//		int port = Integer.parseInt(args[4]);
//		int sleepTime = Integer.parseInt(args[5]);
//		String statusFile = args[6];
//		int timeout = Integer.parseInt(args[7]);

		 // ATM���
		 String atmId = "71111111";
		 // ��������Ϣ
		 int serverPort = 2702;
		 int poolSize = 10;
		 // ������Ϣ
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
	 * ��������
	 */
	private Socket clientSocket = null;
	private boolean connectFlag = false;
	private BufferedWriter writer = null;

	private void startSendClient() {
		logger.info("��ʼ���ӹ���ˣ�");
		try {
			clientSocket = new Socket();
			System.out.println(getIpParam());
			System.out.println(getPortParam());

			clientSocket.connect(new InetSocketAddress(getIpParam(),
					getPortParam()), getTimeoutParam() * 1000);
			connectFlag = true;

			logger.info("���ӹ���˳ɹ�����ʼ������ʱ����ATM״̬����");
			new Thread() {
				BufferedReader fileReader = null;

				public void run() {
					try {
						while (connectFlag) {
							sleep(getSleepTimeParam() * 1000);
							// ����DLL��ȡATM״̬
							// IMyNative.imn.Java_com_width_MyNative_get();
							// ���ļ��л�ȡATM״̬
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
						logger.error("û�ҵ��ļ���");
					} catch (InterruptedException ie) {
					} catch (IOException ioe) {
						logger.error("�������رգ������������ӣ�");
						resetClient();
					}
				}
			}.start();
		} catch (Exception e) {
			logger.info("���Ӵ��󣬳����ٴ����ӣ�");
			resetClient();
		}

	}

	// �Ͽ�����
	private boolean stopSendClient() {
		if (clientSocket != null) {
			try {
				clientSocket.close();
				connectFlag = false;
			} catch (IOException e) {
				logger.error("�ر����Ӵ���");
				return false;
			}
		}
		return true;
	}

	// ��������
	private void resetClient() {
		stopSendClient();
		try {
			Thread.sleep(getSleepTimeParam() * 1000);
		} catch (InterruptedException e) {
			logger.error("�ȴ�ʧ��");
		}
		startSendClient();
	}

	/**
	 * ��������
	 */
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private boolean isRun = true;
	private ExecutorService exec = null;

	private boolean startServer() {
		logger.info("ִ��������");
		logger.info("********************");
		try {
			logger.info("*�����˿�����=" + getServerPortParam());
			serverSocket = new ServerSocket(getServerPortParam());
			logger.info("*����������=" + getPoolSizeParam());
			exec = Executors.newFixedThreadPool(getPoolSizeParam());
			isRun = true;
			logger.info("*********************");
			new Thread() {
				@Override
				public void run() {
					while (isRun) {
						try {
							logger.info("*��ʼ�ȴ�����");
							socket = serverSocket.accept();
							logger.info("*" + socket.getRemoteSocketAddress()
									+ "����");
							exec.execute(new Work(socket));
						} catch (IOException e) {
							logger.error("�����ȴ����ӷ������!");
						}

					}
				}
			}.start();
			return true;
		} catch (IOException e) {
			logger.error("��������˿�ʧ��");
			return false;
		}
	}

	/**
	 * ֹͣ����
	 */
	private boolean stopServer() {
		logger.info("ִ�йرգ�");
		logger.info("@@@@@@@@@@@@@@@@@");
		try {
			isRun = false;
			if (exec != null && !exec.isShutdown()) {
				exec.shutdown();
				logger.info("�ر��̳߳أ�");
			}
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				logger.info("�رն˿ڣ�");
			}
			return true;
		} catch (Exception e) {
			logger.error("�رշ���ʧ��");
			return false;
		} finally {
			exec = null;
			serverSocket = null;
			logger.info("@@@@@@@@@@@@@@@@@");
		}
	}

	/** �������� */
	private boolean resetServer(int port, int size) {
		logger.info("ִ��������");
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
					logger.info("�ر����ӣ�");
				}
			} catch (IOException e) {
				logger.error("�ر�����ʧ��");
			}
		}

		/** ��ȡͨѶ������ʾ */
		private int getRequest() {
			DataInputStream socketIn = null;
			int type = 0;
			try {
				socketIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				type = socketIn.readInt();
			} catch (Exception e) {
				type = 0;
				logger.error("��ȡͨѶ����ʧ��");
			}
			return type;
		}

		/** �������� */
		private String receiveMessage() {
			DataInputStream socketIn = null;
			String message = null;
			try {
				socketIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				message = socketIn.readUTF();
			} catch (Exception e) {
				message = null;
				logger.error("�����ַ���ͨ��ʧ��");
			}
			return message;
		}

		/** ���ͷ��ر��� */
		private void setResponse(int message) {
			DataOutputStream socketOut = null;
			try {
				socketOut = new DataOutputStream(new BufferedOutputStream(
						socket.getOutputStream()));
				socketOut.writeInt(message);
				socketOut.flush();
			} catch (Exception e) {
				logger.error("���ͷ��ر���ʧ��");
			}
		}

		/** �����ļ� */
		private void receiveFile() {
			DataInputStream socketIn = null;
			DataOutputStream fileOut = null;
			byte[] buffer = new byte[1024];
			try {

				socketIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				// ��ȡ�ļ���
				int size = socketIn.readInt();
				logger.info("�ϴ��ļ�����=" + size);
				for (int i = 0; i < size; i++) {
					// ��ȡ�ļ�ȫ·��
					String path = socketIn.readUTF();
					logger.info("�ϴ�·��=" + path);
					// ��ȡ�ļ���С
					long length = socketIn.readLong();
					logger.info("�ϴ��ļ���С=" + length);
					// �����ļ������
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
				logger.error("�����ļ�ʧ��");
			}
		}

		/** �����ļ� */
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
				logger.error("�����ļ�ʧ��");
				logger.error(e.getLocalizedMessage(), e);
			}
		}

		/** ִ������ */
		private void execCommand(String command) {
			Process exec = null;
			try {
				logger.info("command");
				exec = Runtime.getRuntime().exec(command);
			} catch (Exception e) {
				exec.destroy();
				logger.error("ִ������ʧ��");
			}
		}

		/*����2
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
				// ������Ļ��һ��BufferedImage����screenshot
				BufferedImage screenshot = (new Robot(gd))
						.createScreenCapture(new Rectangle(0, 0, (int) d
								.getWidth(), (int) d.getHeight()));
				// �����ļ�ǰ׺�������ļ���ʽ�������Զ������ļ���

				f = new File("snapShot.png");
				logger.info("Save File " + f.getPath());
				// ��screenshot����д��ͼ���ļ�
				ImageIO.write(screenshot, "png", f);
				logger.info("Save snapShot " + f.getPath());
			} catch (Exception ex) {
				logger.error("��ͼʧ��");
			}
			return f.getPath();
		}*/
		
		/** ���� */
		public String snapShot() {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			File f = null;
			try {
				// ������Ļ��һ��BufferedImage����screenshot
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gs = ge.getScreenDevices();
				GraphicsDevice gd;
				gd=gs[0];
				BufferedImage screenshot = (new Robot(gd))
						.createScreenCapture(new Rectangle(0, 0, (int) d
								.getWidth(), (int) d.getHeight()));
				// �����ļ�ǰ׺�������ļ���ʽ�������Զ������ļ���

				f = new File("snapShot.png");
				logger.info("Save File " + f.getPath());
				// ��screenshot����д��ͼ���ļ�
				ImageIO.write(screenshot, "png", f);
				logger.info("Save snapShot " + f.getPath());
			} catch (Exception ex) {
				logger.error("��ͼʧ��");
			}
			return f.getPath();
		}

		/** ִ���� */
		private void execute(int type) {
			logger.info("*����ʼִ��");
			setResponse(1);
			logger.info("*��������:" + type);
			if (type == 0) {
				close();
			} else {
				if (type == 1) {
					logger.info("*��ʼ�����ļ�");
					// ��ȡ�����ļ�·��
					String path = receiveMessage();
					// �����ļ�������
					sendFile(path);
				} else if (type == 2) {
					logger.info("*�����ļ�");
					receiveFile();
					setResponse(1);
				} else if (type == 3) {
					logger.info("*��ʼִ������");
					String path = receiveMessage();
					execCommand(path);
					setResponse(1);
				} else if (type == 4) {
					logger.info("*��ʼ��ȡ��Ļ״̬");
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
