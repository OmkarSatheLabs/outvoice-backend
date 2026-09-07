//package com.omkarsathe.outvoice.user;
//
//import com.omkarsathe.outvoice.common.exception.InvalidRequestException;
//import com.omkarsathe.outvoice.common.exception.UserAlreadyExistsException;
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.country.CountryService;
//import com.omkarsathe.outvoice.phone.PhoneCode;
//import com.omkarsathe.outvoice.phone.PhoneCodeService;
//import com.omkarsathe.outvoice.user.dto.CreateUserRequestDto;
//import io.micrometer.common.util.StringUtils;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    public UserEntity save(CreateUserRequestDto createUserRequestDto) {
//
//        boolean hasEmail = StringUtils.isNotBlank(createUserRequestDto.getEmail());
//        boolean hasPhoneCodeNotMobile = createUserRequestDto.getPhoneCode() != null
//                && StringUtils.isBlank(createUserRequestDto.getMobile());
//        boolean hasMobile = createUserRequestDto.getPhoneCode() != null
//                && StringUtils.isNotBlank(createUserRequestDto.getMobile());
//
//        if (!hasEmail && !hasMobile) {
//            throw new InvalidRequestException("Missing email or phone code and mobile");
//        }
//
//        UserEntity userEntityToSave = new UserEntity();
//
//        if (hasEmail) {
//            String email = createUserRequestDto.getEmail().toLowerCase().trim();
//            if (userRepository.existsByEmail(email)) {
//                throw new UserAlreadyExistsException("User with email " + email + " already exists");
//            }
//            userEntityToSave.setEmail(email);
//        }
//
//        if (hasPhoneCodeNotMobile) {
//            userEntityToSave.setPhoneCode(createUserRequestDto.getPhoneCode());
//        }
//
//        if (hasMobile) {
//            String mobile = createUserRequestDto.getMobile().trim();
//            PhoneCode phoneCode = createUserRequestDto.getPhoneCode();
//            if (userRepository.existsByMobileAndPhoneCodeId(mobile, phoneCode.getId())) {
//                throw new UserAlreadyExistsException("User with mobile " + mobile + " already exists");
//            }
//            userEntityToSave.setPhoneCode(phoneCode);
//            userEntityToSave.setMobile(mobile);
//        }
//
//        userEntityToSave.setFullName(createUserRequestDto.getFullName());
//        userEntityToSave.setPasswordHash(createUserRequestDto.getHashedPassword());
//        userEntityToSave.setIsEmailVerified(false);
//        userEntityToSave.setIsMobileVerified(false);
//        userEntityToSave.setCountry(createUserRequestDto.getCountry());
////        userEntityToSave.setIsPlaceholder(true);
//
//        return userRepository.save(userEntityToSave);
//    }
//
//    public boolean existsById(UUID id) {
//        return userRepository.existsById(id);
//    }
//
//    public UserEntity findByEmail(String email) {
//        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
//    }
//
//    public void assertEmailBelongsToUser(UUID userId, String email) {
//
//        if (!userRepository.existsByIdAndEmailIgnoreCase(userId, email)) {
//            throw new IllegalStateException(
//                    "Email does not belong to user " + userId);
//        }
//    }
//
//    public UserEntity findById(UUID userId) {
//        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User with UUID " + userId + " not found"));
//    }
//
//    /**
//     * Asserts that the given phoneCodeId + mobile combination belongs to the given userId.
//     *
//     * @param userId      the user ID to validate against
//     * @param phoneCodeId the phone/country code ID
//     * @param mobile      the mobile number
//     * @throws InvalidRequestException if the phoneCodeId + mobile does not belong to userId
//     */
//    public void assertMobileBelongsToUser(UUID userId, UUID phoneCodeId, String mobile) {
//        boolean exists = userRepository.existsByIdAndPhoneCodeIdAndMobile(
//                userId, phoneCodeId, mobile
//        );
//
//        if (!exists) {
//            throw new InvalidRequestException(
//                    String.format("Mobile %s with phoneCodeId %s does not belong to user %s",
//                            mobile, phoneCodeId, userId)
//            );
//        }
//    }
//}
