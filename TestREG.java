package cn.sh.ae;

import java.io.IOException;

public class TestREG {

	public static void main(String[] args) {
		String cmd = "cmd /c \"regedit /s D://RCCU_KS_ADV.reg\"";
		try {
			Process ps = Runtime.getRuntime().exec(cmd);
			System.out.println(ps.getInputStream());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

}
