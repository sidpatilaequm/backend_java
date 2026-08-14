file_path = r'src\main\java\com\example\multimedia\file_upload_api\service\UserDetailService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import org.springframework.stereotype.Service;', 'import org.springframework.stereotype.Service;\nimport org.springframework.transaction.annotation.Transactional;')
content = content.replace('public List<Map<String, Object>> getUsersForCurrentAdmin() {', '@Transactional\n    public List<Map<String, Object>> getUsersForCurrentAdmin() {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added @Transactional successfully!")
