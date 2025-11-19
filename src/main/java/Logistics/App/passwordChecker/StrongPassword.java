package Logistics.App.passwordChecker;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Password must be at least 8 chars long, contain one uppercase, one number, and one special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
