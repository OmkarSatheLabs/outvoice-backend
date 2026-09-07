//package com.omkarsathe.outvoice.workspace.customer;
//
//import com.omkarsathe.outvoice.common.entity.Auditable;
//import com.omkarsathe.outvoice.workspace.Workspace;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.Filter;
//
//import java.util.UUID;
//
//@Entity
//@Table(
//        name = "workspace_customers",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_workspace_customer_email",
//                        columnNames = {"workspace_id", "email"}
//                )
//        }
//)
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Filter(name = "deletedFilter")
//public class WorkspaceCustomerEntity extends Auditable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "workspace_id", nullable = false)
//    private Workspace workspace;
//
//    @Column(name = "display_name", nullable = false)
//    private String displayName;
//
//    @Column(name = "email")
//    private String email;
//
//    @Column(name = "mobile")
//    private String mobile;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "linked_workspace_id")
//    private Workspace linkedWorkspace;
//
//    @Column(name = "default_currency", nullable = false, length = 3)
//    private String defaultCurrency;
//
//    @Column(name = "is_deleted", nullable = false)
//    @Builder.Default
//    private boolean isDeleted = false;
//
//}
