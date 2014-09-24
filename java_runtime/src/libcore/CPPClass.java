package libcore;

//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.SOURCE)
public @interface CPPClass {
    String header() default "";
    String framework() default "";
    String library() default "";
    String cflags() default "";
}
