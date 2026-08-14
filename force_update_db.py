import mysql.connector

try:
    mydb = mysql.connector.connect(
      host="localhost",
      user="root",
      password="tiger"
    )
    mycursor = mydb.cursor()
    mycursor.execute("SELECT user_id FROM multimedia_governance.user_details WHERE user_type = 'approver';")
    ids = mycursor.fetchall()
    print("Found IDs to update:", ids)
    
    for row in ids:
        uid = row[0]
        mycursor.execute("UPDATE multimedia_governance.user_details SET user_type = 'APPROVER' WHERE user_id = %s;", (uid,))
    
    mydb.commit()
    print("Database fully updated!")
except Exception as e:
    print(e)
