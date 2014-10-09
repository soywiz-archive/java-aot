wchar_t libcore_Native::lower(wchar_t v) { return ::towlower(v); }

wchar_t libcore_Native::upper(wchar_t v) { return ::towupper(v); }

void libcore_Native::putchar(wchar_t v) { ::putwchar(v); }
float64 libcore_Native::atan(float64 v) { return ::atan(v); }
float64 libcore_Native::cos(float64 v) { return ::cos(v); }
float64 libcore_Native::sin(float64 v) { return ::sin(v); }
float32 libcore_Native::intBitsToFloat(int32 v) { return *(float32*)&v; }
bool libcore_Native::isNaN(float32 v) { return isnan(v); }
void libcore_Native::flush() { ::fflush(stdout); }
void libcore_Native::debugint(int v) { ::printf(\"Int(%d)\", v); }
void libcore_Native::exit(int32 status) { ::exit(status); }

void libcore_Native::arraycopy(java_lang_Object* src, int srcOfs, java_lang_Object* dest, int destOfs, int len) {
    if (src == NULL) return;
    if (dest == NULL) return;
    //Array<int> *src_int = dynamic_cast< Array<int>*>(src);
    //Array<int> *dest_int = dynamic_cast< Array<int>*>(dest);
    //if ((src_int != NULL) && (dest_int != NULL)) {
    //    printf(\"Copying integer list!\\n\");
    //} else {
        ArrayBase *_src = (ArrayBase *)(java_lang_Object*)src;
        ArrayBase *_dest = (ArrayBase *)(java_lang_Object*)dest;
        for (int n = 0; n < len; n++) {
            _dest->__set_object(destOfs + n, _dest->__get_object(srcOfs + n));
        }
    //}
}
void libcore_Native::gc() { }
int64 libcore_Native::currentTimeMillis() { struct timeval tp; ::gettimeofday(&tp, NULL); int64 result = (int64)tp.tv_sec * 1000L + (int64)tp.tv_usec / 1000L; return result; }
int32 libcore_Native::getTimerTime() { return (int32)libcore_Native::currentTimeMillis(); }
