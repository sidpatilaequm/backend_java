file_path = r'src\main\java\com\example\multimedia\file_upload_api\controller\UserDetailController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('@GetMapping("/{userId}")', '@GetMapping("/{userId:\\\\d+}")')
content = content.replace('@PutMapping("/{userId}")', '@PutMapping("/{userId:\\\\d+}")')
content = content.replace('@DeleteMapping("/{userId}")', '@DeleteMapping("/{userId:\\\\d+}")')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated UserDetailController path variables successfully!")
