package com.example.multimedia.file_upload_api.controller;

import com.example.multimedia.file_upload_api.dto.ServiceResponse;
import com.example.multimedia.file_upload_api.dto.UserDetailDTO;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import com.example.multimedia.file_upload_api.service.UserDetailService;
import com.example.multimedia.file_upload_api.utils.AppConstants;
import com.example.multimedia.file_upload_api.utils.ServiceControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.multimedia.file_upload_api.dto.UserCreationRequestDTO;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserDetailController {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private ServiceControllerUtils scutils;

    // requireAdmin()/assertSameTenant() throw SecurityException (403) or a "not found"
    // RuntimeException (404) — previously every failure here, admin-check included, fell through
    // to a generic 500/400, indistinguishable from an actual server error.
    private HttpStatus statusFor(Exception e) {
        if (e instanceof SecurityException) return HttpStatus.FORBIDDEN;
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) return HttpStatus.NOT_FOUND;
        return HttpStatus.BAD_REQUEST;
    }


    @GetMapping("/list")
    public ResponseEntity<ServiceResponse> listUsers() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Map<String, Object>> users = userDetailService.getUsersForCurrentAdmin();
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "Users fetched successfully.");
            response.addData("users", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Error: " + e.getMessage() + "\n" + sw.toString());
            e.printStackTrace();
            return ResponseEntity.status(statusFor(e)).body(response);
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
            e.printStackTrace();
            return ResponseEntity.status(statusFor(e)).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ServiceResponse> registerUser(@RequestBody UserDetailDTO userDetailDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Map<String, Object> result = userDetailService.registerUser(userDetailDTO);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User registered successfully.");
            response.addData("user", result); // Add entire map or specific fields
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Registration failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(statusFor(e)).body(response);
        }
    }
    @GetMapping("/{userId:\\d+}")
    public ResponseEntity<ServiceResponse> getUserById(@PathVariable Long userId) {
        ServiceResponse response = new ServiceResponse();
        try {
            UserDetail userDetail = userDetailService.getUserById(userId);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User fetched successfully.");
            response.addData("user", userDetail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Error fetching user: " + e.getMessage());
            return ResponseEntity.status(statusFor(e)).body(response);
        }
    }

    @PutMapping("/{userId:\\d+}")
    public ResponseEntity<ServiceResponse> updateUser(@PathVariable Long userId, @RequestBody UserDetailDTO userDetailDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            UserDetail updatedUser = userDetailService.updateUser(userId, userDetailDTO);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User updated successfully.");
            response.addData("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Update failed: " + e.getMessage());
            return ResponseEntity.status(statusFor(e)).body(response);
        }
    }

    @DeleteMapping("/{userId:\\d+}")
    public ResponseEntity<ServiceResponse> deactivateUser(@PathVariable Long userId) {
        ServiceResponse response = new ServiceResponse();
        try {
            userDetailService.deactivateUser(userId);
            response = scutils.prepareMobileResponseSuccessStatus(response, AppConstants.SUCCESSCODE, "User deactivated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Deactivation failed: " + e.getMessage());
            return ResponseEntity.status(statusFor(e)).body(response);
        }
    }

    // POST /forgot-password used to live here — removed rather than fixed in place. It took just
    // { email, newPassword } with no proof the caller owned that email, so any authenticated user
    // could overwrite any other account's password. Resetting a user's password is now an
    // admin-only, tenant-checked action via PUT /{userId} (UserDetailService.updateUser) instead.
}
