package cn.sh.ae;

import org.apache.log4j.Logger;

public class TestJNA {
	
	static Logger logger = Logger.getLogger(TestJNA.class.getName());


	public static void main(String[] args) {
		try {
			logger.info("开始获取ATM状态");
//			String[] s = IMyNative.imn.sDll_getStatus();
			logger.info(IMyNative.imn.sDll_getStatus());
//			logger.info("***********************");
//			for (int i = 0; i < s.length; i++) {
//				logger.info(s[i]);
//			}
			logger.info("获取ATM状态结束");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
