package cn.sh.ae;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

/**
 * 
 * ��jna������c����ƥ�䣬Ȼ����java���ʹ�����Ӧ���͵�ֵ
 * 
 * example---------------c��char* argv[]������java��String[] argȡ��
 * ------------------------------ //C ���� void fill_buffer(int *buf, int len);
 * void fill_buffer(int buf[], int len); // same thing with array syntax
 * //jnaӳ������ void fill_buffer(int[] buf, int len);
 * 
 * jnaӳ��ṹ�Ķ���----------------------------------------------------------------------
 * public static class SYSTEMTIME extends Structure { public short wYear; public
 * short wMonth; public short wDayOfWeek; public short wDay; public short wHour;
 * public short wMinute; public short wSecond; public short wMilliseconds; }
 * ����ṹ�Ĳ��� void GetSystemTime(SYSTEMTIME result);
 * 
 * 
 * ָ���ӳ��͵���----------------------------------------- // C ���� void
 * allocate_buffer(char **bufp, int* lenp); // JNA ӳ�� void
 * allocate_buffer(PointerByReference bufp, IntByReference lenp); //java����
 * PointerByReference pref = new PointerByReference(); IntByReference iref = new
 * IntByReference(); lib.allocate_buffer(pref, iref); Pointer p =
 * pref.getValue(); byte[] buffer = p.getByteArray(0, iref.getValue());
 * 
 * 
 * ָ�뺯����ӳ��͵���----------------------------------------- // C ���� typedef void
 * (*sig_t)(int); sig_t signal(sig_t); // JNA ӳ�� public interface CLibrary
 * extends Library { int SIGUSR1 = 30; interface sig_t extends Callback { void
 * invoke(int signal); } sig_t signal(int sig, sig_t fn); int raise(int sig); }
 * //java���� CLibrary lib = (CLibrary)Native.loadLibrary("c", CLibrary.class); //
 * WARNING: you must keep a reference to the callback object // until you
 * deregister the callback; if the callback object // is garbage-collected, the
 * native callback invocation will // probably crash. CLibrary.sig_t fn = new
 * CLibrary.sig_t() { public void invoke(int sig) { System.out.println("signal " +
 * sig + " was raised"); } }; CLibrary.sig_t old_handler =
 * lib.signal(CLibrary.SIGUSR1, fn); lib.raise(CLibrary.SIGUSR1);
 * 
 * //c�е���win����api ���� ��ӳ���ʹ��----------------------------- // C ���� typedef int
 * (__stdcall *WNDENUMPROC)(void*,void*); int __stdcall
 * EnumWindows(WNDENUMPROC,void*);
 * 
 * //JNA ӳ�� public interface User32 extends StdCallLibrary { interface
 * WNDENUMPROC extends StdCallCallback { // Return whether to continue
 * enumeration. boolean callback(Pointer hWnd, Pointer arg); } boolean
 * EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg); } //java���� User32 user32 =
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
