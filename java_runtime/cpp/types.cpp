#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>
#include <sys/time.h>

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

wchar_t* JavaString_to_cstr_wide(java_lang_String*  str) {
    int len = str->length();
    wchar_t* bytes = new wchar_t[len + 1];
    bytes[len] = 0;
    for (int n = 0; n < len; n++) bytes[n] = (wchar_t)str->charAt(n);
    return bytes;
}

char* JavaString_to_cstr_byte(java_lang_String*  str) {
    // @TODO: free
    int len = str->length();
    char* bytes = new char[len + 1];
    bytes[len] = 0;
    for (int n = 0; n < len; n++) bytes[n] = (char)str->charAt(n);
    return bytes;
}
