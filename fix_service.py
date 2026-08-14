file_path = r'src\main\java\com\example\multimedia\file_upload_api\service\UserDetailService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

helper = """
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
"""

content = content.replace("public List<Map<String, Object>> getUsersForCurrentAdmin() {", helper + "\n    public List<Map<String, Object>> getUsersForCurrentAdmin() {")
content = content.replace("SuperAdmin currentSuperAdmin = currentUserService.getCurrentSuperAdmin();", "SuperAdmin currentSuperAdmin = getEffectiveSuperAdmin();")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated UserDetailService successfully!")
