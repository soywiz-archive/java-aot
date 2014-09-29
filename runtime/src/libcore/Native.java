package libcore;

@CPPClass(header =
    "#include <wchar.h>\n" +
    "#include <sys/time.h>\n" +
    "#include <stdio.h>\n" +
    "#include <math.h>\n" +
    "#include <stdlib.h>\n"
)
public class Native {
     @CPPMethod("wchar_t libcore_Native::lower(wchar_t v) { return ::towlower(v); }") static public native char lower(char v);
	//static public char lower(char v) { AOT.cpp("return ::towlower(v);"); return 0; }

    @CPPMethod("wchar_t libcore_Native::upper(wchar_t v) { return ::towupper(v); }")
	static public native char upper(char v);

    @CPPMethod("float64 libcore_Native::atan(float64 v) { return ::atan(v); }")
    static public native double atan(double v);

    @CPPMethod("float64 libcore_Native::cos(float64 v) { return ::cos(v); }")
    static public native double cos(double v);

    @CPPMethod("float64 libcore_Native::sin(float64 v) { return ::sin(v); }")
    static public native double sin(double v);

    @CPPMethod("float32 libcore_Native::intBitsToFloat(int32 v) { return *(float32*)&v; }")
    static public native float intBitsToFloat(int v);

    @CPPMethod("bool libcore_Native::isNaN(float32 v) { return ::isnan(v); }")
    static public native boolean isNaN(float v);

    @CPPMethod("void libcore_Native::putchar(wchar_t v) { ::putwchar(v); }")
    static public native void putchar(char v);

    @CPPMethod("void libcore_Native::flush() { ::fflush(::stdout); }")
    static public native void flush();

    @CPPMethod("void libcore_Native::debugint(int v) { ::printf(\"Int(%d)\", v); }")
    static public native void debugint(int v);

    @CPPMethod("void libcore_Native::exit(int32 status) { ::exit(status); }")
    static public native void exit(int status);

    @CPPMethod(
        "void libcore_Native::arraycopy(java_lang_Object* src, int srcOfs, java_lang_Object* dest, int destOfs, int len) {\n"+
        "    if (src == NULL) return;\n"+
        "    if (dest == NULL) return;\n"+
        "    //Array<int> *src_int = dynamic_cast< Array<int>*>(src);\n"+
        "    //Array<int> *dest_int = dynamic_cast< Array<int>*>(dest);\n"+
        "    //if ((src_int != NULL) && (dest_int != NULL)) {\n"+
        "    //    printf(\"Copying integer list!\\n\");\n"+
        "    //} else {\n"+
        "        ArrayBase *_src = (ArrayBase *)src;\n"+
        "        ArrayBase *_dest = (ArrayBase *)dest;\n"+
        "        for (int n = 0; n < len; n++) {\n"+
        "            _dest->__set_object(destOfs + n, _dest->__get_object(srcOfs + n));\n"+
        "        }\n"+
        "    //}\n"+
        "}\n"
    )
	static public native void arraycopy(Object src, int srcOfs, Object dest, int destOfs, int len);

    @CPPMethod("void libcore_Native::gc() { }")
	static public native void gc();

    @CPPMethod("int64 libcore_Native::currentTimeMillis() { struct timeval tp; ::gettimeofday(&tp, NULL); int64 result = (int64)tp.tv_sec * 1000L + (int64)tp.tv_usec / 1000L; return result; }")
	static public native long currentTimeMillis();
}
