package com.example.multimedia.file_upload_api.service;

import com.example.multimedia.file_upload_api.dto.ForgotPasswordDTO;
import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.UserDetailDTO;
import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.SuperAdmin;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.repository.AuthorizationRepository;
import com.example.multimedia.file_upload_api.repository.SuperAdminRepository;
import com.example.multimedia.file_upload_api.repository.UserAuthenticationRepository;
import com.example.multimedia.file_upload_api.repository.UserDetailRepository;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.multimedia.file_upload_api.dto.UserCreationRequestDTO;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.entity.Department;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.EmployeeRepository;
import com.example.multimedia.file_upload_api.repository.DepartmentRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class UserDetailService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ServiceControllerUtils serviceControllerUtils;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;





    
    private SuperAdmin getEffectiveSuperAdmin() {
        try {
            return currentUserService.getCurrentSuperAdmin();
        } catch (Exception e) {
            try {
                UserDetail user = currentUserService.getCurrentUser();
                if (user.getSuperAdmin() != null) {
                    return user.getSuperAdmin();
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
        throw new RuntimeException("Could not determine SuperAdmin context for the current user.");
    }

    @Transactional
    public List<Map<String, Object>> getUsersForCurrentAdmin() {
        SuperAdmin currentSuperAdmin = getEffectiveSuperAdmin();
        List<UserDetail> users = userDetailRepository.findBySuperAdmin(currentSuperAdmin);
        
        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getUserId());
            map.put("email", u.getEmail());
            map.put("firstName", u.getFirstName());
            map.put("lastName", u.getLastName());
            map.put("phoneNumber", u.getPhoneNumber());
            map.put("isActive", u.getIsActive());
            map.put("role", u.getUserType() != null ? u.getUserType().name() : "EMPLOYEE");
            
            // Check if employee exists
            Optional<Employee> empOpt = employeeRepository.findByUserDetail_UserId(u.getUserId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                map.put("employeeCode", emp.getEmployeeCode());
                if (emp.getDepartment() != null) {
                    map.put("deptCode", emp.getDepartment().getDeptCode());
                    map.put("deptName", emp.getDepartment().getDeptName());
                }
            }
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createEmployeeUser(UserCreationRequestDTO dto) {
        SuperAdmin currentSuperAdmin = getEffectiveSuperAdmin();
        
        if (userDetailRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        UserType userType;
        try {
            userType = UserType.valueOf(dto.getRole().toUpperCase());
        } catch (Exception e) {
            userType = UserType.EMPLOYEE;
        }

        UserDetail userDetail = new UserDetail();
        userDetail.setEmail(dto.getEmail());
        userDetail.setPassword(passwordEncoder.encode(dto.getPassword()));
        userDetail.setFirstName(dto.getFirstName());
        userDetail.setLastName(dto.getLastName());
        userDetail.setPhoneNumber(dto.getPhoneNumber());
        userDetail.setSuperAdmin(currentSuperAdmin);
        userDetail.setUserType(userType);
        userDetail.setIsActive(true);
        userDetail.setSignupDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        userDetail = userDetailRepository.save(userDetail);
        
        // Default AuthKey mapping logic based on role
        String authKeyStr = "employee"; // default
        if (userType == UserType.ADMINISTRATOR || userType == UserType.SUPER_ADMIN) authKeyStr = "administrator";
        else if (userType == UserType.PROCUREMENT_MANAGER) authKeyStr = "procurement_manager";
        
        Optional<Authorization> authOpt = authorizationRepository.findByAuthKeyIgnoreCase(authKeyStr);
        if (authOpt.isPresent()) {
            UserAuthentication userAuth = new UserAuthentication();
            userAuth.setUserId(userDetail.getUserId());
            userAuth.setAuthKey(String.valueOf(authOpt.get().getAuthId()));
            userAuth.setIsActive(true);
            userAuthenticationRepository.save(userAuth);
        }
        
        // Create Employee Profile
        Employee employee = new Employee();
        String randomCode = "EMP-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        employee.setEmployeeCode(randomCode);
        employee.setName(dto.getFirstName() + " " + (dto.getLastName() != null ? dto.getLastName() : ""));
        employee.setEmail(dto.getEmail());
        employee.setUserDetail(userDetail);
        
        if (dto.getDeptCode() != null && !dto.getDeptCode().trim().isEmpty()) {
            Optional<Department> deptOpt = departmentRepository.findById(dto.getDeptCode());
            if (deptOpt.isPresent()) {
                employee.setDepartment(deptOpt.get());
            }
        }
        
        employeeRepository.save(employee);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetail.getUserId());
        response.put("employeeCode", employee.getEmployeeCode());
        response.put("message", "User created successfully");
        return response;
    }

    public Map<String, Object> registerUser(UserDetailDTO userDetailDTO) {
        // Validate SuperAdmin
        SuperAdmin superAdmin = superAdminRepository.findById(userDetailDTO.getSuperAdminId())
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));

        Optional<UserDetail> emailUser = userDetailRepository.findByEmail(userDetailDTO.getEmail());
        Optional<UserDetail> phoneUser = userDetailRepository.findByPhoneNumber(userDetailDTO.getPhoneNumber());

        UserDetail userDetail;

        // Case 1: Email or phone exists and belong to different users
        if (emailUser.isPresent() && phoneUser.isPresent() && !emailUser.get().getUserId().equals(phoneUser.get().getUserId())) {
            throw new RuntimeException("Email and phone number belong to different users.");
        }

        String rawPassword = "********";

        // Case 2: Use existing user if found
        if (emailUser.isPresent()) {
            userDetail = emailUser.get();
        } else if (phoneUser.isPresent()) {
            userDetail = phoneUser.get();
        } else {
            // Case 3: Create new user
            userDetail = new UserDetail();
            userDetail.setEmail(userDetailDTO.getEmail());
            
            // Set random password with name included
            rawPassword = com.example.multimedia.file_upload_api.utils.PasswordUtils.generateRandomPassword(userDetailDTO.getFirstName());
            userDetail.setPassword(passwordEncoder.encode(rawPassword)); 
            
            userDetail.setSignupDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            userDetail.setIsActive(true);
            
            // Set optional fields if provided
            if (userDetailDTO.getFirstName() != null) {
            userDetail.setFirstName(userDetailDTO.getFirstName());
            }
            if (userDetailDTO.getLastName() != null) {
            userDetail.setLastName(userDetailDTO.getLastName());
            }
            if (userDetailDTO.getPhoneNumber() != null) {
            userDetail.setPhoneNumber(userDetailDTO.getPhoneNumber());
            }
            
            userDetail.setSuperAdmin(superAdmin);
            userDetail = userDetailRepository.save(userDetail);
        }

        // Get Authorization
        Authorization authorization = authorizationRepository.findByAuthKey(userDetailDTO.getAuthKey())
                .orElseThrow(() -> new RuntimeException("Invalid authKey"));

        // Check if user already has this role
        boolean roleExists = userAuthenticationRepository.existsByUserIdAndAuthKey(userDetail, authorization);
        if (roleExists) {
            throw new RuntimeException("User already registered with this role.");
        }

        // Save new role mapping
        UserAuthentication userAuth = new UserAuthentication();
        userAuth.setUserId(userDetail.getUserId());
        userAuth.setAuthKey(String.valueOf(authorization.getAuthId()));
        userAuth.setIsActive(true);
        userAuthenticationRepository.save(userAuth);

        // Response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetail.getUserId());
        response.put("email", userDetail.getEmail());
        response.put("firstName", userDetail.getFirstName());
        response.put("lastName", userDetail.getLastName());
        response.put("phoneNumber", userDetail.getPhoneNumber());
        response.put("isActive", userDetail.getIsActive());
        response.put("authId", authorization.getAuthId());
        response.put("superAdminId", superAdmin.getSuperAdminId());
        response.put("password", rawPassword); // Return the generated password so the user can see it
        response.put("message", "User registered successfully with role: " + authorization.getAuthName());

        return response;
    }

    public UserDetail getUserById(Long userId) {
        return userDetailRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDetail updateUser(Long userId, UserDetailDTO userDetailDTO) {
        UserDetail existingUser = getUserById(userId);
        
        if (userDetailDTO.getFirstName() != null) {
        existingUser.setFirstName(userDetailDTO.getFirstName());
        }
        if (userDetailDTO.getLastName() != null) {
        existingUser.setLastName(userDetailDTO.getLastName());
        }
        if (userDetailDTO.getPhoneNumber() != null) {
        existingUser.setPhoneNumber(userDetailDTO.getPhoneNumber());
        }
        
        if (userDetailDTO.getPassword() != null && !userDetailDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetailDTO.getPassword()));
        }

        return userDetailRepository.save(existingUser);
    }

    public void deactivateUser(Long userId) {
        UserDetail user = getUserById(userId);
        user.setIsActive(false);
        userDetailRepository.save(user);
    }

    public ServiceResponse resetPasswordByEmail(ForgotPasswordDTO dto) {
        ServiceResponse response = new ServiceResponse();

        Optional<UserDetail> userOpt = userDetailRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            return serviceControllerUtils.prepareMobileResponseInvalidData(response, "No user found with the provided email.");
        }

        UserDetail user = userOpt.get();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userDetailRepository.save(user);

        return serviceControllerUtils.prepareMobileResponseSuccessStatus(
                response, AppConstants.SUCCESSCODE, "Password updated successfully.");
    }
} 