package com.stanfy.enroscar.rest.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates models.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {

  /** @return wrapper class */
  Class<?> wrapper() default Model.class;

  /** @return analyzer name */
  String analyzer() default "";

}
