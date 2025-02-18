package net.wasys.getdoc.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para facilitar a validação de parâmetros esperados nas requests http. 
 * 
 * @author jonas.baggio@wasys.com.br
 * @create 25 de jul de 2018 09:09:15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotNull {
	
	String messageKey() default "";
	String nomeCampo() default "";

}