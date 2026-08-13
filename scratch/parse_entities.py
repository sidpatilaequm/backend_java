import os
import re

entity_dir = r"d:\Multimedia Governance project\file-upload-api\src\main\java\com\example\multimedia\file_upload_api\entity"
output_file = r"d:\Multimedia Governance project\file-upload-api\DATABASE_SCHEMA_DOCS.md"

markdown = []
markdown.append("# Database Schema Reference Documentation\n")
markdown.append("This document contains all entity classes, their mapped tables, columns, data types, and foreign key relationships as defined in the Spring Boot project. It is formatted for use by the Data Analysis team.\n")

files = [f for f in os.listdir(entity_dir) if f.endswith(".java")]
files.sort()

for filename in files:
    filepath = os.path.join(entity_dir, filename)
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # Find Class Name
    class_match = re.search(r"public\s+class\s+(\w+)", content)
    if not class_match:
        continue
    class_name = class_match.group(1)

    # Find Table Name
    table_match = re.search(r"@Table\(\s*name\s*=\s*\"([^\"]+)\"", content)
    table_name = table_match.group(1) if table_match else class_name.lower()

    markdown.append(f"## Entity: `{class_name}` (Table: `{table_name}`)\n")
    markdown.append("| Field Name | Java Type | Database Column / Mapping | Details |\n")
    markdown.append("| :--- | :--- | :--- | :--- |\n")

    # Read line by line to locate fields and their @Column or @JoinColumn annotations
    lines = content.split("\n")
    current_col = ""
    current_details = ""
    
    for i, line in enumerate(lines):
        line_strip = line.strip()
        
        # Check for annotations
        col_match = re.search(r"@Column\(\s*name\s*=\s*\"([^\"]+)\"", line_strip)
        if col_match:
            current_col = col_match.group(1)
        
        join_match = re.search(r"@JoinColumn\(\s*name\s*=\s*\"([^\"]+)\"", line_strip)
        if join_match:
            current_col = join_match.group(1)
            current_details = "Foreign Key Mapping"

        # Check for relationship annotations
        if "@OneToOne" in line_strip:
            current_details = "One-to-One Relationship"
            mapped_by_match = re.search(r"mappedBy\s*=\s*\"([^\"]+)\"", line_strip)
            if mapped_by_match:
                current_details += f" (mapped by {mapped_by_match.group(1)})"
        elif "@OneToMany" in line_strip:
            current_details = "One-to-Many Relationship"
            mapped_by_match = re.search(r"mappedBy\s*=\s*\"([^\"]+)\"", line_strip)
            if mapped_by_match:
                current_details += f" (mapped by {mapped_by_match.group(1)})"
        elif "@ManyToOne" in line_strip:
            current_details = "Many-to-One Relationship"
        elif "@ManyToMany" in line_strip:
            current_details = "Many-to-Many Relationship"

        id_match = "@Id" in line_strip
        if id_match:
            current_details = "Primary Key"

        # Detect fields: private/protected/public <Type> <name>;
        field_match = re.search(r"private\s+([\w<>\?,]+)\s+(\w+)\s*;", line_strip)
        if field_match:
            field_type = field_match.group(1)
            field_name = field_match.group(2)
            
            # If no column annotation was found, default to field name
            if not current_col:
                current_col = field_name
            
            markdown.append(f"| `{field_name}` | `{field_type}` | `{current_col}` | {current_details} |\n")
            
            # Reset temporary variables
            current_col = ""
            current_details = ""
            
    markdown.append("\n---\n")

# Save markdown to file
os.makedirs(os.path.dirname(output_file), exist_ok=True)
with open(output_file, "w", encoding="utf-8") as f:
    f.writelines(markdown)

print("SUCCESS: Database schema generated.")
