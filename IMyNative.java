package cn.sh.ae;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

/**
 * 
 * 将jna类型与c类型匹配，然后用java类型传入相应类型的值
 * 
 * example---------------c中char* argv[]可以由java中String[] arg取代
 * ------------------------------ //C 类型 void fill_buffer(int *buf, int len);
 * void fill_buffer(int buf[], int len); // same thing with array syntax
 * //jna映射类型 void fill_buffer(int[] buf, int len);
 * 
 * jna映射结构的定义----------------------------------------------------------------------
 * public static class SYSTEMTIME extends Structure { public short wYear; public
 * short wMonth; public short wDayOfWeek; public short wDay; public short wHour;
 * public short wMinute; public short wSecond; public short wMilliseconds; }
 * 传入结构的参数 void GetSystemTime(SYSTEMTIME result);
 * 
 * 
 * 指针的映射和调用----------------------------------------- // C 类型 void
 * allocate_buffer(char **bufp, int* lenp); // JNA 映射 void
 * allocate_buffer(PointerByReference bufp, IntByReference lenp); //java调用
 * PointerByReference pref = new PointerByReference(); IntByReference iref = new
 * IntByReference(); lib.allocate_buffer(pref, iref); Pointer p =
 * pref.getValue(); byte[] buffer = p.getByteArray(0, iref.getValue());
 * 
 * 
 * 指针函数的映射和调用----------------------------------------- // C 类型 typedef void
 * (*sig_t)(int); sig_t signal(sig_t); // JNA 映射 public interface CLibrary
 * extends Library { int SIGUSR1 = 30; interface sig_t extends Callback { void
 * invoke(int signal); } sig_t signal(int sig, sig_t fn); int raise(int sig); }
 * //java调用 CLibrary lib = (CLibrary)Native.loadLibrary("c", CLibrary.class); //
 * WARNING: you must keep a reference to the callback object // until you
 * deregister the callback; if the callback object // is garbage-collected, the
 * native callback invocation will // probably crash. CLibrary.sig_t fn = new
 * CLibrary.sig_t() { public void invoke(int sig) { System.out.println("signal " +
 * sig + " was raised"); } }; CLibrary.sig_t old_handler =
 * lib.signal(CLibrary.SIGUSR1, fn); lib.raise(CLibrary.SIGUSR1);
 * 
 * //c中调用win本地api 函数 的映射和使用----------------------------- // C 类型 typedef int
 * (__stdcall *WNDENUMPROC)(void*,void*); int __stdcall
 * EnumWindows(WNDENUMPROC,void*);
 * 
 * //JNA 映射 public interface User32 extends StdCallLibrary { interface
 * WNDENUMPROC extends StdCallCallback { // Return whether to continue
 * enumeration. boolean callback(Pointer hWnd, Pointer arg); } boolean
 * EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg); } //java调用 User32 user32 =
 * User32.INSTANCE; user32.EnumWindows(new WNDENUMPROC() { int count; public
 * boolean callback(Pointer hWnd, Pointer userData) { System.out.println("Found
 * window " + hWnd + ", total " + ++count); return true; } }, null);
 * 
 * 
 */
public interface IMyNative extends StdCallLibrary {

	IMyNative imn = (IMyNative) Native.loadLibrary("dll_getStatus",
			IMyNative.class);

	String sDll_getStatus();

	// int sDll_getStatus();
}
