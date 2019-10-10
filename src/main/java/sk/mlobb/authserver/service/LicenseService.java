package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.LicensesRepository;
import sk.mlobb.authserver.model.License;
import sk.mlobb.authserver.model.LicenseType;
import sk.mlobb.authserver.model.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
public class LicenseService {

    private final LicensesRepository licensesRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public LicenseService(PasswordEncoder passwordEncoder, LicensesRepository licensesRepository,
                          UserService userService) {
        this.licensesRepository = licensesRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    public List<License> getAllLicenses() {
        return licensesRepository.findAll();
    }

    public License getLicense(String hash) {
        return licensesRepository.findByLicense(hash);
    }

    public License generate(LicenseType licenseType, String username) throws NotFoundException {
        final String encodedLicense = passwordEncoder.encode(getLicenseInput(licenseType, username));
        final String extractedRawLicense = extractLicense(encodedLicense);
        final String license = splitEncodedLicense(extractedRawLicense);
        return License.builder()
                .license(license)
                .licenseType(licenseType)
                .build();
    }

    private String splitEncodedLicense(String license) {
        String rawLicense = license.substring(0, 23);
        int totalLength = rawLicense.length();
        int halfIndex = totalLength / 2;
        int thirdIndex = halfIndex / 2;
        return rawLicense.substring(0, thirdIndex) + "-"
                + rawLicense.substring(thirdIndex, thirdIndex * 2) + "-"
                + rawLicense.substring(thirdIndex * 2, thirdIndex * 3) + "-"
                + rawLicense.substring(thirdIndex * 3, totalLength);
    }

    private String extractLicense(final String encodedString) {
        StringBuilder rawLicenseBuilder = new StringBuilder();
        for (char charOrNumber : encodedString.toCharArray()) {
            if (Character.isLetterOrDigit(charOrNumber)) {
                rawLicenseBuilder.append(charOrNumber);
            }
        }
        return rawLicenseBuilder.toString();
    }

    private String getLicenseInput(LicenseType licenseType, String username) {
        return String.format("%s-%s", licenseType.getType(), username);
    }
}
