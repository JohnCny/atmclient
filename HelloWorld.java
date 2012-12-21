package cn.sh.ae;

import com.sun.jna.Library;
import com.sun.jna.Native;

/** Simple example of native library declaration and usage. */

public class HelloWorld {

	public interface CLibrary extends Library {

		CLibrary INSTANCE = (CLibrary)

		Native.loadLibrary("msvcrt",

		CLibrary.class);

		void printf(String format, Object... args);

	}

	public static void main(String[] args) {

		CLibrary.INSTANCE.printf("Hello, World\n");

		for (int i = 0; i < args.length; i++) {

			CLibrary.INSTANCE.printf("Argument %d: %s\n", i, args[i]);

		}

	}

}
