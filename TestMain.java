package cn.sh.ae;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class TestMain {
	Socket client;

	FileInputStream fis;// �������������ȡ������Ҫ������ļ�
	DataOutputStream dos;// ���������������һ̨����(��������)��������
	DataInputStream dis;// �������������ȡ��һ̨���ԵĻ�Ӧ��Ϣ

	public void ClientStart() {
		try {
			client = new Socket("192.168.2.101", 30000);// �������˵�IP,(���ֻ���ھ������ڵ�)�ҵ������,��ĸ���ʵ�ʶ���
			// client=new Socket("localhost",30000);
			System.out.println("������");
			dos = new DataOutputStream(client.getOutputStream());
			dis = new DataInputStream(client.getInputStream());
			transmit(new File("e:\\1\\"));
			String s = "/]00";// ��ʾ������ϵı��
			byte b[] = s.getBytes();
			dos.write(b, 0, s.length());
			dos.flush();
		} catch (IOException e) {
			System.out.println("Error");
		}
	}

	public void transmit(File f) throws IOException// ���Ǵ���ĺ���,���ҽ����ݹ�
	{
		byte b[];
		String ts;
		int ti;
		for (File f1 : f.listFiles()) { // ����ͨ��if����ж�f1���ļ������ļ���
			if (f1.isDirectory()) // fi���ļ���,����������˴���һ����Ϣ
			{
				ts = "/]0f" + (f1.getPath().replace("e:\\1\\", ""));// "/]0f"���ڱ�ʾ������Ϣ���������ļ�������
				b = ts.getBytes();
				dos.write(b);
				dos.flush();
				dis.read();
				transmit(f1);// ����f1���ļ���(��Ŀ¼),������������п��ܻ����ļ������ļ���,���Խ��еݹ�
			} else {
				fis = new FileInputStream(f1);
				ts = "/]0c" + (f1.getPath().replace("e:\\1\\", ""));// ͬ��,��ʾ����һ���ļ�������
				b = ts.getBytes();
				dos.write(b);
				dos.flush();
				dis.read();
				dos.writeInt(fis.available());// ����һ������ֵ,ָ����Ҫ������ļ��Ĵ�С
				dos.flush();
				dis.read();
				b = new byte[10000];
				while (fis.available() > 0)// ��ʼ�����ļ�
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
