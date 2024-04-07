package com.cy.common.vaild;

import java.lang.annotation.*;

@Documented
@javax.validation.Constraint(validatedBy = {ListValueConstraintValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {
    String message() default "{com.cy.common.vaild.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends javax.validation.Payload>[] payload() default {};

    int[] vals() default {};
}
