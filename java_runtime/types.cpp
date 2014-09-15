#include <wchar.h>

#include "types.h"
#include "java_lang_String.h"
#include "libcore_Native.h"

java_lang_String* cstr_to_JavaString(const wchar_t* str) {
    //Array<wchar_t>* p0
    int str_len = wcslen(str);
    java_lang_String* str2 = new java_lang_String();
    str2->__init__(new (Array<wchar_t>)(str, str_len));
    return str2;
}

wchar_t libcore_Native::upper(wchar_t v) {
    return ::towupper(v);
}

wchar_t libcore_Native::lower(wchar_t v) {
    return ::towlower(v);
}

void libcore_Native::putchar(wchar_t v) {
    ::putchar(v);
}