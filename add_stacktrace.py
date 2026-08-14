file_path = r'src\main\java\com\example\multimedia\file_upload_api\controller\UserDetailController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('return ResponseEntity.status(500).body(response);', 'e.printStackTrace();\n            return ResponseEntity.status(500).body(response);')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
