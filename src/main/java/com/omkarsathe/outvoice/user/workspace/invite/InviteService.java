//package com.omkarsathe.outvoice.user.workspace.invite;
//
//import com.omkarsathe.outvoice.phone.PhoneCodeService;
////import com.omkarsathe.outvoice.user.UserEntity;
////import com.omkarsathe.outvoice.user.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class InviteService {
//
//    private final InviteRepository inviteRepository;
//
//    private final PhoneCodeService phoneCodeService;
////    private final UserService userService;
////
////    public InviteResponse createInvite(UUID workspaceId, @Valid CreateInviteRequest request, UUID createdBy) {
////
////        InviteEntity invite = new InviteEntity();
////        try {
//////            UserEntity user = userService.findByEmailIgnoreCaseOrPhoneCodeIdAndMobileThrows();
//////            invite.setUserId(user);
////        } catch (Exception ex) {
////            System.out.println(ex.getMessage());
////        }
////
////        invite.setEmail(request.email());
////        invite.setPhoneCodeId(phoneCodeService.findById(request.phoneCodeId()));
////        invite.setMobile(request.mobile());
////        invite.setInvitedBy(userService.findById(createdBy));
////        return new InviteResponse();
////    }
//
//    public List<InviteResponse> getInvites(UUID workspaceId) {
//        return null;
//    }
//}
