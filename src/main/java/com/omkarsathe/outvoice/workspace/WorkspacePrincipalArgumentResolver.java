//package com.omkarsathe.outvoice.workspace;
//
//import com.omkarsathe.outvoice.common.exception.ForbiddenException;
//import com.omkarsathe.outvoice.common.security.SecurityUtils;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
////import com.omkarsathe.outvoice.user.workspace.WorkspaceMembershipService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.MethodParameter;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//import org.springframework.web.servlet.HandlerMapping;
//
//import java.util.Map;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//public class WorkspacePrincipalArgumentResolver implements HandlerMethodArgumentResolver {
//
////    private final UserWorkspaceRepository userWorkspaceRepository;
////    private final WorkspaceMembershipService membershipService;
//
//    @Override
//    public boolean supportsParameter(MethodParameter parameter) {
//        return parameter.hasParameterAnnotation(CurrentWorkspaceUser.class)
//                && parameter.getParameterType().equals(WorkspacePrincipal.class);
//    }
//
////    @Override
////    public Object resolveArgument(
////            MethodParameter parameter,
////            ModelAndViewContainer mavContainer,
////            NativeWebRequest webRequest,
////            WebDataBinderFactory binderFactory) {
////
////        UUID currentUserId = SecurityUtils.getCurrentUserId();
////
////        UUID workspaceId = extractWorkspaceIdFromPath(webRequest);
////
////        // 1. Membership check — is this user even part of this workspace?
//////        WorkspacePrincipal principal = membershipService
//////                .findPrincipal(currentUserId, workspaceId)
//////                .orElseThrow(() -> new ForbiddenException("NOT_A_MEMBER", workspaceId.toString()));
////
////        // 2. Permission check — declarative, based on annotation attribute
////        CurrentWorkspaceUser annotation = parameter.getParameterAnnotation(CurrentWorkspaceUser.class);
////        Permission required = annotation.requires();
////
//////        if (required != Permission.NONE && !principal.hasPermission(required)) {
//////            throw new ForbiddenException("PERMISSION_DENIED", required.name());
//////        }
//////
//////        return principal;
////    }
//
//    private UUID extractWorkspaceIdFromPath(NativeWebRequest webRequest) {
//        Map<String, String> pathVars = (Map<String, String>) webRequest.getAttribute(
//                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
//        String raw = pathVars.get("workspaceId");
//        if (raw == null) {
//            throw new IllegalStateException("@CurrentWorkspaceUser requires a {workspaceId} path variable");
//        }
//        return UUID.fromString(raw);
//    }
//}
