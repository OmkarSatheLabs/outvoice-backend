package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.auth.ContactRequest;
import com.omkarsathe.outvoice.auth.ValidSignupContact;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ValidSignupContact
public record CreateCustomer(

        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(min = 1, max = 4, message = "Phone code must be between 1 and 4 characters")
        String phoneCode,

        @Size(min = 7, max = 15, message = "Mobile number must be between 7 and 15 characters")
        String mobile,

        @NotBlank(message = "Full name is required")
        @Size(max = 200, message = "Full name must be at most 200 characters")
        String fullName
) implements ContactRequest {
}
