package com.cy.common.vaild;

@java.lang.annotation.Documented
@javax.validation.Constraint(validatedBy = {ListValueConstraintValidator.class})
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.TYPE_USE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ListValue {
    String message() default "{com.cy.common.vaild.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends javax.validation.Payload>[] payload() default {};

    int[] vals() default {};
}
