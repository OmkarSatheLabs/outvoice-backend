package com.omkarsathe.outvoice.phone;

//import com.omkarsathe.outvoice.entity.CountryPhoneCode;
//import com.omkarsathe.outvoice.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "phone_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    // status

//    @OneToMany(mappedBy = "phoneCode", fetch = FetchType.LAZY)
//    @Builder.Default
//    private List<CountryPhoneCode> countryMappings = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by", nullable = false, updatable = false)
//    private UserEntity createdBy;

    @UpdateTimestamp
    @Column()
    private LocalDateTime updatedAt;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "updated_by")
//    private UserEntity updatedBy;

    @Column()
    private LocalDateTime deletedAt;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "deleted_by")
//    private UserEntity deletedBy;
}
