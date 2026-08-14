file_path = r'src\main\java\com\example\multimedia\file_upload_api\controller\UserDetailController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Error fetching users: " + e.getMessage());', 'java.io.StringWriter sw = new java.io.StringWriter();\n            e.printStackTrace(new java.io.PrintWriter(sw));\n            response = scutils.prepareMobileResponseErrorStatus(response, AppConstants.ERRORCODE, "Error: " + e.getMessage() + "\\n" + sw.toString());')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added full stack trace to error response!")
