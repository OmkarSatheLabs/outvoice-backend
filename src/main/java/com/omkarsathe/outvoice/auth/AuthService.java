package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.country.CountryRepository;
//import com.omkarsathe.outvoice.country.CountryService;
//import com.omkarsathe.outvoice.currency.CurrencyEntity;
//import com.omkarsathe.outvoice.currency.CurrencyRepository;
//import com.omkarsathe.outvoice.currency.CurrencyService;
import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.mail.MailType;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.phone.PhoneCodeService;
import com.omkarsathe.outvoice.security.JwtService;
//import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
//import com.omkarsathe.outvoice.user.UserService;
//import com.omkarsathe.outvoice.user.dto.CreateUserRequestDto;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceService;
//import com.omkarsathe.outvoice.user.workspace.dto.CreateUserWorkspaceRequestDto;
import com.omkarsathe.outvoice.workspace.*;
//import com.omkarsathe.outvoice.workspace.dto.CreateWorkspaceRequestDto;
//import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import com.omkarsathe.outvoice.workspace.member.MemberService;
import com.omkarsathe.outvoice.workspace.user.CreateUser;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserResponse;
import com.omkarsathe.outvoice.workspace.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PhoneCodeService phoneCodeService;
//    private final CountryService countryService;
//    private final UserService userService;
//    private final WorkspaceService workspaceService;
//    private final CurrencyService currencyService;
//    private final UserWorkspaceService userWorkspaceService;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
//    private final UserWorkspaceRepository userWorkspaceRepository;
//    private final CountryRepository countryRepository;
//    private final CurrencyRepository currencyRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Logger logger = Logger.getLogger(AuthService.class.getName());
    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final MemberService memberService;
    private final MailService mailService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        logger.info("signup request: " + request);

        CreateUser createUser = new CreateUser(
                request.user().email(),
                request.user().phoneCode(),
                request.user().mobile(),
                request.user().fullName(),
                request.user().password()
        );
        UserResponse user = userService.create(createUser);

        CreateWorkspace createWorkspace = new CreateWorkspace(
                request.workspace().name()
        );
        WorkspaceResponse workspace = workspaceService.create(createWorkspace);

        memberService.create(workspace.id(), user.id());

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", user.fullName());
        MailRequest mailRequest = new MailRequest(user.email(), MailType.WELCOME, variables);
        mailService.queue(mailRequest);

        return new AuthResponse(jwtService.generateToken(user.id().toString()));
    }

    public AuthResponse login(LoginRequest request) {
        final String errorMessage = "Invalid email address/mobile number or password";

        Optional<User> userOptional = Optional.empty();

        if (StringUtils.hasText(request.email()) && StringUtils.hasText(request.password())) {
            userOptional = userRepository.findByEmail(request.email());
        } else if (request.phoneCode() != null && StringUtils.hasText(request.mobile())) {
            String phoneCode = request.phoneCode().replaceAll("\\s+", "");
            String mobile = request.mobile().replaceAll("\\s+", "");
            userOptional =
                    userRepository.findByMobileAndPhoneCode_Code(mobile, phoneCode);
        } else {
            throw new BadCredentialsException(errorMessage);
        }

        User user = userOptional.orElseThrow(() -> new BadCredentialsException(errorMessage));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(errorMessage);
        }

        return new AuthResponse(jwtService.generateToken(user.getUsername()));
    }
}
