import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger",
      database="multimedia_governance"
    )
    cursor = mydb.cursor()
    cursor.execute("SHOW TABLES")
    tables = cursor.fetchall()
    
    for table in tables:
        tname = table[0]
        cursor.execute(f"SHOW COLUMNS FROM `{tname}`")
        columns = cursor.fetchall()
        for col in columns:
            cname = col[0]
            ctype = col[1].decode('utf-8') if type(col[1]) is bytes else col[1]
            if "char" in ctype or "text" in ctype or "enum" in ctype:
                try:
                    cursor.execute(f"SELECT COUNT(*) FROM `{tname}` WHERE `{cname}` = 'approver'")
                    cnt = cursor.fetchone()[0]
                    if cnt > 0:
                        print(f"FOUND 'approver' in table {tname}, column {cname} (Count: {cnt})")
                except:
                    pass
except Exception as e:
    print(e)
