#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>
#include <math.h>
#include <sys/time.h>
#include <memory>

#include "types.h"
#include "java/lang/String.h"
#include "libcore/Native.h"

void java_lang_Object::__init__() {
    return;
}
bool java_lang_Object::equals(java_lang_Object* p0) {
    return this != p0;
}

int32 java_lang_Object::hashCode() {
    return 0;
 }

java_lang_Class* java_lang_Object::getClass() {
    java_lang_Class* clazz = NULL;
    return clazz;
}

java_lang_String* java_lang_Object::toString() {
    return cstr_to_JavaString(L"ObjectInstance");
}

java_lang_String* cstr_to_JavaString(const wchar_t* str) {
    int len = (int)wcslen(str);
    java_lang_String* str2 = new java_lang_String();
    str2->__init__(new (Array<wchar_t>)(str, len));
    return str2;
}

java_lang_String* cstr_byte_to_JavaString(const char* str) {
    int len = (int)strlen(str);
    Array< wchar_t >* array = new (Array< wchar_t >)(len);
    for (int n = 0; n < len; n++) array->set(n, str[n]);

    java_lang_String* str2 = new java_lang_String();
    str2->__init__(array);
    return str2;
}

wchar_t* JavaString_to_cstr_wide(java_lang_String* str) {
    int len = (int)str->length();
    wchar_t* bytes = new wchar_t[len + 1];
    bytes[len] = 0;
    for (int n = 0; n < len; n++) bytes[n] = (wchar_t)str->charAt(n);
    return bytes;
}

char* JavaString_to_cstr_byte(java_lang_String* str) {
    // @TODO: free
    int len = (int)str->length();
    char* bytes = new char[len + 1];
    bytes[len] = 0;
    for (int n = 0; n < len; n++) bytes[n] = (char)str->charAt(n);
    return bytes;
}

int32 cmp(int64 l, int64 r) { return (l == r) ? 0 : ((l < r) ? -1 : +1); }
int32 cmp(float32 l, float32 r) { return (l == r) ? 0 : ((l < r) ? -1 : +1); }
int32 cmp(float64 l, float64 r) { return (l == r) ? 0 : ((l < r) ? -1 : +1); }

int32 cmpl(float32 l, float32 r) { return (isnan(l) || isnan(r)) ? -1 : cmp(l, r); }
int32 cmpg(float32 l, float32 r) { return (isnan(l) || isnan(r)) ? +1 : cmp(l, r); }
int32 cmpl(float64 l, float64 r) { return (isnan(l) || isnan(r)) ? -1 : cmp(l, r); }
int32 cmpg(float64 l, float64 r) { return (isnan(l) || isnan(r)) ? +1 : cmp(l, r); }
