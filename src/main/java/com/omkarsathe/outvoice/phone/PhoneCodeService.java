package com.omkarsathe.outvoice.phone;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneCodeService {

    private final PhoneCodeRepository phoneCodeRepository;

    @Transactional(readOnly = true)
    public List<PhoneCodeDto> getAll() {
        return phoneCodeRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(p -> new PhoneCodeDto(p.getId(), p.getCode()))
                .toList();
    }

    public PhoneCode findByCode(String code) {
        return phoneCodeRepository.findByCode(code);
    }

    public PhoneCode findById(UUID phoneCodeId) {
        return phoneCodeRepository.findById(phoneCodeId).orElseThrow(() -> new RuntimeException("Phone code with UUID " + phoneCodeId + " not found"));
    }
}
