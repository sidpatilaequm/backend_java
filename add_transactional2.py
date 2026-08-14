file_path = r'src\main\java\com\example\multimedia\file_upload_api\service\UserDetailService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('public Map<String, Object> createEmployeeUser(UserCreationRequestDTO dto) {', '@Transactional\n    public Map<String, Object> createEmployeeUser(UserCreationRequestDTO dto) {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added @Transactional to createEmployeeUser successfully!")
