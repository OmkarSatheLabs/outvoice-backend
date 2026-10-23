package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.mfa.MfaService;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaResendRequest;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaSetupResponse;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaVerifyRequest;
import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.mail.MailType;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.phone.PhoneCodeService;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.workspace.CreateWorkspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import com.omkarsathe.outvoice.workspace.WorkspaceService;
import com.omkarsathe.outvoice.workspace.member.MemberService;
import com.omkarsathe.outvoice.workspace.user.CreateUser;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
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
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Logger logger = Logger.getLogger(AuthService.class.getName());
    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final MemberService memberService;
    private final MailService mailService;
    private final MfaService mfaService;

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

        return AuthResponse.ofToken(jwtService.generateToken(user.id().toString()));
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

        if (user.isMfaEnabled()) {
            return mfaService.initiateLoginChallenge(user);
        }

        return AuthResponse.ofToken(jwtService.generateToken(user.getUsername()));
    }

    public AuthResponse verifyMfa(MfaVerifyRequest request) {
        return mfaService.verifyLoginChallenge(request);
    }

    public Map<String, String> resendMfa(MfaResendRequest request) {
        return mfaService.resendLoginChallenge(request);
    }

    public MfaSetupResponse fallbackEmailMfa(MfaResendRequest request) {
        return mfaService.fallbackToEmail(request);
    }
}
