import re

file_path = r'src\main\java\com\example\multimedia\file_upload_api\service\UserDetailService.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports = '''import com.example.multimedia.file_upload_api.dto.UserCreationRequestDTO;
import com.example.multimedia.file_upload_api.entity.Employee;
import com.example.multimedia.file_upload_api.entity.Department;
import com.example.multimedia.file_upload_api.enums.UserType;
import com.example.multimedia.file_upload_api.repository.EmployeeRepository;
import com.example.multimedia.file_upload_api.repository.DepartmentRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
'''

content = content.replace('import org.springframework.stereotype.Service;', imports + 'import org.springframework.stereotype.Service;')

deps = '''
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

'''

content = content.replace('    @Autowired\n    private ServiceControllerUtils serviceControllerUtils;', '    @Autowired\n    private ServiceControllerUtils serviceControllerUtils;\n' + deps)

methods = '''
    public List<Map<String, Object>> getUsersForCurrentAdmin() {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        List<UserDetail> users = userDetailRepository.findBySuperAdmin_SuperAdminId(currentSuperAdmin.getSuperAdminId());
        
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

    public Map<String, Object> createEmployeeUser(UserCreationRequestDTO dto) {
        SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();
        
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
'''

content = content.replace('    public Map<String, Object> registerUser(UserDetailDTO userDetailDTO) {', methods + '\n    public Map<String, Object> registerUser(UserDetailDTO userDetailDTO) {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Modified UserDetailService.java successfully")
