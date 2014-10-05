#ifndef types_h_def
#define types_h_def
#include <stdio.h>
#include <stdlib.h>
#include <memory>

typedef signed char int8;
typedef signed short int16;
typedef signed int int32;
typedef signed long long int int64;
typedef float float32;
typedef double float64;

class java_lang_String;
class java_lang_Object;
class java_lang_Class;

class java_lang_Object {
    public: void __init__();
    public: bool equals(java_lang_Object* p0);
    public: int32 hashCode();
    public: java_lang_Class* getClass();
    public: java_lang_String* toString();
};

class ArrayBase : public java_lang_Object {
public:
    virtual void __set_object(int index, void* value) = 0;
    virtual void* __get_object(int index) = 0;
};

template <class T> class Array : public ArrayBase {
public:
    T* items;
    int len;
    int alloc;

    Array(int len) {
        int len1 = len + 1;
        this->items = new T[len1];
        this->alloc = 1;
        this->len = len;
        memset(this->items, 0, len1 * sizeof(T));
    }

    ~Array() {
        if (alloc) {
            delete this->items;
            this->items = NULL;
            this->len = 0;
            this->alloc = 0;
        }
    }

    Array(T* items, int len) {
        this->items = items;
        this->len = len;
        this->alloc = 0;
    }
    Array(const T* items, int len) {
        this->items = (T*)items;
        this->len = len;
        this->alloc = 0;
    }

    int size() { return len; }

    void set(int index, T value) { items[index] = value; }
    T& get(int index) {
        if (index < 0 || index >= len) {
            fprintf(stderr, "Array access out of bounds index(%d) bounds(0, %d)", index, len);
            abort();
        }
        return items[index];
    }

    T& operator[](int index) { return items[index]; }
    void __set_object(int index, void* value) { items[index] = (T)(int64)(void *)(value); }
    void* __get_object(int index) { return (void *)(int64)items[index]; }
};

java_lang_String* cstr_to_JavaString(const wchar_t* str);
java_lang_String* cstr_byte_to_JavaString(const char* str);
wchar_t* JavaString_to_cstr(java_lang_String* str);
char* JavaString_to_cstr_byte(java_lang_String* str);

int32 cmp(int64 l, int64 r);

int32 cmpl(float32 l, float32 r);
int32 cmpg(float32 l, float32 r);

int32 cmpl(float64 l, float64 r);
int32 cmpg(float64 l, float64 r);

#endif