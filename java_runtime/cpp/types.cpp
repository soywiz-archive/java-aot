#include <wchar.h>
#include <wchar.h>

#include "types.h"
#include "java_lang_String.h"
#include "libcore_Native.h"

void java_lang_Object::__init__() { java_lang_Object* local0;
    local0 = this;
    return;
}
bool java_lang_Object::equals(java_lang_Object* p0) {
    return this != p0;
}

int32 java_lang_Object::hashCode() {
    return 0;
 }

java_lang_Class* java_lang_Object::getClass() {
    return NULL;
}

java_lang_String* java_lang_Object::toString() {
    return cstr_to_JavaString(L"ObjectInstance");
}

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

void libcore_Native::gc() {
}

int64 libcore_Native::currentTimeMillis() {
    return 0L;
}

void libcore_Native::arraycopy(java_lang_Object* src, int srcOfs, java_lang_Object* dest, int destOfs, int len) {
    if (src == NULL) return;
    if (dest == NULL) return;
    //Array<int> *src_int = dynamic_cast< Array<int>*>(src);
    //Array<int> *dest_int = dynamic_cast< Array<int>*>(dest);
    //if ((src_int != NULL) && (dest_int != NULL)) {
    //    printf("Copying integer list!\n");
    //} else {
        ArrayBase *_src = (ArrayBase *)src;
        ArrayBase *_dest = (ArrayBase *)dest;
        for (int n = 0; n < len; n++) {
            _dest->__set_object(destOfs + n, _dest->__get_object(srcOfs + n));
        }
    //}
}
