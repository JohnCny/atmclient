package cn.sh.ae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class TestMain {
	Socket client;

	FileInputStream fis;// 此输入流负责读取本机上要传输的文件
	DataOutputStream dos;// 此输出流负责向另一台电脑(服务器端)传输数据
	DataInputStream dis;// 此输入流负责读取另一台电脑的回应信息

	public void ClientStart() {
		try {
			client = new Socket("192.168.2.101", 30000);// 服务器端的IP,(这个只是在局域网内的)我的是这个,你的根据实际而定
			// client=new Socket("localhost",30000);
			System.out.println("已连接");
			dos = new DataOutputStream(client.getOutputStream());
			dis = new DataInputStream(client.getInputStream());
			transmit(new File("e:\\1\\"));
			String s = "/]00";// 提示传输完毕的标记
			byte b[] = s.getBytes();
			dos.write(b, 0, s.length());
			dos.flush();
		} catch (IOException e) {
			System.out.println("Error");
		}
	}

	public void transmit(File f) throws IOException// 这是传输的核心,而且将被递归
	{
		byte b[];
		String ts;
		int ti;
		for (File f1 : f.listFiles()) { // 首先通过if语句判断f1是文件还是文件夹
			if (f1.isDirectory()) // fi是文件夹,则向服务器端传送一条信息
			{
				ts = "/]0f" + (f1.getPath().replace("e:\\1\\", ""));// "/]0f"用于表示这条信息的内容是文件夹名称
				b = ts.getBytes();
				dos.write(b);
				dos.flush();
				dis.read();
				transmit(f1);// 由于f1是文件夹(即目录),所以它里面很有可能还有文件或者文件夹,所以进行递归
			} else {
				fis = new FileInputStream(f1);
				ts = "/]0c" + (f1.getPath().replace("e:\\1\\", ""));// 同上,表示这是一个文件的名称
				b = ts.getBytes();
				dos.write(b);
				dos.flush();
				dis.read();
				dos.writeInt(fis.available());// 传输一个整型值,指明将要传输的文件的大小
				dos.flush();
				dis.read();
				b = new byte[10000];
				while (fis.available() > 0)// 开始传送文件
				{
					ti = fis.read(b);
					dos.write(b, 0, ti);
					dos.flush();
				}
				dos.flush();
				fis.close();
				dis.read();
			}
		}

	}

}
