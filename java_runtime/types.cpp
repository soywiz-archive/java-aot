#include <wchar.h>

#include "types.h"
#include "java_lang_String.h"
#include "java_lang_SystemOut.h"

java_lang_String* cstr_to_JavaString(const wchar_t* str) {
    //Array<wchar_t>* p0
    int str_len = wcslen(str);
    java_lang_String* str2 = new java_lang_String();
    str2->__init__(new (Array<wchar_t>)(str, str_len));
    return str2;
}

void java_lang_SystemOut::println(java_lang_String* str) {
    for (int n = 0; n < str->chars->len; n++) putchar(str->chars->items[n]);
    putchar('\n');
}