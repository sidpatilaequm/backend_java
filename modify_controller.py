file_path = r'src\main\java\com\example\multimedia\file_upload_api\controller\UserDetailController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports = '''import com.example.multimedia.file_upload_api.dto.UserCreationRequestDTO;
import java.util.List;
'''
content = content.replace('import java.util.Map;', imports + 'import java.util.Map;')

methods = '''
    @GetMapping("/list")
    public ResponseEntity<ServiceResponse> listUsers() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Map<String, Object>> users = userDetailService.getUsersForCurrentAdmin();
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Users fetched successfully.");
            response.addData("users", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Error fetching users: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ServiceResponse> createEmployeeUser(@RequestBody UserCreationRequestDTO dto) {
        ServiceResponse response = new ServiceResponse();
        try {
            Map<String, Object> result = userDetailService.createEmployeeUser(dto);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User created successfully.");
            response.addData("user", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Creation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
'''

content = content.replace('    @PostMapping("/register")', methods + '\n    @PostMapping("/register")')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Modified UserDetailController.java successfully")
