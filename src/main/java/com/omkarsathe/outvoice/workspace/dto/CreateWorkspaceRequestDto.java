//package com.omkarsathe.outvoice.workspace.dto;
//
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.currency.CurrencyEntity;
//import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.workspace.WorkspaceStatus;
//import com.omkarsathe.outvoice.workspace.WorkspaceType;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class CreateWorkspaceRequestDto {
//
//    private String name;
//
//    private String slug;
//
//    private WorkspaceType type = WorkspaceType.STANDARD;
//
//    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;
//
//    private String baseCurrency;
//
//    private CurrencyEntity currency;
//
//    private String gstin;
//
//    private String defaultPlaceOfSupply = null;
//
//    private boolean gstRegistered = false;
//
//    private boolean claimed = true;
//
//    private String billingAddress;
//
//    private Country country;
//
//    private String supportEmail;
//
//    private String supportPhone;
//
//    private String logoUrl;
//
//    private String invoiceNumberPrefix;
//
//    private UserEntity createdBy;
//
//}
