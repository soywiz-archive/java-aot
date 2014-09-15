#ifndef types_h_def
#define types_h_def
#include <stdio.h>
typedef int int32;
typedef long long int int64;

class java_lang_String;

template <class T> class Array {
public:
    T* items;
    int len;

    Array(T* items, int len) {
        this->items = items;
        this->len = len;
    }
    Array(const T* items, int len) {
        this->items = (T*)items;
        this->len = len;
    }

    T operator[](int index) {
        return items[index];
    }
};

java_lang_String* cstr_to_JavaString(const wchar_t* str);

#endif