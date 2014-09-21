package libcore;

//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.SOURCE)
public @interface CPP {
    String header() default "";
    String framework() default "";
}
