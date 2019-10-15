package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.LicensesRepository;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.model.License;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.NotFoundException;

import java.util.UUID;

@Slf4j
@Service
public class LicenseService {

    private final LicensesRepository licensesRepository;
    private final RolesRepository rolesRepository;
    private final UserService userService;

    @Autowired
    public LicenseService(RolesRepository rolesRepository, LicensesRepository licensesRepository,
                          UserService userService) {
        this.licensesRepository = licensesRepository;
        this.rolesRepository = rolesRepository;
        this.userService = userService;
    }

    public License getLicense(String userIdentifier) {
        User user = userService.getUserByName(userIdentifier);
        return licensesRepository.findByUser(user);
    }

    public void deleteLicense(String userIdentifier) {
        User user = userService.getUserByName(userIdentifier);
        licensesRepository.deleteByUser(user);
    }

    public License updateLicense(String userIdentifier, String role) {
        User user = userService.getUserByName(userIdentifier);
        Role dbRole = rolesRepository.findByRole(role);
        checkRole(role, dbRole);

        License existingLicense = licensesRepository.findByUser(user);
        existingLicense.setRole(dbRole);
        existingLicense.setLicense(generateLicense());
        return licensesRepository.save(existingLicense);
    }


    public License addLicenseToUser(String username, String role) {
        User user = userService.getUserByName(username);
        Role dbRole = rolesRepository.findByRole(role);
        checkRole(role, dbRole);
        License license = License.builder().license(generateLicense()).user(user).build();
        return licensesRepository.save(license);
    }

    private String generateLicense() {
        return UUID.randomUUID().toString();
    }

    private void checkRole(String role, Role dbRole) {
        if (dbRole == null) {
            throw new NotFoundException(String.format("Failed to get defined role: %s", role));
        }
    }
}
