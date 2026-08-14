file_path = r'src\main\java\com\example\multimedia\file_upload_api\enums\UserType.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('EMPLOYEE,\n    PURCHASE_DEPT', 'EMPLOYEE,\n    PURCHASE_DEPT,\n    APPROVER')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added APPROVER to UserType Enum!")
